package plugins.regalos

import net.dv8tion.jda.api.utils.TimeFormat

object Gifts {

    private val gifs: List<Gift> = listOf(
        Gift(
            name = "No te ha tocado nada :(",
            description = "Puedes intentarlo de nuevo ${TimeFormat.RELATIVE.format(System.currentTimeMillis() + 86400000)}",
            image = "https://media.tenor.com/OasM7f4Pe6UAAAAC/serious-ibai.gif",
            rarity = 0,
            chance = 10
        ),
        Gift(
            name = "¡ Un rol exclusivo del servidor !",
            description = "Has ganado un rol exclusivo del servidor, ¡Enhorabuena!\nPuedes reclamarlo mandándole un mensaje a <@!835642946962718731>",
            image = "https://media.tenor.com/JITzgeo8qjYAAAAC/ibai-bailando.gif",
            rarity = 0,
            chance = 1
        ),
        Gift(
            name = "Un corazon de ibai :heart:",
            description = "jajdsadjajdjsa pero que cojones esta haciendo con las manos",
            image = "https://media.tenor.com/_W41NMLGC5AAAAAC/ibai.gif",
            rarity = 0,
            chance = 5
        )
    )

    fun getGift(): Gift {
        val random = (0..100).random()
        var chance = 0
        for (gift in gifs) {
            chance += gift.chance
            if (random <= chance) {
                return gift
            }
        }
        return gifs[0]
    }

    fun giftsSize(): Int {
        return gifs.size
    }

}