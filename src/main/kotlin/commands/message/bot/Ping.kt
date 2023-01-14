package commands.message.bot

import database.schema.Guild
import interfaces.Command
import interfaces.CommandResponse
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import utils.Emojis
import utils.Emojis.f

class Ping: Command {
    override fun execute(event: MessageReceivedEvent, args: List<String>, config: Guild): CommandResponse {
        event.message.reply("${f(Emojis.ping)}  Mi ping es de `${event.jda.gatewayPing}ms`!").queue()

        return CommandResponse.success()
    }

    override val name: String
        get() = "ping"
    override val description: String
        get() = "Muestra el ping del bot"
    override val aliases: List<String>
        get() = listOf("pong")
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
        get() = true
    override val permissions: List<Permission>
        get() = listOf()
    override val permissionLevel: Int
        get() = 1
    override val botPermissions: List<Permission>
        get() = listOf()
}