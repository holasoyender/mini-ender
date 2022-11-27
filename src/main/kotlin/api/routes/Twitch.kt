package api.routes

import config.Env
import org.apache.commons.codec.binary.Hex
import org.json.JSONObject
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import plugins.twitch.TwitchManager
import java.security.Key
import javax.crypto.Mac
import javax.servlet.http.HttpServletRequest

@RestController
class Twitch {

    @PostMapping("/twitch/gateway")
    fun twitchRequest(
        request: HttpServletRequest,
        @RequestBody bodyString: String?,
    ): ResponseEntity<String> {

        val hmacMessage =
            request.getHeader("Twitch-Eventsub-Message-Id".lowercase()) + request.getHeader("Twitch-Eventsub-Message-Timestamp".lowercase()) + bodyString
        val secret = Env.TWITCH_CLIENT_SECRET

        val body = JSONObject(bodyString ?: "{}")

        val hmac = Mac.getInstance("HmacSHA256")
        val secretKey: Key = javax.crypto.spec.SecretKeySpec(secret!!.toByteArray(), "HmacSHA256")
        hmac.init(secretKey)
        hmac.update(hmacMessage.toByteArray())
        val hmacHex = Hex.encodeHexString(hmac.doFinal())

        val key = "sha256=$hmacHex"

        if (key.equals(request.getHeader("Twitch-Eventsub-Message-Signature".lowercase()), ignoreCase = true)) {

            if (request.getHeader("Twitch-Eventsub-Message-Type".lowercase()) == "webhook_callback_verification")
                return ResponseEntity.ok()
                    .body(body["challenge"] as String? ?: "No se ha podido obtener el challenge")
            else
                if (request.getHeader("Twitch-Eventsub-Message-Type".lowercase()) == "notification")
                    return TwitchManager.handleEvent(body, request.getHeader("Twitch-Eventsub-Message-Id".lowercase()), request.getHeader("Twitch-Eventsub-Message-Timestamp".lowercase()))

            return ResponseEntity.ok("OK")

        } else
            return ResponseEntity.badRequest().body("Invalid signature")

    }

    //https://dev.twitch.tv/docs/eventsub/manage-subscriptions
}