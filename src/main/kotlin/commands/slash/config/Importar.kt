package commands.slash.config

import config.manager.GuildConfigImporter
import interfaces.CommandResponse
import interfaces.SlashCommand
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

class Importar: SlashCommand {
    override fun execute(event: SlashCommandInteractionEvent): CommandResponse {

        val file = event.getOption("archivo")?.asAttachment ?: return CommandResponse.error("No se ha adjuntado ningún archivo")
        return GuildConfigImporter.import(file, event.guild!!)

    }

    override val name: String
        get() = "importar"
    override val description: String
        get() = "Importar un archivo de configuración"
    override val category: String
        get() = "Configuración"
    override val enabled: Boolean
        get() = true
    override val ownerOnly: Boolean
        get() = false
    override val guildOnly: Boolean
        get() = true
    override val permissions: List<Permission>
        get() = listOf(Permission.MANAGE_SERVER)
    override val botPermissions: List<Permission>
        get() = listOf()
    override val metadata: SlashCommandData
        get() = Commands.slash(this.name, this.description).addOptions(
            OptionData(OptionType.ATTACHMENT, "archivo", "Archivo de configuración", true)
        )
}