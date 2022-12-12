package commands.message.ocio

import http.HttpManager
import interfaces.Command
import interfaces.CommandResponse
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.utils.FileUpload
import org.json.JSONArray
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.URL
import javax.imageio.ImageIO

class Bird: Command {

    /*
    * No te voy a engañar, he robado este código de un repositorio open source de github,
    * pretendía poner un comentario del tipo "código prestado del proyecto..." pero se
    * me ha olvidado por completo a quien se lo copié
    * Supongo que este es mi código ahora, si el legítimo autor llega a leer esto, lo siento :p
    *  - holasoyender 15/11/2022
    */
    override fun execute(event: MessageReceivedEvent, args: List<String>): CommandResponse {
        try {

            val url = URL("https://shibe.online/api/birds")
            val text = HttpManager.request(url)

            val parse = JSONArray(text)

            val image = parse[0] as String
            val format = image.substring(image.lastIndexOf(".") + 1)

            val imageURL = URL(image)
            val img: BufferedImage = ImageIO.read(imageURL)

            val os = ByteArrayOutputStream()

            ImageIO.write(img, format, os)

            val inputStream: InputStream = ByteArrayInputStream(os.toByteArray())
            event.message.replyFiles(FileUpload.fromData(inputStream, "bird.$format")).queue()
            return CommandResponse.success()

        } catch (e: Exception) {
            println(e)
            return CommandResponse.error("No he podido encontrar una imagen valida D:")
        }
    }

    override val name: String
        get() = "bird"
    override val description: String
        get() = "Muestra una imagen de un pájaro"
    override val aliases: List<String>
        get() = listOf("pájaro", "pajaro")
    override val usage: String
        get() = ""
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
        get() = listOf(Permission.MESSAGE_ATTACH_FILES)
}