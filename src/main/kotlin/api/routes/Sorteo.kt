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
                    "Unknown"
                }
            }.toMutableList()

            if (winners.isEmpty())
                if (giveaway.ended)
                    winners.add("Sin ganadores")
                else
                    winners.add("Aún no ha terminado el sorteo")

            val timeLeft = if (giveaway.ended) "Terminado" else {
                val time = giveaway.startedAt + giveaway.endAfter - System.currentTimeMillis()
                val hours = (time % 86400000) / 3600000
                val minutes = (time % 3600000) / 60000
                val seconds = (time % 60000) / 1000

                val timeString = StringBuilder()
                if (hours > 10)
                    timeString.append("$hours")
                else if (hours in 1..9)
                    timeString.append("0$hours")
                else if (hours <= 0)
                    timeString.append("00")
                timeString.append(":")
                if (minutes > 10)
                    timeString.append("$minutes")
                else if (minutes in 1..9)
                    timeString.append("0$minutes")
                else if (minutes <= 0)
                    timeString.append("00")
                timeString.append(":")
                if (seconds > 10)
                    timeString.append("$seconds")
                else if (seconds in 1..9)
                    timeString.append("0$seconds")
                else if (seconds <= 0)
                    timeString.append("00")
                timeString.toString()
            }

            val server = jda!!.shardManager!!.getGuildById(giveaway.guildId)
            model.addAttribute("serverName", server?.name ?: "Unknown")
            model.addAttribute("price", "Sorteo de ${giveaway.prize}")
            model.addAttribute("clickers", giveaway.clickers.size)
            model.addAttribute("winnerCount", giveaway.winnerCount)
            model.addAttribute("time", timeLeft)
            model.addAttribute("serverIcon", server?.iconUrl ?: "https://cdn.discordapp.com/embed/avatars/0.png")
            model.addAttribute("title", "Sorteo en ${server?.name ?: "Unknown"}")
            model.addAttribute("winners", winners)

            "giveaway"
        } else {
            "404"
        }
    }
}