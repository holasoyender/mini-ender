package events

import commandManager
import config.Env.PREFIX
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.awt.Color
import java.time.Instant

class InteractionHandler: ListenerAdapter() {

    override fun onSelectMenuInteraction(event: SelectMenuInteractionEvent) {
        val type = event.componentId.split("::")[0]
        val command = event.componentId.split("::")[1].split(":")[0]
        val userId = event.componentId.split("::")[1].split(":")[1]

        when (type) {
            "cmd" -> {

                if(userId != event.user.id) {
                    event.reply("No puedes usar este menú!").setEphemeral(true).queue()
                    return
                }

                when (command) {
                    "help" -> {
                        val selected = event.selectedOptions[0].value
                        val category = selected.split(":")[1]

                        if(category != "simple") {

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
                                .setDescription("Usa `${PREFIX}help <Comando>` para más información sobre un comando específico")

                            for (cmd in commands) {
                                embed.addField("`${PREFIX}${cmd.name}`", cmd.description, true)
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
}