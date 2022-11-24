package plugins.twitch

import cache.TwitchEventCache
import org.json.JSONObject
import org.springframework.http.ResponseEntity
import java.time.Instant

object TwitchManager {

    fun handleEvent(body: JSONObject, messageId: String, Timestamp: String): ResponseEntity<String> {

        val event = TwitchEventBody(body)

        val date = Instant.parse(Timestamp).plusSeconds(10).toEpochMilli()
        val now = System.currentTimeMillis()

        if(date < now)
            return ResponseEntity.ok("Event is older than 10 seconds")

        if(TwitchEventCache.getEvent(messageId) != null)
            return ResponseEntity.ok("Event already handled")

        TwitchEventCache.addEvent(messageId, event)

        return ResponseEntity.ok().body("OK")
    }
}