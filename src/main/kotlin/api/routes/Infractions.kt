package api.routes

import com.google.gson.Gson
import database.schema.Infraction
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class Infractions {

    @GetMapping("/infractions/{guildID}/{id}")
    fun getInfractionById(@PathVariable guildID: String, @PathVariable id: Long): HashMap<String, Any> {
        val inf = Infraction.get(id, guildID)
        return if (inf != null) {

            val response: HashMap<String, Any> = HashMap()
            response["status"] = "200"
            response["message"] = "OK"

            val json = Gson().toJson(inf)
            response["data"] = Gson().fromJson(json, HashMap::class.java)

            return response

        } else {
            val response: HashMap<String, Any> = HashMap()
            response["status"] = "404"
            response["message"] = "Infraction not found"
            response
        }
    }
}