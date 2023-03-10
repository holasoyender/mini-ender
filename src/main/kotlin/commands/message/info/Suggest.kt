package commands.message.info

import database.schema.Guild
import interfaces.Command
import interfaces.CommandResponse
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import utils.Emojis

class Suggest: Command {
    override fun execute(event: MessageReceivedEvent, args: List<String>, config: Guild): CommandResponse {

        if (config.suggestChannel.isBlank())
            return CommandResponse.error("${Emojis.f(Emojis.error)}  No hay un canal de sugerencias configurado en este servidor!")

        val toSuggest: String = args.subList(1, args.size).joinToString(" ")

        if(toSuggest.isNotBlank() && (toSuggest.length > 1000 || toSuggest.length < 10))
            return CommandResponse.error("${Emojis.f(Emojis.error)}  La sugerencia debe de tener entre 10 y 1000 caracteres!")

        if(!database.Redis.usingRedis)
            return CommandResponse.error("${Emojis.f(Emojis.error)}  Este bot no admite sugerencias, si crees que esto es un error, contacta con un administrador del bot")

        event.message.reply("Haz click en el botón de abajo para crear una sugerencia! ${
            if (toSuggest.isNotBlank())
                "\n`$toSuggest`"
            else ""
        }").setActionRow(
            Button.primary("cmd::suggest:${event.author.id}", "${
                if (toSuggest.isNotBlank())
                    "Enviar"
                else
                    "Crear"
            } sugerencia")
        ).queue()

        return CommandResponse.success()
    }

    override val name: String
        get() = "suggest"
    override val description: String
        get() = "Hacer una sugerencia para el servidor."
    override val aliases: List<String>
        get() = listOf("sugerencia", "sugerir", "sugest", "sug")
    override val usage: String
        get() = "suggest <sugerencia>"
    override val category: String
        get() = "Información"
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
    override val permissionLevel: Int
        get() = 1
    override val botPermissions: List<Permission>
        get() = listOf(Permission.CREATE_PUBLIC_THREADS, Permission.CREATE_PRIVATE_THREADS, Permission.MANAGE_THREADS)
}