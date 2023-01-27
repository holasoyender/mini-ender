package commands.slash.config

import database.Redis
import database.schema.Guild
import interfaces.CommandResponse
import interfaces.SlashCommand
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.utils.FileUpload
import utils.Emojis
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

class Exportar: SlashCommand {
    override fun execute(event: SlashCommandInteractionEvent): CommandResponse {

        if(Redis.usingRedis)
            Redis.connection!!.del("guilds:${event.guild!!.id}")
        val config = Guild.get(event.guild!!.id, true)

        if(config != null) {

            val content = config.raw
            val inputStream = content.byteInputStream()
            val file = FileUpload.fromData(inputStream, "${event.guild!!.id}.yaml")
            event.reply("${Emojis.success}  Aquí tienes el archivo de configuración de este servidor")
                .setFiles(file)
                .queue()

            return CommandResponse.success()
        } else {

            val file = javaClass
                .classLoader
                .getResourceAsStream("config/default.yml") ?: return CommandResponse.error("No he podido encontrar el archivo de configuración por defecto")

            val content = fileContent(file)
                .replace("{guildId}", event.guild!!.id)
                .replace("{guildName}", event.guild!!.name)
                .replace("{everyoneRoleId}", event.guild!!.publicRole.id)

            event.reply("${Emojis.success}  Aquí tienes un nuevo archivo de configuración para este servidor")
                .setFiles(FileUpload.fromData(content.byteInputStream(), "${event.guild!!.id}.yaml"))
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
        get() = "Exporta la configuración del servidor"
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
    override val permissionLevel: Int
        get() = 4
    override val botPermissions: List<Permission>
        get() = listOf()
    override val metadata: SlashCommandData?
        get() = null
}