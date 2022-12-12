package interfaces

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

interface Command {

    fun execute(event: MessageReceivedEvent, args: List<String>): CommandResponse

    val name: String

    val description: String
    val aliases: List<String>
    val usage: String
    val category: String
    val enabled: Boolean
    val ownerOnly: Boolean
    val guildOnly: Boolean
    val global: Boolean
    val permissions: List<Permission>
    val permissionLevel: /*min 0 max 5*/ Int
    val botPermissions: List<Permission>
}