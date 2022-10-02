package commands.bot

import interfaces.Command
import interfaces.CommandResponse
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

class Error: Command {
    override fun execute(event: MessageReceivedEvent, args: List<String>): CommandResponse {
        TODO("Not yet implemented")
    }

    override val name: String
        get() = "error"
    override val description: String
        get() = "Muestra un error"
    override val aliases: List<String>
        get() = listOf("report")
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
    override val permissions: List<Permission>
        get() = listOf()
    override val botPermissions: List<Permission>
        get() = listOf()
}