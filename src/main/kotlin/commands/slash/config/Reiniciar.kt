package commands.slash.config

import interfaces.CommandResponse
import interfaces.SlashCommand
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.interactions.components.buttons.Button

class Reiniciar: SlashCommand {
    override fun execute(event: SlashCommandInteractionEvent): CommandResponse {

        event.reply("¿Estás seguro de que quieres reiniciar la configuración de este servidor?").setActionRow(
            Button.danger("cmd::reiniciar:${event.user.id}", "Reiniciar configuración"),
            Button.secondary("cmd::cancel:${event.user.id}", "Cancelar")
        ).queue()

        return CommandResponse.success()

    }

    override val name: String
        get() = "reiniciar"
    override val description: String
        get() = "Reiniciar la configuración del servidor"
    override val category: String
        get() = "Configuración"
    override val enabled: Boolean
        get() = true
    override val ownerOnly: Boolean
        get() = false
    override val guildOnly: Boolean
        get() = true
    override val permissions: List<Permission>
        get() = listOf(Permission.ADMINISTRATOR)
    override val botPermissions: List<Permission>
        get() = listOf()
    override val metadata: SlashCommandData?
        get() = null
}