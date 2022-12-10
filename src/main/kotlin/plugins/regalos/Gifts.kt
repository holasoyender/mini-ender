package plugins.regalos

import enums.Severity
import plugins.warnings.WarningsManager

object Gifts {

    private val gifts: List<Gift> = listOf(
        Gift(
            name = "No te ha tocado nada :(",
            description = "Parece que esta vez no has tenido suerte, pero no te preocupes, mañana puede ser tu día de suerte!",
            image = "https://media.tenor.com/OasM7f4Pe6UAAAAC/serious-ibai.gif",
            rarity = 0,
            chance = 25
        ),
        Gift(
            name = "¡ Un rol exclusivo del servidor !",
            description = "Has ganado un rol exclusivo del servidor, ¡Enhorabuena!\nSi no has recibido el rol automáticamente, contacta con <@!835642946962718731>.",
            image = "https://media.tenor.com/JITzgeo8qjYAAAAC/ibai-bailando.gif",
            rarity = 5,
            chance = 10,
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
            }
        ),
        Gift(
            name = "Clase de mates express con Ibai",
            description = "Has ganado una clase de mates express con Ibai, ¡Enhorabuena!\nPresta atención :eyes: que esto es muy importante.",
            image = "https://media.tenor.com/upTNR7Zz4McAAAAC/ibai-explicando.gif",
            rarity = 1,
            chance = 13,
        ),
        Gift(
            name = "Un cuadro de Ibai",
            description = "Has ganado un cuadro de Ibai, ¡Enhorabuena!\nSeguro que queda genial en tu salón.",
            image = "https://cdn.discordapp.com/attachments/855118494005198939/1051157732104884346/luis-yrisarry-labadia-ibai3danim2-king-black.gif",
            rarity = 1,
            chance = 13,
        ),
        Gift(
            name = "Un zumito",
            description = "Has ganado un zumito, ¡Enhorabuena!\nEspero que te guste (lo he hecho yo :D).",
            image = "https://media.tenor.com/7FAvZXFFnPMAAAAC/koi-grifi.gif",
            rarity = 1,
            chance = 13,
        ),
        Gift(
            name = "Un saludito de Ibai",
            description = "Has ganado un saludito de Ibai, ¡Enhorabuena!\nEspero que te guste.",
            image = "https://media.tenor.com/_W41NMLGC5AAAAAC/ibai.gif",
            rarity = 1,
            chance = 13,
        ),
        Gift(
            name = "Una llave de karate",
            description = "Has ganado una llave de karate, ¡Enhorabuena!\n¡Cuidado con la cabeza!",
            image = "https://cdn.discordapp.com/attachments/855118494005198939/1051158043087355904/ibai-fail.gif",
            rarity = 1,
            chance = 13,
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