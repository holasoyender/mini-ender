package commands.message.config

import database.schema.Guild
import interfaces.Command
import interfaces.CommandResponse
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.utils.FileUpload
import utils.Emojis
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

class Exportar: Command {
    override fun execute(event: MessageReceivedEvent, args: List<String>, config: Guild): CommandResponse {

        if(config.id.isNotBlank() || !config.exists()) {

            val content = config.raw
            val inputStream = content.byteInputStream()
            val file = FileUpload.fromData(inputStream, "${event.guild.id}.yaml")
            event.message.reply("${Emojis.success}  Aquí tienes el archivo de configuración de este servidor")
                .setFiles(file)
                .queue()

            return CommandResponse.success()
        } else {

            val file = javaClass
                .classLoader
                .getResourceAsStream("config/default.yml") ?: return CommandResponse.error("No he podido encontrar el archivo de configuración por defecto")

            val content = fileContent(file)
                .replace("{guildId}", event.guild.id)
                .replace("{guildName}", event.guild.name)
                .replace("{everyoneRoleId}", event.guild.publicRole.id)

            event.message.reply("${Emojis.success}  Aquí tienes un nuevo archivo de configuración para este servidor")
                .setFiles(FileUpload.fromData(content.byteInputStream(), "${event.guild.id}.yaml"))
                .queue()

            return CommandResponse.success()
        }

    }

    @Throws(IOException::class)
    private fun fileContent(`is`: InputStream): String {
        var lines = ""
        InputStreamReader(`is`).use { isr ->
            BufferedReader(isr).use { br ->
                var line: String?
                while (br.readLine().also { line = it } != null) {
                    lines += line + "\n"
                }
                `is`.close()
            }
        }
        return lines
    }

    override val name: String
        get() = "exportar"
    override val description: String
        get() = "Exportar la configuración del servidor"
    override val aliases: List<String>
        get() = listOf("export", "configexport", "configexportar")
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
        get() = listOf(Permission.MANAGE_SERVER)
    override val permissionLevel: Int
        get() = 4
    override val botPermissions: List<Permission>
        get() = listOf()
}