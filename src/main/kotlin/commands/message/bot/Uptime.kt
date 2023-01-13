package commands.message.bot

import database.schema.Guild
import interfaces.Command
import interfaces.CommandResponse
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import utils.Emojis
import java.lang.management.ManagementFactory
import kotlin.time.Duration.Companion.milliseconds

class Uptime: Command {
    override fun execute(event: MessageReceivedEvent, args: List<String>, config: Guild): CommandResponse {

        val duration = ManagementFactory.getRuntimeMXBean().uptime.milliseconds

        event.message.reply("${Emojis.time}  Llevo online **$duration**").queue()
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