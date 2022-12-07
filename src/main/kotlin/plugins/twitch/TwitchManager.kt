package plugins.twitch

import cache.TwitchEventCache
import database.schema.Guild
import enums.Severity
import jda
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.components.buttons.Button
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import plugins.warnings.WarningsManager
import java.awt.Color
import java.time.Instant

object TwitchManager {

    private val logger = LoggerFactory.getLogger(TwitchManager::class.java)

    fun handleEvent(body: JSONObject, messageId: String, timestamp: String): ResponseEntity<String> {

        val event = TwitchEventBody(body)
        println("this " + event.event.broadcasterUserLogin)

        val date = Instant.parse(timestamp).plusSeconds(10).toEpochMilli()
        val now = System.currentTimeMillis()

        if (date < now)
            return ResponseEntity.ok("Event is older than 10 seconds")

        if (TwitchEventCache.getEvent(messageId) != null)
            return ResponseEntity.ok("Event already handled")

        TwitchEventCache.addEvent(messageId, event)

        val streamer = TwitchSubscriptionManager.getStreamer(event.event.broadcasterUserId)
        val stream = TwitchSubscriptionManager.getStream(event.event.broadcasterUserId)

        val guilds = Guild.getGuildsWithTwitchSubscription(event.event.broadcasterUserLogin)
        if (guilds.isEmpty())
            return ResponseEntity.ok("No guilds with this subscription")

        guilds.forEach {

            val guild = jda!!.shardManager!!.getGuildById(it.id)
            if (guild != null) {
                val channelId = it.twitchAnnounceChannelId
                val rawMessage = it.twitchAnnounceMessage
                val liveChannelId = it.twitchLiveChannelId
                val liveMessage = it.twitchLiveMessage

                if (channelId.isNotEmpty()) {
                    val channel = guild.getTextChannelById(channelId)
                    if (channel != null) {

                        val message =
                            rawMessage.ifEmpty { "¡**${event.event.broadcasterUserName}** ha iniciado un directo en **Twitch**!" }
                                .replace("{streamer}", event.event.broadcasterUserName)
                                .replace("{title}", stream?.title ?: "Sin título")
                                .replace("{game}", stream?.gameName ?: "Sin juego")
                                .replace("{url}", "https://twitch.tv/${event.event.broadcasterUserLogin}")
                                .replace(
                                    "{thumbnail}",
                                    stream?.thumbnailUrl?.replace("{width}", "1920")?.replace("{height}", "1080")
                                        ?: "https://static-cdn.jtvnw.net/ttv-static/404_preview-1920x1080.jpg"
                                )

                        channel.sendMessage(message).addEmbeds(
                            EmbedBuilder()
                                .setColor(Color.decode("#9146FF"))
                                .setAuthor(
                                    streamer?.displayName,
                                    null,
                                    streamer?.profileImageUrl ?: guild.iconUrl ?: jda!!.selfUser.avatarUrl
                                )
                                .setThumbnail(streamer?.profileImageUrl ?: guild.iconUrl ?: jda!!.selfUser.avatarUrl)
                                .setImage(
                                    stream?.thumbnailUrl?.replace("{width}", "1920")?.replace("{height}", "1080")
                                        ?: "https://static-cdn.jtvnw.net/ttv-static/404_preview-1920x1080.jpg"
                                )
                                .setTitle(
                                    stream?.title ?: "Sin título",
                                    "https://twitch.tv/${event.event.broadcasterUserLogin}"
                                )
                                .addField("Jugando a", stream?.gameName ?: "Nada", true)
                                .addField("Vistas", stream?.viewerCount?.toString() ?: "0", true)
                                .build()
                        ).setActionRow(
                            Button.link("https://twitch.tv/${event.event.broadcasterUserLogin}", "Ir al directo")
                        ).queue()

                    } else {
                        WarningsManager.createWarning(
                            guild,
                            "No se ha encontrado el canal de anuncios de Twitch",
                            Severity.MEDIUM
                        )
                    }
                }
                if (liveChannelId.isNotEmpty()) {
                    val channel = guild.getTextChannelById(liveChannelId)

                    if(channel != null) {

                        val message = liveMessage.ifEmpty { "¡**${event.event.broadcasterUserName}** ha iniciado un directo en **Twitch**!\nPodéis usar este chat para hablar hasta que el directo finalize" }
                            .replace("{streamer}", event.event.broadcasterUserName)
                            .replace("{title}", stream?.title ?: "Sin título")
                            .replace("{game}", stream?.gameName ?: "Sin juego")
                            .replace("{url}", "https://twitch.tv/${event.event.broadcasterUserLogin}")
                            .replace(
                                "{thumbnail}",
                                stream?.thumbnailUrl?.replace("{width}", "1920")?.replace("{height}", "1080")
                                    ?: "https://static-cdn.jtvnw.net/ttv-static/404_preview-1920x1080.jpg"
                            )

                        val allowedPermissions = channel.getPermissionOverride(guild.publicRole)?.allowed ?: mutableListOf()
                        val deniedPermissions = channel.getPermissionOverride(guild.publicRole)?.denied ?: mutableListOf()
                        allowedPermissions.add(Permission.MESSAGE_SEND)
                        deniedPermissions.remove(Permission.MESSAGE_SEND)

                        channel.manager.putRolePermissionOverride(
                            guild.publicRole.idLong,
                            allowedPermissions,
                            deniedPermissions
                        ).queue({
                            channel.sendMessage(message).queue()
                        }, {
                            WarningsManager.createWarning(
                                guild,
                                "No se han podido dar permisos de enviar mensajes al canal de Twitch Live",
                                Severity.MEDIUM
                            )
                        })

                    } else {
                        WarningsManager.createWarning(
                            guild,
                            "No se ha encontrado el canal de chat de Twitch Live",
                            Severity.MEDIUM
                        )
                    }
                }
            }
        }

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