package plugins.twitch

import cache.TwitchEventCache
import database.schema.Guild
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import java.time.Instant

object TwitchManager {

    private val logger = LoggerFactory.getLogger(TwitchManager::class.java)

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

    fun doInitialChecks() {

        val activeSubscriptions = TwitchSubscriptionManager.getActiveSubscriptions()
        val guildsWithSubscriptions = Guild.getGuildsWithTwitchSubscriptions()

        val notSubscribedChannels = guildsWithSubscriptions.filter { guild ->
            activeSubscriptions.none { subscription ->
                subscription.second.login == guild.twitchChannel
            }
        }.map { it.twitchChannel }

        if(notSubscribedChannels.isNotEmpty()) {
            logger.info("Se han encontrado ${notSubscribedChannels.size} canales de Twitch sin suscripción activa. Se procederá a suscribirlos.")
            notSubscribedChannels.forEach { channel ->
                val (isOk, error) = TwitchSubscriptionManager.subscribeToChannel(channel)
                if(!isOk)
                    logger.error("Error al suscribirse al canal $channel: $error")
            }
        } else
            logger.info("No se han encontrado canales de Twitch sin suscripción activa.")

        val inactiveSubscriptions = activeSubscriptions.filter { subscription ->
            guildsWithSubscriptions.none { guild ->
                guild.twitchChannel == subscription.second.login
            }
        }

        if(inactiveSubscriptions.isNotEmpty()) {
            logger.info("Se han encontrado ${inactiveSubscriptions.size} canales de Twitch con suscripción activa pero sin suscripción en la base de datos. Se procederá a des-suscribirlos.")
            inactiveSubscriptions.forEach { channel ->
                val (isOk, error) = TwitchSubscriptionManager.unsubscribeFromChannel(channel.first)
                if(!isOk)
                    logger.error("Error al des-suscribirse del canal $channel: $error")
            }
        } else
            logger.info("No se han encontrado canales de Twitch inactivos.")
    }
    /*
    * Gracias a Marcock por la ayuda a la hora de implementar el sistema de suscripciones de Twitch.
    * :D
    * */
}