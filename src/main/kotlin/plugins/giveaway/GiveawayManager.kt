package plugins.giveaway

import database.schema.Sorteo
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.utils.TimeFormat
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
        interaction: InteractionHook
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
                        interaction.editOriginal("No se ha podido crear el sorteo: \n`El webhook no se ha podido crear`")
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
                        .setDescription(
                            "**Haz click en el botón para entrar al sorteo!**\n<:regalo:829082183460192256>  Premio: **${prize}**\n⚙️  Alojado por: <@!${host.id}>\n⏰  Acaba: ${
                                TimeFormat.DEFAULT.format(
                                    timestamp
                                )
                            }  (${TimeFormat.RELATIVE.format(timestamp)})\n\n🏆  Ganador(es): `${winners}`"
                        )
                        .build()
                )
                    .build()
            )
            .addComponents(
                ActionRow.of(
                    Button.primary("cmd::giveaway:enter:${guild.id}", "Entrar al sorteo").withEmoji(PartialEmoji.of("tadaa", "1037465732159656117", true))
                )
            )

        /*
        * TODO:
        *  - Cambiar el estilo del embed y ponerle un banner grande como dyno
        *  - Que los botones funcionen
        *  - Loop para comprobar si ya ha acabado algún sorteo
        *  - Todos los comandos de sorteo
        *    - Acabar
        *    - Reroll
        *    - Info
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
        }
    }
}