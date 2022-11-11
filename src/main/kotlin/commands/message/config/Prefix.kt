package commands.message.config

import config.DefaultConfig
import database.schema.Guild
import interfaces.Command
import interfaces.CommandResponse
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import utils.Emojis

class Prefix: Command {
    override fun execute(event: MessageReceivedEvent, args: List<String>): CommandResponse {

        val config = Guild.get(event.guild.id) ?: DefaultConfig.get()

        if(args.size == 1) {
            event.message.reply("${Emojis.slash}  El prefijo actual es `${config.prefix}`").queue()
            return CommandResponse.success()
        }

        val newPrefix = args[1]
        if(newPrefix.length > 5)
            return CommandResponse.error("El prefijo no puede tener más de 5 caracteres!")

        config.prefix = newPrefix
        config.save()

        event.message.reply("${Emojis.success}  El prefijo ha sido cambiado a `$newPrefix`").queue()
        return CommandResponse.success()
    }

    override val name: String
        get() = "prefix"
    override val description: String
        get() = "Cambia el prefijo del bot"
    override val aliases: List<String>
        get() = listOf("setprefix")
    override val usage: String
        get() = "<nuevo prefijo>"
    override val category: String
        get() = "Config"
    override val enabled: Boolean
        get() = true
    override val ownerOnly: Boolean
        get() = false
    override val guildOnly: Boolean
        get() = true
    override val global: Boolean
        get() = false
    override val permissions: List<Permission>
        get() = listOf(Permission.MANAGE_SERVER)
    override val botPermissions: List<Permission>
        get() = listOf()
}