package events

import config.DefaultConfig
import config.Env
import database.schema.Error
import database.schema.Guild
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.components.buttons.Button
import plugins.antilink.LinksInteractions
import plugins.suggest.SuggestionManager
import utils.Emojis
import utils.Emojis.f
import java.awt.Color
import java.util.*

class ModalHandler: ListenerAdapter() {

    override fun onModalInteraction(event: ModalInteractionEvent) {

        when (event.modalId.split("::")[0]) {
            "error" -> {

                val error =
                    event.getValue("body")?.asString ?: return event.reply("No has escrito nada!").setEphemeral(true)
                        .queue()

                val id = "err-"+(0..1000000).random().toString()

                val dbError = Error(
                    id,
                    userid = event.user.id,
                    error = error,
                    date = Date().toString(),
                    acknowledged = false,
                    solved = false,
                )

                dbError.save()

                val embed = EmbedBuilder()
                    .setDescription("Se ha reportado un error con id `$id`")
                    .addField("Error", "```$error```", false)
                    .addField("Usuario", event.user.asTag + " (${event.user.id})", false)
                    .addField("ID", "`$id`", true)
                    .addField("Estado", "Sin clasificar", true)
                    .setColor(Color.decode("#2f3136"))
                    .setThumbnail("https://cdn.discordapp.com/attachments/1026084700189638738/1026237793296470106/emoji.png")
                    .setAuthor("Nuevo error reportado", null, event.jda.selfUser.avatarUrl)

                event.jda.getTextChannelById(Env.ERROR_CHANNEL_ID!!)?.sendMessageEmbeds(embed.build())?.addActionRow(
                    Button.primary("error::acknowledge:$id", "Marcar como identificado"),
                    Button.success("error::solve:$id", "Marcar como resuelto"),
                    Button.danger("error::delete:$id", "Eliminar")
                )?.queue()

                event.reply("¡Gracias por reportar el error!\nTu error ha recibido la ID `${id}`").setEphemeral(true).queue()
            }
            "links.tempban" -> LinksInteractions.handleTempBanModal(event)
            "links.tempmute" -> LinksInteractions.handleTempMuteModal(event)
            "cmd" -> {

                val args = event.modalId.split("::")[1].split(":")

                when (args[0]) {
                    "suggest" -> {

                        val suggestion = event.getValue("body")?.asString ?: return event.reply("No has escrito nada!")
                            .setEphemeral(true)
                            .queue()

                        val config = Guild.get(event.guild!!.id) ?: DefaultConfig.get(event.guild!!.id)

                        if (config.suggestChannel.isBlank())
                            return event.reply("No hay un canal de sugerencias configurado en este servidor!")
                                .setEphemeral(true)
                                .queue()

                        val channel =
                            event.guild!!.getTextChannelById(config.suggestChannel) ?: event.guild!!.getNewsChannelById(
                                config.suggestChannel
                            ) ?: return event.reply("No se ha encontrado el canal de sugerencias!").setEphemeral(true)
                                .queue()

                        val ok = SuggestionManager.createSuggestion(suggestion, config, event.user, channel)

                        if (ok)
                            event.reply("${Emojis.success}  Tu sugerencia ha sido enviada correctamente!").setEphemeral(true)
                                .queue()
                        else
                            event.reply("${f(Emojis.error)}  Ha ocurrido un error al enviar tu sugerencia!").setEphemeral(true)
                                .queue()
                    }
                }

            }
        }

    }
}