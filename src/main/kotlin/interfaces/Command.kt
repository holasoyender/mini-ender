package interfaces

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

interface Command {

    fun execute(event: MessageReceivedEvent, args: List<String>): CommandResponse

    val name: String
        get() = ""
    val description: String
        get() = ""
    val aliases: List<String>
        get() = listOf()
    val usage: String
        get() = ""
    val category: String
        get() = ""
    val enabled: Boolean
        get() = true
    val ownerOnly: Boolean
        get() = false
    val guildOnly: Boolean
        get() = false
    val permissions: List<Permission>
        get() = listOf()
    val botPermissions: List<Permission>
        get() = listOf()
}