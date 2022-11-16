package commands.message.info

import interfaces.Command
import interfaces.CommandResponse
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.utils.FileUpload
import java.awt.image.BufferedImage
import java.io.File
import java.net.URL
import java.nio.file.Files
import javax.imageio.ImageIO

class Avatar: Command {
    override fun execute(event: MessageReceivedEvent, args: List<String>): CommandResponse {
        val user = try {
            event.message.mentions.users.firstOrNull() ?: args.getOrNull(1)?.let { event.jda.retrieveUserById(it).complete() } ?: event.author
        } catch (e: Exception) {
            return CommandResponse.error("No se ha podido encontrar al usuario con ID ${args[1]}")
        }

        val avatar = (user.avatarUrl ?: user.defaultAvatarUrl) + "?size=512"

        return try {
            val url = URL(avatar)
            val img: BufferedImage = ImageIO.read(url)
            val random = (0..100000).random()
            val file = File("temp-${random}.png")
            ImageIO.write(img, "png", file)
            event.message.replyFiles(FileUpload.fromData(file)).queue {
                Files.delete(file.toPath())
            }
            CommandResponse.success()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            CommandResponse.error("No se ha podido encontrar el avatar del usuario")
        }

    }

    override val name: String
        get() = "avatar"
    override val description: String
        get() = "Muestra el avatar de un usuario"
    override val aliases: List<String>
        get() = listOf("av")
    override val usage: String
        get() = "[usuario]"
    override val category: String
        get() = "Información"
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
    override val botPermissions: List<Permission>
        get() = listOf()
}