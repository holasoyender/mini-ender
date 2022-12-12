package commands.message.ocio

import interfaces.Command
import interfaces.CommandResponse
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import utils.Emojis
import utils.Emojis.f

class Tirar: Command {
    override fun execute(event: MessageReceivedEvent, args: List<String>): CommandResponse {

        val input = args.getOrNull(1) ?: return CommandResponse.error("Debes introducir un número!")
        val number = input.toIntOrNull() ?: return CommandResponse.error("El argumento debe ser un número valido")

        if(number < 0) return CommandResponse.error("El número debe ser positivo!")

        val random = (0..number).random()

        event.message.reply("${f(Emojis.dado)}  Ha salido `$random`!").queue()
        return CommandResponse.success()
    }

    override val name: String
        get() = "tirar"
    override val description: String
        get() = "Elegir un numero al azar entre 0 y el numero que pongas"
    override val aliases: List<String>
        get() = listOf("tirar-dado", "tirar-dados", "tirar-dado-azar", "tirar-dados-azar")
    override val usage: String
        get() = "<numero>"
    override val category: String
        get() = "Ocio"
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