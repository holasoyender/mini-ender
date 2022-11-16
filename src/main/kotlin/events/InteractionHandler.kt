package events

import commandManager
import config.DefaultConfig
import database.schema.Guild
import database.schema.Infraction
import handlers.ErrorReporter
import handlers.InfractionButtons
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.selections.SelectOption
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.dv8tion.jda.api.interactions.modals.Modal
import net.dv8tion.jda.internal.entities.emoji.CustomEmojiImpl
import plugins.antilink.LinksInteractions
import plugins.giveaway.GiveawayInteractions
import slashCommandManager
import utils.Constants.OWNER_IDS
import utils.Emojis
import java.awt.Color
import java.time.Instant

class InteractionHandler: ListenerAdapter() {

    override fun onStringSelectInteraction(event: StringSelectInteractionEvent) {
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

                        val config = Guild.get(event.guild?.id ?: "") ?: DefaultConfig.get()

                        val selected = event.selectedOptions[0].value
                        val category = selected.split(":")[1]

                        if (category == "Dev" && !OWNER_IDS.contains(event.user.id)) {
                            event.reply("No puedes ver los comandos de desarrollador!").setEphemeral(true).queue()
                            return
                        }

                        when (category) {
                            "simple" -> {
                                val commands = commandManager?.getSimpleCommands()

                                if (commands == null) {
                                    event.reply("No hay comandos en esta categoría!").setEphemeral(true).queue()
                                    return
                                }

                                val embed: EmbedBuilder = EmbedBuilder()
                                    .setAuthor("Categoría de comandos simples", null, event.jda.selfUser.avatarUrl)
                                    .setThumbnail(event.jda.selfUser.avatarUrl)
                                    .setColor(Color.decode("#2f3136"))
                                    .setTimestamp(Instant.now())
                                    .setDescription("Aquí están todos los comandos simples: ```${commands.joinToString(", ") { it.name }}```")

                                event.editMessageEmbeds(embed.build()).queue()
                            }
                            "slash" -> {
                                val commands = slashCommandManager?.getCommands()

                                if (commands == null) {
                                    event.reply("No hay comandos en esta categoría!").setEphemeral(true).queue()
                                    return
                                }

                                val embed: EmbedBuilder = EmbedBuilder()
                                    .setAuthor("Categoría de comandos slash", null, event.jda.selfUser.avatarUrl)
                                    .setThumbnail(event.jda.selfUser.avatarUrl)
                                    .setColor(Color.decode("#2f3136"))
                                    .setTimestamp(Instant.now())

                                for (cmd in commands) {
                                    embed.addField("`/${cmd.name}`", cmd.description, true)
                                }

                                event.editMessageEmbeds(embed.build()).queue()
                            }
                            else -> {
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
                            }
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

                if(isUserId(userId))
                if (userId != event.user.id) {
                    event.reply("No puedes usar este botón!").setEphemeral(true).queue()
                    return
                }

                when (command) {
                    "help" -> {

                        val config = Guild.get(event.guild?.id ?: "") ?: DefaultConfig.get()

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
                            StringSelectMenu.create("cmd::help:${event.user.id}")
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
                                .addOption("Slash commands", "help:slash", "Lista de comandos de barra diagonal", Emoji.fromCustom(CustomEmojiImpl("slash", 941024012270710874, false)))

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
                    "giveaway" -> {

                        when(event.componentId.split("::")[1].split(":")[1]) {
                            "enter" -> GiveawayInteractions.handleJoinButton(event)
                            "redo" -> GiveawayInteractions.handleRedoButton(event)
                            "end" -> GiveawayInteractions.handleEndButton(event)
                            "reload" -> GiveawayInteractions.handleReloadButton(event)
                            "leave" -> GiveawayInteractions.handleLeaveButton(event)
                        }
                    }
                    "links" -> {

                        when(event.componentId.split("::")[1].split(":")[1]) {
                            "accept" -> LinksInteractions.handleAcceptButton(event)
                            "ban" -> LinksInteractions.handleBanButton(event)
                            "kick" -> LinksInteractions.handleKickButton(event)
                            "mute" -> LinksInteractions.handleMuteButton(event)
                            "warn" -> LinksInteractions.handleWarnButton(event)
                            "tempban" -> LinksInteractions.handleTempBanButton(event)
                            "tempmute" -> LinksInteractions.handleTempMuteButton(event)
                            "delete" -> LinksInteractions.handleDeleteButton(event)
                        }
                    }
                    "infrs" -> {
                        when(event.componentId.split("::")[1].split(":")[2]) {
                            "prev" -> InfractionButtons.prevPage(event)
                            "next" -> InfractionButtons.nextPage(event)
                            "reload" -> InfractionButtons.reload(event)
                        }
                    }
                    "delinfr-confirm" -> {

                        val infID = event.componentId.split("::")[1].split(":")[2]
                        val id = infID.toLongOrNull()

                        if(id == null) {
                            event.editMessage("El ID de la infracción no es válido").setComponents().queue()
                            return
                        }

                        val infraction = Infraction.get(id, event.guild!!.id)

                        if(infraction == null) {
                            event.editMessage("No se ha encontrado ninguna infracción con ese ID").setComponents().queue()
                            return
                        }

                        infraction.delete()
                        event.editMessage("${Emojis.success}  La infracción `${infraction.id}` ha sido eliminada").setComponents().queue()

                    }
                    "delinfrs-confirm" -> {

                        val userID = event.componentId.split("::")[1].split(":")[2]

                        val infraction = Infraction.getAllByUserId(event.guild!!.id, userID)

                        if(infraction.isEmpty()) {
                            event.editMessage("Ese usuario no tiene infracciones").setComponents().queue()
                            return
                        }

                        infraction.forEach { it.delete() }
                        event.editMessage("${Emojis.success}  Las infracciones del usuario con ID `${userID}` han sido eliminadas").setComponents().queue()

                    }
                    "cancel" -> {
                        event.editMessage("${Emojis.warning}  Operación cancelada").setEmbeds().setComponents().queue()
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

    private fun isUserId(id: String): Boolean {
        return try {
            val i = id.toLong()
            i > 10000000
        } catch (e: NumberFormatException) {
            false
        }
    }
}