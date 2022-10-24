package events

import commandManager
import config.Env.PREFIX
import database.schema.Guild
import handlers.ErrorReporter
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.Modal
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu
import net.dv8tion.jda.api.interactions.components.selections.SelectOption
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.dv8tion.jda.internal.entities.emoji.CustomEmojiImpl
import utils.Constants.OWNER_IDS
import java.awt.Color
import java.time.Instant

class InteractionHandler: ListenerAdapter() {

    override fun onSelectMenuInteraction(event: SelectMenuInteractionEvent) {
        val type = event.componentId.split("::")[0]
        val command = event.componentId.split("::")[1].split(":")[0]
        val userId = event.componentId.split("::")[1].split(":")[1]

        when (type) {
            "cmd" -> {

                if (userId != event.user.id) {
                    event.reply("No puedes usar este menú!").setEphemeral(true).queue()
                    return
                }

                when (command) {
                    "help" -> {

                        val config = Guild.get(event.guild?.id ?: "") ?: Guild(event.guild?.id ?: "", PREFIX ?: "-", null, null)

                        val selected = event.selectedOptions[0].value
                        val category = selected.split(":")[1]

                        if (category == "Dev" && !OWNER_IDS.contains(event.user.id)) {
                            event.reply("No puedes ver los comandos de desarrollador!").setEphemeral(true).queue()
                            return
                        }

                        if (category != "simple") {

                            val allCategories = commandManager?.getCommands()?.groupBy { it.category }
                            val commands = allCategories?.get(category)

                            if (commands == null) {
                                event.reply("No hay comandos en esta categoría!").setEphemeral(true).queue()
                                return
                            }

                            val embed: EmbedBuilder = EmbedBuilder()
                                .setAuthor("Categoría de $category", null, event.jda.selfUser.avatarUrl)
                                .setThumbnail(event.jda.selfUser.avatarUrl)
                                .setColor(Color.decode("#2f3136"))
                                .setTimestamp(Instant.now())
                                .setDescription("Usa `${config.prefix}help <Comando>` para más información sobre un comando específico")

                            for (cmd in commands) {
                                embed.addField("`${config.prefix}${cmd.name}`", cmd.description, true)
                            }
                            event.editMessageEmbeds(embed.build()).queue()
                        } else {
                            val commands = commandManager?.getSimpleCommands()

                            val embed: EmbedBuilder = EmbedBuilder()
                                .setAuthor("Categoría de comandos simples", null, event.jda.selfUser.avatarUrl)
                                .setThumbnail(event.jda.selfUser.avatarUrl)
                                .setColor(Color.decode("#2f3136"))
                                .setTimestamp(Instant.now())
                                .setDescription("Aquí están todos los comandos simples: ```${commands?.joinToString(", ") { it.name }}```")

                            event.editMessageEmbeds(embed.build()).queue()
                        }
                    }
                }
            }
        }
    }

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        val type = event.componentId.split("::")[0]
        val command = event.componentId.split("::")[1].split(":")[0]

        when (type) {
            "cmd" -> {

                val userId = event.componentId.split("::")[1].split(":")[1]

                if (userId != event.user.id) {
                    event.reply("No puedes usar este botón!").setEphemeral(true).queue()
                    return
                }

                when (command) {
                    "help" -> {

                        val config = Guild.get(event.guild?.id ?: "") ?: Guild(event.guild?.id ?: "", PREFIX ?: "-", null, null)

                        val embed: EmbedBuilder = EmbedBuilder()
                            .setAuthor(
                                "Lista de comandos de ${event.jda.selfUser.name}",
                                null,
                                event.jda.selfUser.avatarUrl
                            )
                            .setFooter("> " + event.user.asTag, event.user.avatarUrl ?: "")
                            .setThumbnail("https://cdn.discordapp.com/attachments/934142973418016838/1025062210013249556/emoji.png")
                            .setColor(Color.decode("#2f3136"))
                            .setTimestamp(Instant.now())
                            .setDescription(
                                """**Hola** :wave:, soy `${event.jda.selfUser.name}`, un bot de ayuda para el servidor de **KenaBot**!
                                Para obtener información de un comando en específico usa `${config.prefix}help <comando>`
                                """
                            )

                        val categories = commandManager?.getCommands()?.groupBy { it.category }

                        event.editMessageEmbeds(embed.build()).setActionRow(
                            SelectMenu.create("cmd::help:${event.user.id}")
                                .setPlaceholder("Selecciona una categoría")
                                .setMaxValues(1)
                                .setMinValues(0)
                                .addOptions(
                                    categories?.map {
                                        SelectOption.of(it.key, "help:${it.key}")
                                            .withDescription("Comandos de la categoría ${it.key}").withEmoji(
                                                Emoji.fromCustom(CustomEmojiImpl("rigth", 940316141782458418, false))
                                            )
                                    } ?: listOf()
                                ).addOption(
                                    "Comandos simples", "help:simple", "Lista de comandos simples", Emoji.fromCustom(
                                        CustomEmojiImpl("slash", 941024012270710874, false)
                                    )
                                )
                                .build()
                        ).queue()
                    }

                    "error" -> {

                        val body: TextInput = TextInput.create("body", "Descripción del error", TextInputStyle.PARAGRAPH)
                            .setPlaceholder("Una breve descripción del error que has encontrado")
                            .setMinLength(30)
                            .setMaxLength(4000)
                            .build()

                        val modal: Modal = Modal.create("error", "Reportar un error - KenaBot")
                            .addActionRows(ActionRow.of(body))
                            .build()

                        event.replyModal(modal).queue()
                    }
                }

            }
            "error" -> {
                when(command) {
                    "acknowledge" -> ErrorReporter.acknowledgeError(event)
                    "solve" -> ErrorReporter.solveError(event)
                    "delete" -> ErrorReporter.deleteError(event)
                }
            }
        }
    }
}