package api.routes

import com.google.gson.Gson
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

            val json = Gson().toJson(giveaway)
            response["data"] = Gson().fromJson(json, HashMap::class.java)
            return response

        } else {
            val response: HashMap<String, Any> = HashMap()
            response["status"] = "404"
            response["message"] = "Giveaway not found"
            response
        }
    }
}