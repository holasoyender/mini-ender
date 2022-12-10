package plugins.giveaway

import net.dv8tion.jda.api.entities.Guild
import java.awt.image.BufferedImage
import java.net.URL
import javax.imageio.ImageIO

class Banner(guild: Guild) {

    val image: BufferedImage
    private val baseBanner: BufferedImage
    var processedBanner: BufferedImage
    private var isProcessed = false

    init {
        image =  ImageIO.read(URL(guild.iconUrl ?: guild.jda.selfUser.avatarUrl ?: "https://cdn.discordapp.com/embed/avatars/0.png")) ?: throw Exception("No se ha podido cargar la imagen para el banner")
        baseBanner = ImageIO.read(javaClass.classLoader.getResource("./img/sorteo.png") ?: throw Exception("No se ha podido leer el banner de sorteos")) ?: throw Exception("No se ha podido leer el banner de sorteos")
        processedBanner = baseBanner
    }

    fun getBanner(): BufferedImage {
        if (isProcessed) return processedBanner

        val g = baseBanner.createGraphics()

        g.clip = java.awt.geom.Ellipse2D.Double(40.0, 30.0, 140.0, 140.0)
        g.drawImage(image, 40, 30, 140, 140, null)
        g.dispose()

        isProcessed = true
        processedBanner = baseBanner
        return processedBanner
    }

}