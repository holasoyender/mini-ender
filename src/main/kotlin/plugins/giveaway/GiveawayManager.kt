package plugins.giveaway

import database.schema.Sorteo
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.utils.TimeFormat
import utils.Emojis
import utils.Emojis.f
import webhook.external.JDAWebhookClient
import webhook.send.WebhookEmbedBuilder
import webhook.send.WebhookMessageBuilder
import webhook.send.component.ActionRow
import webhook.send.component.Button
import webhook.send.component.PartialEmoji
import java.awt.image.BufferedImage
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.util.*
import javax.imageio.ImageIO


object GiveawayManager {

    fun createGiveaway(
        guild: Guild,
        channel: TextChannel,
        time: Long,
        winners: Long,
        prize: String,
        host: User,
        hook: InteractionHook
    ): Boolean {

        try {

            channel.retrieveWebhooks().queue {

                val webhook = it.firstOrNull { w -> w.name == "Sorteos" }

                if (webhook == null) {
                    try {
                        val url = URL(guild.iconUrl!!)
                        val img: BufferedImage = ImageIO.read(url)
                        val random = (0..100000).random()
                        val file = File("temp-${random}.png")
                        ImageIO.write(img, "png", file)
                        channel.createWebhook("Sorteos").setAvatar(Icon.from(file)).queue { wh ->
                            Files.delete(file.toPath())

                            sendWithWebhook(wh, guild, channel, time, winners, prize, host)

                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                        hook.editOriginal("No se ha podido crear el sorteo: \n`El webhook no se ha podido crear`")
                            .queue()
                    }
                } else {
                    sendWithWebhook(webhook, guild, channel, time, winners, prize, host)
                }
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    private fun sendWithWebhook(
        webhook: Webhook,
        guild: Guild,
        channel: TextChannel,
        time: Long,
        winners: Long,
        prize: String,
        host: User,
    ) {
        val builder = JDAWebhookClient.from(webhook)
        val timestamp = System.currentTimeMillis() + time

        val banner = Banner(guild).getBanner()
        val random = (0..100000).random()
        val file = File("temp-${random}.png")
        ImageIO.write(banner, "png", file)

        val message = WebhookMessageBuilder()
            .addEmbeds(
                WebhookEmbedBuilder.fromJDA(
                    EmbedBuilder()
                        .setAuthor(
                            "Ha comenzado un sorteo!",
                            null,
                            "https://cdn.discordapp.com/attachments/755000173922615336/1037465851122696293/emoji..gif"
                        )
                        .setColor(0x2f3136)
                        .setThumbnail(guild.iconUrl ?: guild.jda.selfUser.avatarUrl)
                        .setTitle("Premio: $prize")
                        .setFooter("Acaba el")
                        .setTimestamp(Date(timestamp).toInstant())
                        .setImage("attachment://banner.png")
                        .setDescription(
                            "\n${Emojis.OWNER}  Alojado por: <@!${host.id}>\n${Emojis.time}  Acaba: ${
                                TimeFormat.DEFAULT.format(
                                    timestamp
                                )
                            }  (${TimeFormat.RELATIVE.format(timestamp)})\n\n${Emojis.right}  Número de ganadores: `${winners}`"
                        )
                        .build()
                )
                    .build()
            )
            .addComponents(
                ActionRow.of(
                    Button.primary("cmd::giveaway:enter", "Entrar al sorteo").withEmoji(PartialEmoji.of("tadaa", "1037465732159656117", true)),
                )
            ).addFile("banner.png", file)

        /*
        * TODO:
        *  - Loop para comprobar si ya ha acabado algún sorteo
        * */

        builder.send(message.build()).whenComplete { msg, _ ->
            val sorteo = Sorteo(
                guildId = guild.id,
                channelId = channel.id,
                messageId = msg.id.toString(),
                hostId = host.id,
                endAfter = time,
                startedAt = System.currentTimeMillis(),
                prize = prize,
                winnerCount = winners.toInt(),
                ended = false,
                winnerIds = arrayOf(),
                clickers = arrayOf()
            )

            sorteo.save()
            Files.delete(file.toPath())
            builder.close()
        }
    }

    fun endGiveaway(
        webhook: Webhook,
        giveaway: Sorteo,
        hook: InteractionHook?,
        guild: Guild,
    ) {

        val winners = WinnerChooser(giveaway.winnerCount, giveaway.clickers, giveaway.winnerIds).result
        val builder = JDAWebhookClient.from(webhook)

        if (winners.isEmpty()) {
            giveaway.ended = true
            giveaway.winnerIds = arrayOf()
            giveaway.save()

            val channelMessage = WebhookMessageBuilder()
                .setContent("${Emojis.warning} No ha habido participantes suficientes para el sorteo de **${giveaway.prize}**")
                .addComponents(
                    ActionRow.of(
                        Button.link(
                            "https://discord.com/channels/${giveaway.guildId}/${giveaway.channelId}/${giveaway.messageId}",
                            "Ir al sorteo"
                        ),
                        Button.secondary("cmd::giveaway:count", "${giveaway.clickers.size} Participantes", true),
                    )
                )

            builder.send(channelMessage.build()).whenComplete { _, _ ->

                val editMessage = WebhookMessageBuilder()
                    .reset()
                    .addEmbeds(
                        WebhookEmbedBuilder.fromJDA(
                            EmbedBuilder()
                                .setAuthor(
                                    "Sorteo finalizado!",
                                    null,
                                    "https://cdn.discordapp.com/attachments/755000173922615336/1037465851122696293/emoji..gif"
                                )
                                .setColor(0x2f3136)
                                .setThumbnail(guild.iconUrl ?: guild.jda.selfUser.avatarUrl)
                                .setTitle("Premio: ${giveaway.prize}")
                                .setFooter("Acabó el")
                                .setTimestamp(Date(giveaway.startedAt + giveaway.endAfter).toInstant())
                                .setDescription(
                                    "\n⭐  Alojado por: <@!${giveaway.hostId}>\n\n\uD83C\uDF89 Ganador: Nadie ha participado"
                                )
                                .build()
                        ).build()
                    ).addComponents(
                        ActionRow.of(
                            Button.primary("cmd::giveaway:enter", "Entrar al sorteo", true)
                                .withEmoji(PartialEmoji.of("tadaa", "1037465732159656117", true)),
                        )
                    )

                builder.edit(giveaway.messageId, editMessage.build()).whenComplete { _, _ ->
                    builder.close()
                    hook?.editOriginal("${Emojis.success}  Se ha finalizado el sorteo correctamente")?.queue()
                }
            }
        } else {

            giveaway.ended = true
            giveaway.winnerIds = winners.toTypedArray()
            giveaway.save()

            val channelMessage = WebhookMessageBuilder()
                .setContent("${Emojis.giveaway}  Enhorabuena ${winners.joinToString { "<@!$it> " }} ${if (winners.size > 1) "habéis" else "has"} ganado **${giveaway.prize}**!! Gracias a <@!${giveaway.hostId}> por este sorteo!")
                .addComponents(
                    ActionRow.of(
                        Button.link(
                            "https://discord.com/channels/${giveaway.guildId}/${giveaway.channelId}/${giveaway.messageId}",
                            "Ir al sorteo"
                        ),
                        Button.secondary("cmd::giveaway:count", "${giveaway.clickers.size} Participantes", true),
                    )
                )

            builder.send(channelMessage.build()).whenComplete { _, _ ->

                val editMessage = WebhookMessageBuilder()
                    .reset()
                    .addEmbeds(
                        WebhookEmbedBuilder.fromJDA(
                            EmbedBuilder()
                                .setAuthor(
                                    "Sorteo finalizado!",
                                    null,
                                    "https://cdn.discordapp.com/attachments/755000173922615336/1037465851122696293/emoji..gif"
                                )
                                .setColor(0x2f3136)
                                .setThumbnail(guild.iconUrl ?: guild.jda.selfUser.avatarUrl)
                                .setTitle("Premio: ${giveaway.prize}")
                                .setFooter("Acabó el")
                                .setTimestamp(Date(giveaway.startedAt + giveaway.endAfter).toInstant())
                                .setDescription(
                                    "\n⭐  Alojado por: <@!${giveaway.hostId}>\n\n\uD83C\uDF89 ${if (winners.size > 1) "Ganadores" else "Ganador"}: ${winners.joinToString { "<@!$it> " }}"
                                )
                                .build()
                        ).build()
                    ).addComponents(
                        ActionRow.of(
                            Button.primary("cmd::giveaway:enter", "Entrar al sorteo", true)
                                .withEmoji(PartialEmoji.of("tadaa", "1037465732159656117", true)),
                        )
                    )

                builder.edit(giveaway.messageId, editMessage.build()).whenComplete { _, _ ->
                    builder.close()
                    hook?.editOriginal("${Emojis.success}  Se ha finalizado el sorteo correctamente")?.queue()
                }
            }
        }
    }

    fun redoGiveaway(
        webhook: Webhook,
        giveaway: Sorteo,
        hook: InteractionHook,
        guild: Guild,
    ) {

        val winners = WinnerChooser(giveaway.winnerCount, giveaway.clickers, giveaway.winnerIds).result

        if(winners.isEmpty()) {
            hook.editOriginal("${f(Emojis.error)}  No se ha podido acabar el sorteo: \n`No hay participantes suficientes`").queue()
            return
        }

        giveaway.winnerIds = winners.toTypedArray()
        giveaway.save()

        val builder = JDAWebhookClient.from(webhook)

        val channelMessage = WebhookMessageBuilder()
            .setContent("${Emojis.giveaway}  Enhorabuena ${winners.joinToString { "<@!$it> " }} ${if(winners.size > 1) "habéis" else "has"} ganado el reroll de **${giveaway.prize}**!! Gracias a <@!${giveaway.hostId}> por este sorteo!")
            .addComponents(
                ActionRow.of(
                    Button.link("https://discord.com/channels/${giveaway.guildId}/${giveaway.channelId}/${giveaway.messageId}", "Ir al sorteo"),
                    Button.secondary("cmd::giveaway:count", "${giveaway.clickers.size} Participantes", true),
                )
            )

        builder.send(channelMessage.build()).whenComplete { _, _ ->

            val editMessage = WebhookMessageBuilder()
                .reset()
                .addEmbeds(
                    WebhookEmbedBuilder.fromJDA(
                        EmbedBuilder()
                            .setAuthor(
                                "Sorteo finalizado!",
                                null,
                                "https://cdn.discordapp.com/attachments/755000173922615336/1037465851122696293/emoji..gif"
                            )
                            .setColor(0x2f3136)
                            .setThumbnail(guild.iconUrl ?: guild.jda.selfUser.avatarUrl)
                            .setTitle("Premio: ${giveaway.prize}")
                            .setFooter("Acabó el")
                            .setTimestamp(Date(giveaway.startedAt + giveaway.endAfter).toInstant())
                            .setDescription(
                                "\n⭐  Alojado por: <@!${giveaway.hostId}>\n\n\uD83C\uDF89 ${if(winners.size > 1) "Ganadores" else "Ganador"}: ${winners.joinToString { "<@!$it> " }}"
                            )
                            .build()
                    ).build()
                ).addComponents(
                    ActionRow.of(
                        Button.primary("cmd::giveaway:enter", "Entrar al sorteo", true).withEmoji(PartialEmoji.of("tadaa", "1037465732159656117", true)),
                    )
                )

            builder.edit(giveaway.messageId, editMessage.build()).whenComplete { _, _ ->
                builder.close()
                hook.editOriginal("${Emojis.success}  Se ha rehecho el sorteo correctamente").queue()
            }
        }
    }
}