package plugins.giveaway

import database.schema.Sorteo
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.utils.TimeFormat
import net.dv8tion.jda.internal.entities.emoji.CustomEmojiImpl
import utils.Emojis
import utils.Emojis.f
import java.awt.Color

object GiveawayInteractions {

    fun handleJoinButton(event: ButtonInteractionEvent) {

        val messageId = event.messageId
        val userId = event.user.id

        val giveaway = Sorteo.get(messageId)

        if (giveaway != null) {

            if (giveaway.clickers.contains(userId)) {
                event.reply("${Emojis.warning}  Ya estás participando en este sorteo!").setEphemeral(true).addActionRow(
                    Button.danger("cmd::giveaway:leave:$messageId", "Dejar de participar")
                ).queue()
                return
            }

            giveaway.clickers = giveaway.clickers.plus(userId)
            giveaway.save()

            event.reply("${Emojis.giveaway}  Te has unido al sorteo! Buena suerte :D").setEphemeral(true).queue()

        } else {
            event.reply("${f(Emojis.error)}  Parece que este sorteo ya no existe!").setEphemeral(true).queue()
        }
    }

    fun handleRedoButton(event: ButtonInteractionEvent) {

        val messageId = event.componentId.split("::")[1].split(":")[2]

        val giveaway = Sorteo.get(messageId)

        if (giveaway != null) {

            if (!giveaway.ended) {
                event.reply("${Emojis.warning}  Este sorteo aún no ha terminado!").setEphemeral(true).queue()
                return
            }
            val channel = event.guild!!.getTextChannelById(giveaway.channelId)

            if (channel == null) {
                event.reply("${f(Emojis.error)}  Parece que el canal de este sorteo ya no existe!").setEphemeral(true)
                    .queue()
                return
            } else {

                channel.retrieveWebhooks().queue { webhooks ->
                    val webhook = webhooks.firstOrNull { w -> w.name == "Sorteos" }

                    if (webhook == null) {
                        event.reply("${f(Emojis.error)} No se ha encontrado el webhook del sorteo").queue()
                    } else {
                        event.reply("${Emojis.loading} Repitiendo el sorteo...").setEphemeral(true).queue { msg ->
                            GiveawayManager.redoGiveaway(webhook, giveaway, msg, event.guild!!)
                        }
                    }
                }

            }
        } else {
            event.reply("${f(Emojis.error)}  Parece que este sorteo ya no existe!").setEphemeral(true).queue()
        }
    }

    fun handleEndButton(event: ButtonInteractionEvent) {

        val messageId = event.componentId.split("::")[1].split(":")[2]

        val giveaway = Sorteo.get(messageId)

        if (giveaway != null) {

            if (giveaway.ended) {
                event.reply("${Emojis.warning}  Este sorteo ya ha terminado!").setEphemeral(true).queue()
                return
            }
            val channel = event.guild!!.getTextChannelById(giveaway.channelId)

            if (channel == null) {
                event.reply("${f(Emojis.error)}  Parece que el canal de este sorteo ya no existe!").setEphemeral(true)
                    .queue()
                return
            } else {

                channel.retrieveWebhooks().queue { webhooks ->
                    val webhook = webhooks.firstOrNull { w -> w.name == "Sorteos" }

                    if (webhook == null) {
                        event.reply("${f(Emojis.error)} No se ha encontrado el webhook del sorteo").queue()
                    } else {
                        event.reply("${Emojis.loading} Finalizando el sorteo...").setEphemeral(true).queue { msg ->
                            GiveawayManager.endGiveaway(webhook, giveaway, msg, event.guild!!)
                        }
                    }
                }

            }
        } else {
            event.reply("${f(Emojis.error)}  Parece que este sorteo ya no existe!").setEphemeral(true).queue()
        }
    }

    fun handleReloadButton(event: ButtonInteractionEvent) {
        val messageId = event.componentId.split("::")[1].split(":")[2]

        val giveaway = Sorteo.get(messageId)

        if (giveaway != null) {

            event.editMessageEmbeds(
                EmbedBuilder()
                    .setTitle("Información del sorteo")
                    .setColor(Color.decode("#2f3136"))
                    .addField("Canal", "<#${giveaway.channelId}>", true)
                    .addField("Tiempo", "${TimeFormat.DEFAULT.format(giveaway.endAfter + giveaway.startedAt)}  (${ TimeFormat.RELATIVE.format(giveaway.endAfter + giveaway.startedAt)})", true)
                    .addField("Ganadores", "`${giveaway.winnerCount}` ganador${if(giveaway.winnerCount > 1) "es" else ""}", true)
                    .addField("Premio", "```${giveaway.prize}```", false)
                    .addField("Host", "<@${giveaway.hostId}>", true)
                    .addField("Mensaje", "[Click aquí](https://discord.com/channels/${event.guild!!.id}/${giveaway.channelId}/${giveaway.messageId})", true)
                    .addField("Estado", if(giveaway.ended) "Terminado" else "En curso", true)
                    .addField("Ganadores", if(giveaway.ended) giveaway.winnerIds.joinToString(", ") { "<@${it}>" } else "Ninguno", true)
                    .addField("Participantes totales", "`${giveaway.clickers.size}` participantes", true)
                    .setThumbnail(event.jda.selfUser.avatarUrl ?: event.jda.selfUser.defaultAvatarUrl)
                    .build()
            ).setActionRow(
                Button.danger("cmd::giveaway:end:${giveaway.messageId}", "Acabar el sorteo").withDisabled(giveaway.ended),
                Button.success("cmd::giveaway:redo:${giveaway.messageId}", "Repetir el sorteo").withDisabled(!giveaway.ended),
                Button.link("https://discord.com/channels/${event.guild!!.id}/${giveaway.channelId}/${giveaway.messageId}", "Ir al mensaje"),
                Button.primary("cmd::giveaway:reload:${giveaway.messageId}", Emoji.fromCustom(CustomEmojiImpl("loop", 952242523521294456, false)))
            ).queue()

        } else {
            event.reply("${f(Emojis.error)}  Parece que este sorteo ya no existe!").setEphemeral(true).queue()
        }
    }

    fun handleLeaveButton(event: ButtonInteractionEvent) {

        val messageId = event.componentId.split("::")[1].split(":")[2]
        val userId = event.user.id

        val giveaway = Sorteo.get(messageId)

        if (giveaway != null) {

            if (!giveaway.clickers.contains(userId)) {
                event.reply("${Emojis.warning}  No estás participando en este sorteo!").setEphemeral(true).queue()
                return
            }

            giveaway.clickers = giveaway.clickers.filter { it != userId }.toTypedArray()
            giveaway.save()

            event.reply("${Emojis.success}  Has dejado el sorteo!").setEphemeral(true).queue()

        } else {
            event.reply("${f(Emojis.error)}  Parece que este sorteo ya no existe!").setEphemeral(true).queue()
        }
    }

}