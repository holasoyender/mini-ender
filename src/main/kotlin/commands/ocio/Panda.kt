package commands.ocio

import http.HttpManager
import interfaces.Command
import interfaces.CommandResponse
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.utils.FileUpload
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.awt.image.BufferedImage
import java.io.File
import java.net.URL
import java.nio.file.Files
import javax.imageio.ImageIO

class Panda: Command {
    override fun execute(event: MessageReceivedEvent, args: List<String>): CommandResponse {
        try {

            val urls = listOf("https://some-random-api.ml/img/panda", "https://some-random-api.ml/img/red_panda")

            val url = URL(urls.random())
            val text = HttpManager.request(url)

            val parse = JSONParser()
            val data: JSONObject = parse.parse(text) as JSONObject

            val image = data["link"] as String

            val imageURL = URL(image)
            val img: BufferedImage = ImageIO.read(imageURL)
            val random = (0..100000).random()
            val file = File("temp-${random}.${imageURL.file.substring(imageURL.file.lastIndexOf(".") + 1)}")
            ImageIO.write(img, imageURL.file.substring(imageURL.file.lastIndexOf(".") + 1), file)
            event.message.replyFiles(FileUpload.fromData(file)).queue {
                Files.delete(file.toPath())
            }
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
    override val permissions: List<Permission>
        get() = listOf()
    override val botPermissions: List<Permission>
        get() = listOf(Permission.MESSAGE_ATTACH_FILES)
}