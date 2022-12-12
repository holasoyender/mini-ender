package plugins.regalos

import enums.Severity
import plugins.warnings.WarningsManager
import java.util.concurrent.TimeUnit

object Gifts {

    private val gifts: List<Gift> = listOf(
        Gift(
            name = "No te ha tocado nada :(",
            description = "Parece que esta vez no has tenido suerte, pero no te preocupes, mañana puede ser tu día de suerte!",
            image = "https://media.tenor.com/OasM7f4Pe6UAAAAC/serious-ibai.gif",
            rarity = 0,
            chance = 16
        ),
        Gift(
            name = "¡ Un rol exclusivo del servidor !",
            description = "Has ganado un rol exclusivo del servidor, ¡Enhorabuena!\nSi no has recibido el rol automáticamente, contacta con <@!835642946962718731>.",
            image = "https://media.tenor.com/JITzgeo8qjYAAAAC/ibai-bailando.gif",
            rarity = 5,
            chance = 5,
            hook = { member ->
                val role = member?.guild?.getRoleById("1051119802472738846")
                if (role != null) {
                    member.guild.addRoleToMember(member, role)
                        .queue({}, {
                            WarningsManager.createWarning(
                                member.guild,
                                "No se ha podido dar el rol de regalo al usuario ${member.user.id}",
                                Severity.LOW
                            )
                        })
                }
            },
            unique = true
        ),
        Gift(
            name = "Clase de mates express con Ibai",
            description = "Has ganado una clase de mates express con Ibai, ¡Enhorabuena!\nPresta atención :eyes: que esto es muy importante.",
            image = "https://media.tenor.com/upTNR7Zz4McAAAAC/ibai-explicando.gif",
            rarity = 1,
            chance = 8,
        ),
        Gift(
            name = "Un cuadro de Ibai",
            description = "Has ganado un cuadro de Ibai, ¡Enhorabuena!\nSeguro que queda genial en tu salón.",
            image = "https://cdn.discordapp.com/attachments/855118494005198939/1051157732104884346/luis-yrisarry-labadia-ibai3danim2-king-black.gif",
            rarity = 1,
            chance = 8,
        ),
        Gift(
            name = "Un zumito",
            description = "Has ganado un zumito, ¡Enhorabuena!\nEspero que te guste (lo he hecho yo :D).",
            image = "https://media.tenor.com/7FAvZXFFnPMAAAAC/koi-grifi.gif",
            rarity = 1,
            chance = 8,
        ),
        Gift(
            name = "Un saludito de Ibai",
            description = "Has ganado un saludito de Ibai, ¡Enhorabuena!\nEspero que te guste.",
            image = "https://media.tenor.com/_W41NMLGC5AAAAAC/ibai.gif",
            rarity = 1,
            chance = 8,
        ),
        Gift(
            name = "Una llave de karate",
            description = "Has ganado una llave de karate, ¡Enhorabuena!\n¡Cuidado con la cabeza!",
            image = "https://cdn.discordapp.com/attachments/855118494005198939/1051158043087355904/ibai-fail.gif",
            rarity = 1,
            chance = 8,
        ),
        Gift(
            name = "Beso de kena",
            description = "Has ganado un besito de kena, ¡Enhorabuena!\n¡Espero que te guste!",
            image = "https://cdn.discordapp.com/attachments/704662032888365067/1051558976480874587/4XRTZUqYn_1200x630__1.png",
            rarity = 1,
            chance = 8,
        ),
        Gift(
            name = "Siesta perruna",
            description = "Has ganado un siesta perruna, ¡Enhorabuena!\n¡Duerme bien!",
            image = "https://cdn.discordapp.com/attachments/704662032888365067/1051559433035059270/16325006382946.png",
            rarity = 1,
            chance = 8,
        ),
        Gift(
            name = "TEMPMUTE",
            description = "Has ganado un tempmute de 1m :), ¡Enhorabuena!\n¡Espero que lo pases genial (xd)!",
            image = "https://cdn.discordapp.com/attachments/704662032888365067/1051557370494464030/image0.jpg",
            rarity = 1,
            chance = 5,
            hook = {
                try {
                    it?.guild?.timeoutFor(it, 1, TimeUnit.MINUTES)
                        ?.queue({}, { _ ->
                            WarningsManager.createWarning(
                                it.guild,
                                "No se ha podido dar el temp-mute de regalo al usuario ${it.user.id}",
                                Severity.VERY_LOW
                            )
                        })
                } catch (e: Exception) {
                    WarningsManager.createWarning(
                        it?.guild ?: return@Gift,
                        "No se ha podido dar el temp-mute de regalo al usuario ${it.user.id}",
                        Severity.VERY_LOW
                    )
                }
            }
        ),
        Gift(
            name = "5K XP",
            description = "Has ganado 5k de XP en el servidor, ¡Enhorabuena!\nNo has recibido la XP automáticamente, para reclamarla contacta con <@!835642946962718731>.",
            image = "https://cdn.discordapp.com/attachments/704662032888365067/1051557319701438484/png-clipart-computer-icons-twitter-logo-monochrome-thumbnail.png",
            rarity = 1,
            chance = 2,
        ),
        Gift(
            name = "TUBARAO",
            description = "¡cuidado que viene tubarao!",
            image = "https://cdn.discordapp.com/attachments/704662032888365067/1051554500638429224/6A9F817E-B551-4487-AD7E-1C2B1DC93AF3.gif",
            rarity = 1,
            chance = 8,
        ),
        Gift(
            name = "Porro de chocolate",
            description = "Ibai te regala un porro un porro de chocolate. \nEspero que te guste (lo ha hecho Remilio :D).",
            image = "https://cdn.discordapp.com/attachments/704662032888365067/1051555080463196220/Screenshot_20221211-114257.png",
            rarity = 1,
            chance = 8,
        ),
    )

    fun getGift(): Gift {
        //the chance is a percentage divided by 100 of the gift being selected
        val chance = (0..100).random()
        var currentChance = 0
        for (gift in gifts) {
            currentChance += gift.chance
            if (chance <= currentChance) return gift
        }
        return gifts[0]
    }

}