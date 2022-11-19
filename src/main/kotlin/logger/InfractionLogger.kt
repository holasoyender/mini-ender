package logger

import database.schema.Infraction
import enums.InfractionType
import enums.Severity
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.utils.TimeFormat
import plugins.warnings.WarningsManager
import java.awt.Color
import java.util.Date

class InfractionLogger(
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

        if (config.logChannelId.isEmpty() || config.logChannelId.trim().isEmpty()) {
            channelId = null
            channel = null
        } else {
            channelId = config.logChannelId
            channel = try {
                guild.getTextChannelById(channelId)
            } catch (e: Exception) {
                null
            }
        }
    }

    fun log(infraction: Infraction) {

        if (channelId == null || channel == null) return

        val infractionName = infraction.type.name.lowercase().replaceFirstChar { it.uppercase() }

        val embed = EmbedBuilder()
            .setColor(Color.decode("#FEE75C"))
            .setAuthor(infraction.userName + " - " + infractionName, null, guild.iconUrl ?: "")
            .setFooter(
                "${guild.jda.selfUser.name} - Sentinel",
                "https://cdn.discordapp.com/attachments/839400943517827092/1038135823650013255/sentinel.png"
            )
            .setThumbnail("https://cdn.discordapp.com/attachments/839400943517827092/1038135823650013255/sentinel.png")
            .setTimestamp(Date().toInstant())
            .addField("Usuario", "${infraction.userName} (${infraction.userId})", true)
            .addField("Moderador", "<@!${infraction.moderatorId}> (${infraction.moderatorId})", true)
            .addField("Tipo", "`$infractionName`", true)
            .setDescription("El usuario ha sido sancionado por la razón ```${infraction.reason}```")

        when (infraction.type) {
            InfractionType.TEMP_MUTE,
            InfractionType.TEMP_BAN -> {
                embed.addField(
                    "Duración",
                    "Hasta ${TimeFormat.DEFAULT.format(System.currentTimeMillis() + infraction.duration)} (${
                        TimeFormat.RELATIVE.format(System.currentTimeMillis() + infraction.duration)
                    })",
                    true
                )
            }

            else -> {}
        }

        channel.sendMessageEmbeds(embed.build()).queue({}, {
            WarningsManager.createWarning(
                guild,
                "No se pudo enviar un mensaje de log en el canal $channelId",
                Severity.LOW
            )
        })

    }
}