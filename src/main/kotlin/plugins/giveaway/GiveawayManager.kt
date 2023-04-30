package plugins.giveaway

import config.Env
import database.schema.Sorteo
import enums.Severity
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.utils.TimeFormat
import plugins.warnings.WarningsManager
import utils.Emojis
import utils.Emojis.f
import webhook.external.JDAWebhookClient
import webhook.send.WebhookEmbedBuilder
import webhook.send.WebhookMessageBuilder
import webhook.send.component.ActionRow
import webhook.send.component.Button
import webhook.send.component.PartialEmoji
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.URL
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
        style: String,
        hook: InteractionHook
    ): Boolean {

        try {

            channel.retrieveWebhooks().queue {

                val webhook = it.firstOrNull { w -> w.name == "Sorteo" }

                if (webhook == null) {
                    try {
                        val img = ImageIO.read(URL("https://cdn.discordapp.com/attachments/855118494005198939/1045436479729582121/unknown.png"))
                        val os = ByteArrayOutputStream()

                        ImageIO.write(img, "png", os)

                        val inputStream: InputStream = ByteArrayInputStream(os.toByteArray())

                        channel.createWebhook("Sorteo").setAvatar(Icon.from(inputStream)).queue ({ wh ->
                            sendWithWebhook(wh, guild, channel, time, winners, prize, host, style)
                        }, { thr ->
                            thr.printStackTrace()
                            hook.editOriginal("No se ha podido crear el sorteo: \n`El webhook no se ha podido crear`")
                                .queue()
                        })
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                        hook.editOriginal("No se ha podido crear el sorteo: \n`El webhook no se ha podido crear`")
                            .queue()
                    }
                } else {
                    sendWithWebhook(webhook, guild, channel, time, winners, prize, host, style)
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
        style: String
    ) {
        val builder = JDAWebhookClient.from(webhook)
        val timestamp = System.currentTimeMillis() + time

        val banner = when (style) {
            "ibai" -> ImageIO.read(URL("https://cdn.discordapp.com/attachments/859486644578025472/1046406289498058762/unknown.png"))
            else -> try {
                Banner(guild).getBanner()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        val embed = EmbedBuilder()
            .setColor(0x2f3136)
            .setFooter("$winners ${if (winners == 1L) "Ganador" else "Ganadores"} | Acaba el")
            .setTimestamp(Date(timestamp).toInstant())

        if (banner != null)
            embed.setImage("attachment://banner.png")

        when (style) {
            "minimum" -> {
                embed.setAuthor(
                    "Ha comenzado un sorteo!",
                    null,
                    "https://cdn.discordapp.com/attachments/755000173922615336/1037465851122696293/emoji..gif"
                )
                    .setTitle("Premio: $prize")
                    .addField("Tiempo restante", TimeFormat.RELATIVE.format(timestamp), true)
                    .addField("Alojado por", "<@!${host.id}>", true)
            }

            "ibai" -> {
                embed.setTitle(prize)
                    .addField("Tiempo restante", TimeFormat.RELATIVE.format(timestamp), true)
                    .addField("Alojado por", "<@!${host.id}>", true)
            }

            else -> {
                embed.setAuthor(
                    "Ha comenzado un sorteo!",
                    null,
                    "https://cdn.discordapp.com/attachments/755000173922615336/1037465851122696293/emoji..gif"
                )
                    .setTitle("Premio: $prize")
                    .setDescription(
                        "\n${Emojis.OWNER}  Alojado por: <@!${host.id}>\n${Emojis.time}  Acaba: ${
                            TimeFormat.DEFAULT.format(
                                timestamp
                            )
                        }  (${TimeFormat.RELATIVE.format(timestamp)})\n\n${Emojis.right}  Número de ganadores: `${winners}`"
                    )
                    .setThumbnail(guild.iconUrl ?: guild.jda.selfUser.avatarUrl)
            }
        }

        val message = WebhookMessageBuilder()
            .addEmbeds(WebhookEmbedBuilder.fromJDA(embed.build()).build())
            .addComponents(
                ActionRow.of(
                    Button.primary("cmd::giveaway:enter", "Entrar al sorteo")
                        .withEmoji(PartialEmoji.of("tadaa", "1037465732159656117", true)),
                )
            )

        if (banner != null) {
            val os = ByteArrayOutputStream()
            ImageIO.write(banner, "png", os)
            val inputStream: InputStream = ByteArrayInputStream(os.toByteArray())
            message.addFile("banner.png", inputStream)
        }

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
                clickers = arrayOf(),
                style = style
            )

            sorteo.save()

            builder.edit(
                msg.id, message.setComponents(
                    ActionRow.of(
                        Button.primary("cmd::giveaway:enter", "Entrar al sorteo")
                            .withEmoji(PartialEmoji.of("tadaa", "1037465732159656117", true)),
                        Button.link("${Env.API_URL}/sorteo/${msg.id}", "Ver sorteo")
                    )
                ).build()
            )

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

                val embed = EmbedBuilder()
                    .setColor(0x2f3136)
                    .setFooter("Acabó el")
                    .setTimestamp(Date(giveaway.startedAt + giveaway.endAfter).toInstant())

                when(giveaway.style) {
                    "minimum" -> {
                        embed.setAuthor(
                            "Sorteo finalizado!",
                            null,
                            "https://cdn.discordapp.com/attachments/755000173922615336/1037465851122696293/emoji..gif"
                        )
                            .setTitle("Premio: ${giveaway.prize}")
                            .addField("Alojado por", "<@!${giveaway.hostId}>", true)
                            .addField("Ganador", "Nadie ha participado", true)
                    }
                    "ibai" -> {
                        embed.setTitle(giveaway.prize)
                            .addField("Alojado por", "<@!${giveaway.hostId}>", true)
                            .addField("Ganador", "Nadie ha participado", true)
                            .setImage("https://cdn.discordapp.com/attachments/859486644578025472/1046406289498058762/unknown.png")
                    }
                    else -> {
                        embed.setAuthor(
                            "Sorteo finalizado!",
                            null,
                            "https://cdn.discordapp.com/attachments/755000173922615336/1037465851122696293/emoji..gif"
                        )
                            .setTitle("Premio: ${giveaway.prize}")
                            .setDescription(
                                "\n⭐  Alojado por: <@!${giveaway.hostId}>\n\n\uD83C\uDF89 Ganador: Nadie ha participado"
                            )
                            .setThumbnail(guild.iconUrl ?: guild.jda.selfUser.avatarUrl)
                    }
                }

                val editMessage = WebhookMessageBuilder()
                    .reset()
                    .addEmbeds(WebhookEmbedBuilder.fromJDA(embed.build()).build())
                    .addComponents(
                        ActionRow.of(
                            Button.primary("cmd::giveaway:enter", "Entrar al sorteo", true)
                                .withEmoji(PartialEmoji.of("tadaa", "1037465732159656117", true)),
                            Button.link("${Env.API_URL}/sorteo/${giveaway.messageId}", "Ver sorteo")
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
                        Button.link("${Env.API_URL}/sorteo/${giveaway.messageId}", "Ver sorteo")
                    )
                )

            builder.send(channelMessage.build()).whenComplete { _, _ ->

                val embed = EmbedBuilder()
                    .setColor(0x2f3136)
                    .setFooter("Acabó el")
                    .setTimestamp(Date(giveaway.startedAt + giveaway.endAfter).toInstant())

                when(giveaway.style) {
                    "minimum" -> {
                        embed.setAuthor(
                            "Sorteo finalizado!",
                            null,
                            "https://cdn.discordapp.com/attachments/755000173922615336/1037465851122696293/emoji..gif"
                        )
                            .setTitle("Premio: ${giveaway.prize}")
                            .addField("Alojado por", "<@!${giveaway.hostId}>", true)
                            .addField(
                                if (winners.size > 1) "Ganadores" else "Ganador",
                                winners.joinToString { "<@!$it> " },
                                true
                            )
                    }
                    "ibai" -> {
                        embed.setTitle(giveaway.prize)
                            .addField("Alojado por", "<@!${giveaway.hostId}>", true)
                            .addField(
                                if (winners.size > 1) "Ganadores" else "Ganador",
                                winners.joinToString { "<@!$it> " },
                                true
                            )
                            .setImage("https://cdn.discordapp.com/attachments/859486644578025472/1046406289498058762/unknown.png")
                    }
                    else -> {
                        embed.setAuthor(
                            "Sorteo finalizado!",
                            null,
                            "https://cdn.discordapp.com/attachments/755000173922615336/1037465851122696293/emoji..gif"
                        )
                            .setTitle("Premio: ${giveaway.prize}")
                            .setDescription(
                                "\n⭐  Alojado por: <@!${giveaway.hostId}>\n\n\uD83C\uDF89 ${if (winners.size > 1) "Ganadores" else "Ganador"}: ${winners.joinToString { "<@!$it> " }}"
                            )
                            .setThumbnail(guild.iconUrl ?: guild.jda.selfUser.avatarUrl)
                    }
                }

                val editMessage = WebhookMessageBuilder()
                    .reset()
                    .addEmbeds(
                        WebhookEmbedBuilder.fromJDA(embed.build()).build()
                    ).addComponents(
                        ActionRow.of(
                            Button.primary("cmd::giveaway:enter", "Entrar al sorteo", true)
                                .withEmoji(PartialEmoji.of("tadaa", "1037465732159656117", true)),
                            Button.link("${Env.API_URL}/sorteo/${giveaway.messageId}", "Ver sorteo")

                        )
                    )

                winners.forEach { winner ->
                    try {
                        guild.retrieveMemberById(winner).queue { member ->
                            member.user.openPrivateChannel().queue { channel ->
                                channel.sendMessageEmbeds(
                                    EmbedBuilder()
                                        .setAuthor(
                                            "¡Enhorabuena!",
                                            null,
                                            "https://cdn.discordapp.com/attachments/755000173922615336/1037465851122696293/emoji..gif"
                                        )
                                        .setColor(0x2f3136)
                                        .setThumbnail(guild.iconUrl ?: guild.jda.selfUser.avatarUrl)
                                        .setTitle("Has ganado: ${giveaway.prize}")
                                        .setDescription("Has ganado el sorteo de **${giveaway.prize}** en **${guild.name}**!!")
                                        .build()
                                ).queue()
                            }
                        }
                    } catch (_: Exception) {
                        WarningsManager.createWarning(guild, "No se ha podido enviar un mensaje privado al ganador del sorteo con ID ${giveaway.messageId}", Severity.VERY_LOW)
                    }
                }

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
                    Button.link("${Env.API_URL}/sorteo/${giveaway.messageId}", "Ver sorteo")
                )
            )

        builder.send(channelMessage.build()).whenComplete { _, _ ->

            val embed = EmbedBuilder()
                .setColor(0x2f3136)
                .setFooter("Acabó el")
                .setTimestamp(Date(giveaway.startedAt + giveaway.endAfter).toInstant())

            when(giveaway.style) {
                "minimum" -> {
                    embed.setAuthor(
                        "Sorteo finalizado!",
                        null,
                        "https://cdn.discordapp.com/attachments/755000173922615336/1037465851122696293/emoji..gif"
                    )
                        .setTitle("Premio: ${giveaway.prize}")
                        .addField("Alojado por", "<@!${giveaway.hostId}>", true)
                        .addField(
                            if (winners.size > 1) "Ganadores" else "Ganador",
                            winners.joinToString { "<@!$it> " },
                            true
                        )
                }
                "ibai" -> {
                    embed.setTitle(giveaway.prize)
                        .addField("Alojado por", "<@!${giveaway.hostId}>", true)
                        .addField(
                            if (winners.size > 1) "Ganadores" else "Ganador",
                            winners.joinToString { "<@!$it> " },
                            true
                        )
                        .setImage("https://cdn.discordapp.com/attachments/859486644578025472/1046406289498058762/unknown.png")
                }
                else -> {
                    embed.setAuthor(
                        "Sorteo finalizado!",
                        null,
                        "https://cdn.discordapp.com/attachments/755000173922615336/1037465851122696293/emoji..gif"
                    )
                        .setTitle("Premio: ${giveaway.prize}")
                        .setDescription(
                            "\n⭐  Alojado por: <@!${giveaway.hostId}>\n\n\uD83C\uDF89 ${if(winners.size > 1) "Ganadores" else "Ganador"}: ${winners.joinToString { "<@!$it> " }}"
                        )
                        .setThumbnail(guild.iconUrl ?: guild.jda.selfUser.avatarUrl)
                }
            }

            val editMessage = WebhookMessageBuilder()
                .reset()
                .addEmbeds(
                    WebhookEmbedBuilder.fromJDA(embed.build()).build()
                ).addComponents(
                    ActionRow.of(
                        Button.primary("cmd::giveaway:enter", "Entrar al sorteo", true).withEmoji(PartialEmoji.of("tadaa", "1037465732159656117", true)),
                        Button.link("${Env.API_URL}/sorteo/${giveaway.messageId}", "Ver sorteo")
                    )
                )

            winners.forEach { winner ->
                try {
                    guild.retrieveMemberById(winner).queue { member ->
                        member.user.openPrivateChannel().queue { channel ->
                            channel.sendMessageEmbeds(
                                EmbedBuilder()
                                    .setAuthor(
                                        "¡Enhorabuena!",
                                        null,
                                        "https://cdn.discordapp.com/attachments/755000173922615336/1037465851122696293/emoji..gif"
                                    )
                                    .setColor(0x2f3136)
                                    .setThumbnail(guild.iconUrl ?: guild.jda.selfUser.avatarUrl)
                                    .setTitle("Has ganado: ${giveaway.prize}")
                                    .setDescription("Has ganado el sorteo de **${giveaway.prize}** en **${guild.name}**!!")
                                    .build()
                            ).queue()
                        }
                    }
                } catch (_: Exception) {
                    WarningsManager.createWarning(guild, "No se ha podido enviar un mensaje privado al ganador del sorteo con ID ${giveaway.messageId}", Severity.VERY_LOW)
                }
            }

            builder.edit(giveaway.messageId, editMessage.build()).whenComplete { _, _ ->
                builder.close()
                hook.editOriginal("${Emojis.success}  Se ha rehecho el sorteo correctamente").queue()
            }
        }
    }
}