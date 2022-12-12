package commands.message.bot

import interfaces.Command
import interfaces.CommandResponse
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import utils.Emojis
import java.lang.management.ManagementFactory

class Uptime: Command {
    override fun execute(event: MessageReceivedEvent, args: List<String>): CommandResponse {

        val duration: Long = ManagementFactory.getRuntimeMXBean().uptime

        val years = duration / 31104000000L
        val months = duration / 2592000000L % 12
        val days = duration / 86400000L % 30
        val hours = duration / 3600000L % 24
        val minutes = duration / 60000L % 60
        val seconds = duration / 1000L % 60

        val uptime =
            ((if (years == 0L) "" else "**$years** años, ") + (if (months == 0L) "" else "**$months** meses, ") + (if (days == 0L) "" else "**$days** días, ") + (if (hours == 0L) "" else "**$hours** horas, ")
                    + (if (minutes == 0L) "" else "**$minutes** minutos y ") + if (seconds == 0L) "" else "**$seconds** segundos! ")

        event.message.reply("${Emojis.time}  Llevo online $uptime").queue()
        return CommandResponse.success()
    }

    override val name: String
        get() = "uptime"
    override val description: String
        get() = "Muestra el tiempo que lleva encendido el bot"
    override val aliases: List<String>
        get() = listOf("up")
    override val usage: String
        get() = ""
    override val category: String
        get() = "Bot"
    override val enabled: Boolean
        get() = true
    override val ownerOnly: Boolean
        get() = false
    override val guildOnly: Boolean
        get() = false
    override val global: Boolean
        get() = false
    override val permissions: List<Permission>
        get() = listOf()
    override val permissionLevel: Int
        get() = 1
    override val botPermissions: List<Permission>
        get() = listOf()
}