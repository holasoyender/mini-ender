package messages

import database.schema.Infraction
import enums.InfractionType
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.Channel
import net.dv8tion.jda.api.utils.TimeFormat
import plugins.antilink.Checker

object Formatter {

    fun formatSanctionMessage(message: String, infraction: Infraction, guild: Guild): String {

        val sanction = when (infraction.type) {
            InfractionType.WARN -> "avisado"
            InfractionType.MUTE -> "silenciado"
            InfractionType.TEMP_MUTE -> "silenciado temporalmente hasta el ${
                TimeFormat.DEFAULT.format(
                    infraction.duration + System.currentTimeMillis()
                )
            } (${TimeFormat.RELATIVE.format(infraction.duration + System.currentTimeMillis())})"
            InfractionType.BAN -> "baneado"
            InfractionType.TEMP_BAN -> "baneado temporalmente hasta el ${
                TimeFormat.DEFAULT.format(
                    infraction.duration + System.currentTimeMillis()
                )
            } (${TimeFormat.RELATIVE.format(infraction.duration + System.currentTimeMillis())})"
            InfractionType.KICK -> "expulsado"
        }

        return message
            .replace("{user}", infraction.userName)
            .replace("{userid}", infraction.userId)
            .replace("{server}", guild.name)
            .replace("{reason}", infraction.reason)
            .replace("{sanction}", sanction)

    }

    fun formatLinksMessage(message: String, channel: Channel, user: User, guild: Guild, sentMessage: String, link: Checker): String {

        return message
            .replace("{user}", user.asTag)
            .replace("{userid}", user.id)
            .replace("{channel}", channel.asMention)
            .replace("{server}", guild.name)
            .replace("{message}", sentMessage)
            .replace("{domain}", link.domain)

    }

    fun formatLinksSanctionMessage(message: String, sanction: String, guild: Guild, user: User, domain: String): String {

        return message
            .replace("{user}", user.asTag)
            .replace("{sanction}", sanction)
            .replace("{userid}", user.id)
            .replace("{server}", guild.name)
            .replace("{domain}", domain)
    }
}