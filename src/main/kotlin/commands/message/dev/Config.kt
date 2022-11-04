package commands.message.dev

import database.schema.Guild
import interfaces.Command
import interfaces.CommandResponse
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import utils.Emojis

class Config: Command {
    override fun execute(event: MessageReceivedEvent, args: List<String>): CommandResponse {


        val config = Guild.get(event.guild.id)
        if(config != null)
            return CommandResponse.error("Este servidor ya tiene una configuración!")

        val newConfig = Guild(event.guild.id, "-", arrayOf(), "", false)
        newConfig.save()

        event.message.reply("${Emojis.success}  Se ha creado una configuración por defecto para este servidor").queue()

        return CommandResponse.success()
    }

    override val name: String
        get() = "config"
    override val description: String
        get() = "Crear una nueva configuración por defecto"
    override val aliases: List<String>
        get() = listOf()
    override val usage: String
        get() = ""
    override val category: String
        get() = "Dev"
    override val enabled: Boolean
        get() = true
    override val ownerOnly: Boolean
        get() = true
    override val guildOnly: Boolean
        get() = true
    override val permissions: List<Permission>
        get() = listOf()
    override val botPermissions: List<Permission>
        get() = listOf()
}