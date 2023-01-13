package commands.message.ocio

import database.schema.Guild
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

class Perro: Command {
    override fun execute(event: MessageReceivedEvent, args: List<String>, config: Guild): CommandResponse {
        try {

            val url = URL("https://dog.ceo/api/breeds/image/random")
            val text = HttpManager.request(url)

            val parse = JSONObject(text)

            val image = parse.getString("message")
            val format = image.substring(image.lastIndexOf(".") + 1)

            val imageURL = URL(image)
            val img: BufferedImage = ImageIO.read(imageURL)

            val os = ByteArrayOutputStream()

            ImageIO.write(img, format, os)

            val inputStream: InputStream = ByteArrayInputStream(os.toByteArray())
            event.message.replyFiles(FileUpload.fromData(inputStream, "perro.$format")).queue()
            return CommandResponse.success()

        } catch (e: Exception) {
            return CommandResponse.error("No he podido encontrar una imagen valida D:")
        }
    }

    override val name: String
        get() = "perro"
    override val description: String
        get() = "Muestra una foto de un perro aleatorio"
    override val aliases: List<String>
        get() = listOf("dog", "perrete")
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