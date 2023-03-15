package plugins.youtube

import database.Redis
import database.schema.Guild
import database.schema.YouTube
import http.HttpManager
import jda
import net.dv8tion.jda.api.interactions.components.buttons.Button
import org.json.XML
import org.slf4j.LoggerFactory
import java.net.URL

object YouTubeManager {

    private val logger = LoggerFactory.getLogger(YouTubeManager::class.java)

    fun start() {

        if(!Redis.usingRedis) return

        val thread = Thread {
            while (true) {

                val guildsWithSubscription = Guild.getGuildsWithYoutubeSubscriptions()
                val channels = guildsWithSubscription.map { it.youtubeChannel }.distinct()

                if (channels.isEmpty()) {
                    Thread.sleep(20000)
                    continue
                }

                for (channel in channels) {

                    val guilds = guildsWithSubscription.filter { it.youtubeChannel == channel }

                    val body = try {
                        HttpManager.request(URL("https://www.youtube.com/feeds/videos.xml?channel_id=$channel"))
                    } catch (e: Exception) {
                        logger.error("Error while fetching YouTube feed for channel $channel", e)
                        null
                    }

                    if (body == null) {
                        Thread.sleep(20000)
                        continue
                    }

                    val feed = XML.toJSONObject(body).getJSONObject("feed")
                    val entries = feed.getJSONArray("entry")
                    val links = entries.map {
                        it as org.json.JSONObject

                        val link = it.getJSONObject("link")
                        link.getString("href")
                    }

                    val latestVideo = links.firstOrNull()
                    if (latestVideo == null) {
                        Thread.sleep(20000)
                        continue
                    }
                    val latestVideoInCache = YouTube.get(channel)?.latestVideo

                    if (latestVideoInCache == latestVideo) {
                        Thread.sleep(20000)
                        continue
                    }

                    YouTube(channel, latestVideo).save()

                    if (latestVideoInCache == null) {
                        Thread.sleep(20000)
                        continue
                    }

                    for (guildData in guilds) {
                        val guild = jda!!.shardManager!!.getGuildById(guildData.id) ?: continue
                        val guildChannel =
                            guild.getTextChannelById(guildData.youtubeAnnounceChannelId) ?: guild.getNewsChannelById(
                                guildData.youtubeAnnounceChannelId
                            ) ?: continue

                        val message =
                            guildData.youtubeAnnounceMessage.ifEmpty { "Hay un nuevo video en el canal de YouTube!" } + "\n" + latestVideo
                        guildChannel.sendMessage(message).setActionRow(
                            Button.link(
                                latestVideo,
                                "Ir al vídeo"
                            )
                        ).queue({}, {})
                        Thread.sleep(1000)
                    }

                    Thread.sleep(20000)
                }
            }
        }

        thread.isDaemon = true
        thread.name = "YouTubeManagerService"
        thread.start()

    }
}