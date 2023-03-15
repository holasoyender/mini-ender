package plugins.youtube

import database.Redis
import database.schema.Guild
import database.schema.YouTube
import jda
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.XML
import org.slf4j.LoggerFactory

object YouTubeManager {

    private val logger = LoggerFactory.getLogger(YouTubeManager::class.java)
    private val httpClient = OkHttpClient()

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

                    val request: Request = Request.Builder()
                        .url("https://www.youtube.com/feeds/videos.xml?channel_id=$channel")
                        .delete()
                        .build()

                    val body = try {
                        val response = httpClient.newCall(request).execute()
                        val isSuccessful = response.isSuccessful
                        if (!isSuccessful) {
                            null
                        } else {
                            val body = response.body!!.string()
                            response.body!!.close()
                            body
                        }
                    } catch (e: Exception) {
                        null
                    }

                    if (body == null) {
                        logger.error("Error while fetching YouTube feed for channel $channel")
                        continue
                    }

                    val feed = XML.toJSONObject(body).getJSONObject("feed")
                    val entries = feed.getJSONArray("entry")
                    val links = entries.map {
                        it as org.json.JSONObject

                        val link = it.getJSONObject("link")
                        link.getString("href")
                    }

                    val latestVideo = links.firstOrNull() ?: continue
                    val latestVideoInCache = YouTube.get(channel)?.latestVideo

                    if (latestVideoInCache == latestVideo) continue

                    YouTube(channel, latestVideo).save()

                    if (latestVideoInCache == null) continue

                    for (guildData in guilds) {
                        val guild = jda!!.shardManager!!.getGuildById(guildData.id) ?: continue
                        val guildChannel =
                            guild.getTextChannelById(guildData.youtubeAnnounceChannelId) ?: guild.getNewsChannelById(
                                guildData.youtubeAnnounceChannelId
                            ) ?: continue

                        val message =
                            guildData.youtubeAnnounceMessage.ifEmpty { "Hay un nuevo video en el canal de YouTube!" } + "\n" + latestVideo
                        guildChannel.sendMessage(message).queue({}, {})
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