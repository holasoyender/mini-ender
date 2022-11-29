package commands.message.config

import config.manager.GuildConfigImporter
import interfaces.Command
import interfaces.CommandResponse
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import utils.Emojis

class Importar: Command {
    override fun execute(event: MessageReceivedEvent, args: List<String>): CommandResponse {

        val file = event.message.attachments.firstOrNull() ?: return CommandResponse.error("No se ha adjuntado ningún archivo")
        val response = GuildConfigImporter.import(file, event.guild)

        return if (response.exitStatus != 0) response else {
            event.message.reply("${Emojis.success}  La nueva configuración ha sido importada correctamente").queue()
            CommandResponse.success()
        }
    }

    override val name: String
        get() = "importar"
    override val aliases: List<String>
        get() = listOf("import", "setconfig", "config")
    override val usage: String
        get() = "<archivo>"
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
    override val global: Boolean
        get() = false
    override val permissions: List<Permission>
        get() = listOf(Permission.MANAGE_SERVER)
    override val botPermissions: List<Permission>
        get() = listOf()
}