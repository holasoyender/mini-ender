package plugins.twitch

import org.json.JSONObject
import org.springframework.http.ResponseEntity

object TwitchManager {

    fun handleEvent(body: JSONObject): ResponseEntity<String> {
        return ResponseEntity.ok().body("OK")
    }
}