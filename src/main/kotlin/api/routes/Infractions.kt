package api.routes

import database.schema.Infraction
import org.json.JSONObject
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class Infractions {

    @GetMapping("/infractions/{guildID}/{id}")
    fun getInfractionById(@PathVariable guildID: String, @PathVariable id: Long): String {
        val inf = Infraction.get(id, guildID)
        return if (inf != null) {

            val obj = JSONObject()
            Infraction::class.members.forEach {

                obj.put(it.name, inf.get(it.name))
            }
            return obj.toString()

        } else JSONObject()
            .put("error", "No se ha encontrado la infracción con ID $id en el servidor con ID $guildID").toString()

    }
}