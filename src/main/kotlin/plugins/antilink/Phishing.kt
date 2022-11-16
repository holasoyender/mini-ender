package plugins.antilink

import enums.Severity
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import org.json.JSONObject
import plugins.warnings.WarningsManager
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

object Phishing {

    fun isPhishing(message: Message): Boolean {
        return try {
            val file = javaClass
                .classLoader
                .getResourceAsStream("phishing.json") ?: return false

            val obj = JSONObject(fileContent(file))
            val domains = obj.getJSONArray("domains")

            val content = message.contentRaw
            val checker = Checker(content)

            if (checker.isLink)
                domains.toList().contains(checker.domain)
            else
                false
        } catch (e: IOException) {
            false
        }
    }

    fun checkPhishing(message: Message) {
        if (isPhishing(message)) {

            val content = if(message.contentRaw.length > 1024) message.contentRaw.substring(0, 1020) + "..." else message.contentRaw
            val guild = message.guild
            val channelId = message.channel.id
            val checker = Checker(content)

            val actionTaken = ActionRouter.ban(message.author, guild, checker)

            var deletedMessage = true

            try {
                message.delete().queue()
            } catch (_: Exception) {
                deletedMessage = false
                WarningsManager.createWarning(
                    guild,
                    "El bot no tiene permisos para eliminar mensajes en el canal $channelId",
                    Severity.HIGH
                )
            }

            val logEmbed = EmbedBuilder()
                .setAuthor("Link de phishing detectado", null, message.author.effectiveAvatarUrl)
                .setDescription("He impedido que el usuario ${message.author.asMention} enviara el siguiente mensaje:```$content```\nYa que el dominio `${checker.domain}` está en la lista de links de phishing.")
                .addField(
                    "Acción tomada",
                    "`${if (!actionTaken) "No se ha podido ejecutar la acción" else "Ban permanente"}`",
                    true
                )
                .addField(
                    "Mensaje eliminado",
                    "`${if (!deletedMessage) "No se ha eliminado el mensaje" else "Se ha borrado el mensaje"}`",
                    true
                )
                .addField("Canal", "<#$channelId>", true)
                .addField("Usuario", "${message.author.asMention} (${message.author.id})", true)
                .setThumbnail("https://cdn.discordapp.com/attachments/839400943517827092/1038135823650013255/sentinel.png")
                .setColor(0x2f3136)

            val config = database.schema.Guild.get(guild.id) ?: return
            val channel =
                if (config.logChannelId.isNotBlank()) {
                    guild.getTextChannelById(config.logChannelId)
                } else {
                    WarningsManager.createWarning(
                        guild,
                        "El canal de logs no está configurado y es requerido para el sistema de anti-link",
                        Severity.VERY_HIGH
                    )
                    return
                }

            if (channel == null) {
                WarningsManager.createWarning(
                    guild,
                    "El canal de logs no existe o no se ha podido encontrar y es requerido para el sistema de anti-link",
                    Severity.VERY_HIGH
                )
                return
            }

            channel.sendMessageEmbeds(logEmbed.build()).queue()
        }
    }

    @Throws(IOException::class)
    private fun fileContent(`is`: InputStream): String {
        var lines = ""
        InputStreamReader(`is`).use { isr ->
            BufferedReader(isr).use { br ->
                var line: String?
                while (br.readLine().also { line = it } != null) {
                    lines += line
                }
                `is`.close()
            }
        }
        return lines
    }
}