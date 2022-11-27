package api.routes

import database.schema.Sorteo
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class Giveaway {

    @GetMapping("/giveaways/{messageID}")
    fun getGiveawayById(@PathVariable messageID: String): HashMap<String, Any> {
        val giveaway = Sorteo.get(messageID)
        return if (giveaway != null) {

            val response: HashMap<String, Any> = HashMap()
            response["status"] = "200"
            response["message"] = "OK"

            Sorteo::class.members.forEach {
                val content = giveaway.get(it.name)
                if (content != null)
                    response[it.name] = content
            }
            return response

        } else {
            val response: HashMap<String, Any> = HashMap()
            response["status"] = "404"
            response["message"] = "Giveaway not found"
            response
        }
    }
}