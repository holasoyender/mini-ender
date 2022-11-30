package commands.message.config

import interfaces.Command
import interfaces.CommandResponse
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.components.buttons.Button

class Reiniciar: Command {
    override fun execute(event: MessageReceivedEvent, args: List<String>): CommandResponse {

        event.message.reply("¿Estás seguro de que quieres reiniciar la configuración de este servidor?").setActionRow(
            Button.danger("cmd::reiniciar:${event.author.id}", "Reiniciar configuración"),
            Button.secondary("cmd::cancel:${event.author.id}", "Cancelar")
        ).queue()

        return CommandResponse.success()
    }

    override val name: String
        get() = "reiniciar"
    override val description: String
        get() = "Reiniciar la configuración del servidor"
    override val aliases: List<String>
        get() = listOf("reset", "reset-config", "reset-config-server", "reset-server-config", "reset-server", "iniciar-config", "iniciar-config-server", "iniciar-server-config", "iniciar-server")
    override val usage: String
        get() = ""
    override val category: String
        get() = "Configuración"
    override val enabled: Boolean
        get() = true
    override val ownerOnly: Boolean
        get() = false
    override val guildOnly: Boolean
        get() = true
    override val global: Boolean
        get() = false
    override val permissions: List<Permission>
        get() = listOf(Permission.ADMINISTRATOR)
    override val botPermissions: List<Permission>
        get() = listOf()
}