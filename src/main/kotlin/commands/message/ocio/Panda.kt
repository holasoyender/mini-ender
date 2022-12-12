package commands.message.ocio

import http.HttpManager
import interfaces.Command
import interfaces.CommandResponse
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.utils.FileUpload
import org.json.JSONObject
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.URL
import javax.imageio.ImageIO

class Panda: Command {
    override fun execute(event: MessageReceivedEvent, args: List<String>): CommandResponse {
        try {

            val urls = listOf("https://some-random-api.ml/img/panda", "https://some-random-api.ml/img/red_panda")

            val url = URL(urls.random())
            val text = HttpManager.request(url)

            val parse = JSONObject(text)

            val image = parse.getString("link")
            val format = image.substring(image.lastIndexOf(".") + 1)

            val imageURL = URL(image)
            val img: BufferedImage = ImageIO.read(imageURL)

            val os = ByteArrayOutputStream()

            ImageIO.write(img, format, os)

            val inputStream: InputStream = ByteArrayInputStream(os.toByteArray())
            event.message.replyFiles(FileUpload.fromData(inputStream, "panda.$format")).queue()
            return CommandResponse.success()

        } catch (e: Exception) {
            return CommandResponse.error("No he podido encontrar una imagen valida D:")
        }
    }

    override val name: String
        get() = "panda"
    override val description: String
        get() = "Muestra una imagen de un panda"
    override val aliases: List<String>
        get() = listOf()
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