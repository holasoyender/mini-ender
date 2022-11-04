package plugins.warnings

import database.schema.Warnings
import enums.Severity
import net.dv8tion.jda.api.entities.Guild

object WarningsManager {


    fun createWarning(guild: Guild, warning: String, severity: Severity): String {

        val warn = Warnings.get(warning, guild.idLong)
        if (warn != null) {

            warn.repeats++
            warn.save()
            return warn.id

        } else {

            val id = (1..100000).random()
            val newWarning = Warnings(
                guildId = guild.id,
                message = warning,
                severity = severity,
                resolved = false,
                id = id.toString(),
                repeats = 1
            )
            newWarning.save()

            return id.toString()

        }
    }


}