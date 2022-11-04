package commands.message.bot

import interfaces.Command
import interfaces.CommandResponse
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import utils.Emojis

class Error: Command {
    override fun execute(event: MessageReceivedEvent, args: List<String>): CommandResponse {

        event.message.reply("${Emojis.success}  Haz click en el botón para reportar un error").setActionRow(
            Button.primary("cmd::error:${event.author.id}", "Reportar error")
        ).queue()

        return CommandResponse.success()
        
    }

    override val name: String
        get() = "error"
    override val description: String
        get() = "Reportar un error de algún bot"
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
        get() = true
    override val global: Boolean
        get() = false
    override val permissions: List<Permission>
        get() = listOf()
    override val botPermissions: List<Permission>
        get() = listOf()
}