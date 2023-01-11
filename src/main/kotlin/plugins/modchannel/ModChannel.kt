package plugins.modchannel

import database.schema.Guild
import enums.Severity
import net.dv8tion.jda.api.entities.Icon
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.Webhook
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import plugins.warnings.WarningsManager
import utils.Time
import webhook.external.JDAWebhookClient
import webhook.send.WebhookMessageBuilder
import webhook.send.component.ActionRow
import webhook.send.component.Button
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.URL
import javax.imageio.ImageIO

object ModChannel {

    fun isModChannel(message: Message, config: Guild): Boolean = config.moderationChannelId == message.channel.id

    fun parse(message: Message): ParsedMessage {

        val content = message.contentRaw
        val args = content.split(" ")
        val users = mutableListOf<String>()
        val attachments = mutableListOf<Message.Attachment>()
        var reason = "Sin razón"
        var duration: String? = null

        for (c in args) {

            if (isId(c)) {
                //me duele la cabeza, nada que ver con el código es solo para que lo sepas
                if (!users.contains(c)) users.add(c)
                continue
            }

            if (Time.ms(c) > 0 && duration == null) {
                duration = c
                continue
            }

            reason += " $c"

        }

        if (reason != "Sin razón") reason = reason.replaceFirst("Sin razón", "").trim()

        if (message.attachments.isNotEmpty())
            attachments.addAll(message.attachments)

        return ParsedMessage(users, attachments, reason, duration ?: "0")
    }

    private fun isId(input: String): Boolean =
        input.matches(Regex("[0-9]{18}")) || input.matches(Regex("[0-9]{17}")) || input.matches(Regex("[0-9]{19}"))

    fun doAction(parsedMessage: ParsedMessage, message: Message) {

        if (message.channel is TextChannel)
            (message.channel as TextChannel).retrieveWebhooks().queue {

                val webhook = it.firstOrNull { w -> w.name == "mini-ender" }

                if (webhook == null) {
                    try {
                        val img = ImageIO.read(message.jda.selfUser.avatarUrl?.let { avatar -> URL(avatar) })
                        val os = ByteArrayOutputStream()

                        ImageIO.write(img, "png", os)

                        val inputStream: InputStream = ByteArrayInputStream(os.toByteArray())

                        (message.channel as TextChannel).createWebhook("mini-ender").setAvatar(Icon.from(inputStream))
                            .queue ({ wh -> send(wh, parsedMessage, message) }, {})
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                        WarningsManager.createWarning(
                            message.guild,
                            "No se ha podido crear un webhook para el canal de moderación",
                            Severity.HIGH
                        )
                        message.addReaction(Emoji.fromUnicode("❌")).queue({}, {})
                    }
                } else {
                    send(webhook, parsedMessage, message)
                }
            }
    }

    @Suppress("DEPRECATION")
    private fun send(webhook: Webhook, parsedMessage: ParsedMessage, message: Message) {

        val client = JDAWebhookClient.from(webhook)

        val builder = WebhookMessageBuilder()

        builder.setUsername(message.author.asTag)
        builder.setAvatarUrl(message.author.avatarUrl ?: message.author.defaultAvatarUrl)
        builder.setContent(message.contentRaw)

        builder.setComponents(
            ActionRow.of(
                Button.danger("cmd::mod:ban", "Banear" + if(parsedMessage.duration != "0") " (${parsedMessage.duration})" else " (Permanente)"),
                Button.danger("cmd::mod:mute", "Mutear" + if(parsedMessage.duration != "0") " (${parsedMessage.duration})" else " (Permanente)"),
                Button.secondary("cmd::empty", "${parsedMessage.users.size} Usuarios", true)
            )
        )

        for (attachment in parsedMessage.attachments) {
            builder.addFile(attachment.fileName, attachment.retrieveInputStream().get())
        }

        client.send(builder.build())
        message.delete().queue({}, {})
        client.close()
    }

}