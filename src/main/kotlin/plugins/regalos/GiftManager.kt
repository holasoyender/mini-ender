package plugins.regalos

import database.schema.Regalo
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.utils.TimeFormat
import utils.Emojis

object GiftManager {

    fun run(user: User, member: Member?, skipTimeCheck: Boolean): GiftResponse {

        val dbUser = Regalo.get(user.id)
        if (dbUser != null) {
            return if (dbUser.lastThrow > System.currentTimeMillis() - 72000000 && !skipTimeCheck) {
                GiftResponse("${Emojis.warning}  Ya has abierto tu regalo de hoy! El próximo regalo estará disponible ${TimeFormat.RELATIVE.format(dbUser.lastThrow + 72000000)}", null, true)
            } else {
                /*if (dbUser.gifts.size >= Gifts.giftsSize()) {
                    GiftResponse("${Emojis.warning}  Ya has abierto todos los regalos disponibles! Espera a que se añadan más regalos!", null, true)
                } else {*/

                val gift = Gifts.getGift()

                if(gift.unique && dbUser.gifts.firstOrNull { it["name"] == gift.name } != null) {
                    return GiftResponse("${Emojis.warning}  Parece que te ha tocado un regalo repetido! Prueba a ejecutar el comando otra vez", null, true)
                }

                dbUser.gifts += gift.format()
                dbUser.lastThrow = System.currentTimeMillis()
                dbUser.save()

                if (gift.hook != null)
                    gift.hook.invoke(member)

                GiftResponse(
                    "${Emojis.giveaway}  ¡Has abierto un regalo y has conseguido **${gift.name}**\n${gift.description}",
                    EmbedBuilder()
                        .setAuthor("Has abierto un regalo!", null, user.effectiveAvatarUrl)
                        .setTitle(gift.name)
                        .setDescription(gift.description)
                        .setImage(gift.image), false
                )
                //}
            }
        } else {
            val gift = Gifts.getGift()
            val newUser = Regalo(
                userId = user.id,
                lastThrow = System.currentTimeMillis(),
                gifts = arrayOf(gift.format())
            )
            newUser.save()

            if(gift.hook != null)
                gift.hook.invoke(member)

            return GiftResponse(
                "${Emojis.giveaway}  ¡Has abierto un regalo y has conseguido **${gift.name}**\n${gift.description}",
                EmbedBuilder()
                    .setAuthor("Has abierto un regalo!", null, user.effectiveAvatarUrl)
                    .setTitle(gift.name)
                    .setDescription(gift.description)
                    .setImage(gift.image), false
            )
        }

    }
}