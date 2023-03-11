package logger

import database.Redis
import database.schema.Message
import enums.Severity
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.guild.GuildBanEvent
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent
import net.dv8tion.jda.api.events.message.MessageDeleteEvent
import net.dv8tion.jda.api.events.message.MessageUpdateEvent
import net.dv8tion.jda.api.utils.TimeFormat
import plugins.warnings.WarningsManager
import java.awt.Color
import java.util.*

class EventLogger(
    guild: Guild,
    config: database.schema.Guild,
) {

    private val guild: Guild
    private val config: database.schema.Guild
    private val channelId: String?
    private val channel: TextChannel?

    init {

        this.guild = guild
        this.config = config

        if (config.logsChannelId.isEmpty() || config.logsChannelId.trim().isEmpty()) {
            channelId = null
            channel = null
        } else {
            channelId = config.logsChannelId
            channel = try {
                guild.getTextChannelById(channelId)
            } catch (e: Exception) {
                null
            }
        }
    }

    fun log(embed: EmbedBuilder) {
        if (channelId == null || channel == null) return

        embed
            .setFooter(
                "${guild.jda.selfUser.name} - Logback",
                guild.jda.selfUser.effectiveAvatarUrl
            )
            .setTimestamp(Date().toInstant())

        channel.sendMessageEmbeds(embed.build()).queue({}, {
            WarningsManager.createWarning(
                guild,
                "No se pudo enviar un mensaje de log en el canal $channelId",
                Severity.LOW
            )
        })
    }

    fun log(event: MessageDeleteEvent) {

        if(!Redis.usingRedis) return
        val message = Message.get(event.messageId) ?: return
        val embed = EmbedBuilder()
            .setColor(Color.decode("#FEE75C"))
            .setAuthor("${message.authorTag} - Mensaje eliminado", null, message.authorAvatar)
            .addField("Autor", "<@!${message.authorId}> (`${message.authorId}`)", true)
            .addField("Canal", "<#${message.channelId}> (`${message.channelId}`)", true)
            .addField(
                "Fecha",
                "${TimeFormat.DEFAULT.format(System.currentTimeMillis())} (${TimeFormat.RELATIVE.format(System.currentTimeMillis())})",
                true
            )
            .setDescription("Contenido del mensaje: ```${message.contentDisplay}```")
            .setThumbnail("https://cdn.discordapp.com/attachments/839400943517827092/1045076556093071370/trash.png")

        log(embed)
        message.delete()

    }

    fun log(event: MessageUpdateEvent) {

        if(!Redis.usingRedis) return
        val message = Message.get(event.messageId) ?: return
        val embed = EmbedBuilder()
            .setColor(Color.decode("#FEE75C"))
            .setAuthor("${message.authorTag} - Mensaje editado", null, message.authorAvatar)
            .addField("Autor", "<@!${message.authorId}> (`${message.authorId}`)", true)
            .addField("Canal", "<#${message.channelId}> (`${message.channelId}`)", true)
            .addField(
                "Fecha",
                "${TimeFormat.DEFAULT.format(System.currentTimeMillis())} (${TimeFormat.RELATIVE.format(System.currentTimeMillis())})",
                true
            )
            .addField(
                "Antes",
                "```${
                    if (message.contentDisplay.length > 1024) message.contentDisplay.substring(
                        0,
                        1000
                    ) else message.contentDisplay
                }```",
                false
            )
            .addField(
                "Después",
                "```${
                    if (event.message.contentDisplay.length > 1024) event.message.contentDisplay.substring(
                        0,
                        1000
                    ) else event.message.contentDisplay
                }```",
                false
            )
            .setThumbnail("https://cdn.discordapp.com/attachments/839400943517827092/1045076638532108419/emoji.png")

        log(embed)
        message.content = event.message.contentRaw
        message.contentDisplay = event.message.contentDisplay
        message.save()
    }

    fun log(event: GuildBanEvent) {
        val embed = EmbedBuilder()
            .setColor(Color.decode("#57F287"))
            .setAuthor(event.user.asTag, null, event.user.effectiveAvatarUrl)
            .setDescription("**${event.user.asTag}** (`${event.user.id}`) ha sido baneado permanentemente del servidor")
            .setThumbnail("https://cdn.discordapp.com/attachments/839400943517827092/1045068242051420240/ban.png")

        log(embed)
    }

    fun log(event: GuildUnbanEvent) {
        val embed = EmbedBuilder()
            .setColor(Color.decode("#57F287"))
            .setAuthor(event.user.asTag, null, event.user.effectiveAvatarUrl)
            .setDescription("**${event.user.asTag}** (`${event.user.id}`) ha sido desbaneado del servidor")
            .setThumbnail("https://cdn.discordapp.com/attachments/839400943517827092/1045067980872089692/unban.png")

        log(embed)
    }

    fun log(event: GuildMemberJoinEvent) {
        val embed = EmbedBuilder()
            .setColor(Color.decode("#57F287"))
            .setAuthor(event.user.asTag, null, event.user.effectiveAvatarUrl)
            .setDescription("**${event.user.asTag}** (`${event.user.id}`) se ha unido al servidor ${TimeFormat.RELATIVE.format(event.member.timeJoined)}")
            .addField("Cuenta creada", "${TimeFormat.DEFAULT.format(event.user.timeCreated)} (${TimeFormat.RELATIVE.format(event.user.timeCreated)})", true)
            .addField("Miembro desde", "${TimeFormat.DEFAULT.format(event.member.timeJoined)} (${TimeFormat.RELATIVE.format(event.member.timeJoined)})", true)
            .setThumbnail("https://cdn.discordapp.com/attachments/839400943517827092/1045066052427591690/up.png")

        log(embed)
    }

    fun log(event: GuildMemberRemoveEvent) {
        val embed = EmbedBuilder()
            .setColor(Color.decode("#ED4245"))
            .setAuthor(event.user.asTag, null, event.user.effectiveAvatarUrl)
            .setDescription("**${event.user.asTag}** (`${event.user.id}`) ha abandonado servidor ${TimeFormat.RELATIVE.format(System.currentTimeMillis())}")
            .setThumbnail("https://cdn.discordapp.com/attachments/839400943517827092/1045065820256096417/down.png")

        log(embed)
    }

}