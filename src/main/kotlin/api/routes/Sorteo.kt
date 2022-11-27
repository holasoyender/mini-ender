package api.routes

import database.schema.Sorteo
import jda
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

@Controller
class Sorteo {

    @RequestMapping("/sorteo/{messageID}")
    fun sorteo(@PathVariable messageID: String, model: Model): String {
        val giveaway = Sorteo.get(messageID)
        return if (giveaway != null) {

            val winners = giveaway.winnerIds.map {
                try {
                    jda!!.shardManager!!.retrieveUserById(it).complete().asTag
                } catch (e: Exception) {
                    null
                }
            }

            val server = jda!!.shardManager!!.getGuildById(giveaway.guildId)
            model.addAttribute("serverName", server?.name ?: "Unknown")
            model.addAttribute("price", "Sorteo de ${giveaway.prize}")
            model.addAttribute("clickers", giveaway.clickers.size)
            model.addAttribute("winnerCount", giveaway.winnerCount)
            model.addAttribute("time", "00:00:00")
            model.addAttribute("serverIcon", server?.iconUrl ?: "https://cdn.discordapp.com/embed/avatars/0.png")
            model.addAttribute("title", "Sorteo en ${server?.name ?: "Unknown"}")
            model.addAttribute("winners", winners)

            //TODO: EL TIEMPO
            "giveaway"
        } else {
            "404"
        }
    }
}