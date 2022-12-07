package plugins.twitch

import config.Env
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

object TwitchSubscriptionManager {

    private var thread: Thread? = null
    private var accessToken: String? = ""
    private val httpClient = OkHttpClient()

    fun auth() {
        thread = Thread {
            while(true) {
                val (accessToken, expiresIn) = credentialRequest()
                this.accessToken = accessToken

                if(expiresIn <= 0)
                    Thread.sleep(1000 * 60 * 60)

                Thread.sleep(expiresIn)
            }
        }
        thread!!.isDaemon = true
        thread!!.start()
    }

    private fun credentialRequest(): Pair<String, Long> {

        val requestBody: RequestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("client_id", Env.TWITCH_CLIENT_ID!!)
            .addFormDataPart("client_secret", Env.TWITCH_CLIENT_SECRET!!)
            .addFormDataPart("grant_type", "client_credentials")
            .addFormDataPart("scope", "user_read")
            .addFormDataPart("response_type", "token")
            .build()

        val request: Request = Request.Builder()
            .url("https://id.twitch.tv/oauth2/token")
            .post(requestBody)
            .build()

        return try {
            val response = httpClient.newCall(request).execute()

            if(!response.isSuccessful)
                return Pair("", -1)

            val body = response.body!!.string()
            val json = JSONObject(body)
            val accessToken = json["access_token"] as String
            val expiresIn = json["expires_in"] as Int

            Pair(accessToken, expiresIn.toLong())
        } catch (e: Exception) {
            Pair("", -1)
        }
    }

    fun getStreamer(channel: String): Streamer? {

        val isId = try {
            channel.toLong()
            true
        } catch (e: Exception) {
            false
        }

        val request: Request = Request.Builder()
            .url("https://api.twitch.tv/helix/users?${if(isId) "id" else "login"}=$channel")
            .addHeader("Authorization", "Bearer ${this.accessToken}")
            .addHeader("Client-Id", Env.TWITCH_CLIENT_ID!!)
            .addHeader("Content-Type", "application/json")
            .get()
            .build()

        return try {
            val response = httpClient.newCall(request).execute()

            if(!response.isSuccessful)
                return null

            val body = response.body!!.string()
            val json = JSONObject(body)
            val data = json["data"] as JSONArray

            if(data.isEmpty)
                return null

            val streamer = data[0] as JSONObject

            Streamer(
                streamer["id"] as String,
                streamer["login"] as String,
                streamer["display_name"] as String,
                streamer["type"] as String,
                streamer["broadcaster_type"] as String,
                streamer["description"] as String,
                streamer["profile_image_url"] as String,
                streamer["offline_image_url"] as String,
                streamer["view_count"] as Int
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun getStreamers(channels: List<String>): List<Streamer> {

        val query = channels.joinToString("&id=") { it }

        val request: Request = Request.Builder()
            .url("https://api.twitch.tv/helix/users?id=$query")
            .addHeader("Authorization", "Bearer ${this.accessToken}")
            .addHeader("Client-Id", Env.TWITCH_CLIENT_ID!!)
            .addHeader("Content-Type", "application/json")
            .get()
            .build()

        return try {
            val response = httpClient.newCall(request).execute()

            if(!response.isSuccessful)
                return emptyList()

            val body = response.body!!.string()
            val json = JSONObject(body)
            val data = json["data"] as JSONArray

            if(data.isEmpty)
                return emptyList()

            data.map {
                val streamer = it as JSONObject
                Streamer(
                    streamer["id"] as String,
                    streamer["login"] as String,
                    streamer["display_name"] as String,
                    streamer["type"] as String,
                    streamer["broadcaster_type"] as String,
                    streamer["description"] as String,
                    streamer["profile_image_url"] as String,
                    streamer["offline_image_url"] as String,
                    streamer["view_count"] as Int
                )
            }
        } catch (e: Exception) {
            listOf()
        }

    }

    fun getStream(streamerId: String): Stream? {

        val request: Request = Request.Builder()
            .url("https://api.twitch.tv/helix/streams?user_id=$streamerId")
            .addHeader("Authorization", "Bearer ${this.accessToken}")
            .addHeader("Client-Id", Env.TWITCH_CLIENT_ID!!)
            .addHeader("Content-Type", "application/json")
            .get()
            .build()

        return try {
            val response = httpClient.newCall(request).execute()

            if(!response.isSuccessful)
                return null

            val body = response.body!!.string()
            val json = JSONObject(body)
            val data = json["data"] as JSONArray

            if(data.isEmpty)
                return null

            val stream = data[0] as JSONObject

            Stream(
                stream["id"] as String,
                stream["user_id"] as String,
                stream["user_login"] as String,
                stream["user_name"] as String,
                stream["game_id"] as String,
                stream["game_name"] as String,
                stream["type"] as String,
                stream["title"] as String,
                stream["viewer_count"] as Int,
                stream["started_at"] as String,
                stream["language"] as String,
                stream["thumbnail_url"] as String
            )

        } catch (e: Exception) {
            null
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun getActiveSubscriptions(): List<Pair<String, Streamer>> {
        val request: Request = Request.Builder()
            .url("https://api.twitch.tv/helix/eventsub/subscriptions")
            .addHeader("Authorization", "Bearer ${this.accessToken}")
            .addHeader("Client-Id", Env.TWITCH_CLIENT_ID!!)
            .addHeader("Content-Type", "application/json")
            .build()

        return try {
            val response = httpClient.newCall(request).execute()

            if(!response.isSuccessful)
                return emptyList()

            val body = response.body!!.string()
            val json = JSONObject(body)
            val data = json["data"] as JSONArray

            val streams = data.filter { (it as JSONObject)["status"] == "enabled" && it["type"] == "stream.online" }
                .map { Pair(((it as JSONObject)["condition"] as JSONObject)["broadcaster_user_id"] as String, it["id"] as String) }

            val streamers = getStreamers(streams.map { it.first })
            streams.map { Pair(it.second, streamers.find { streamer -> streamer.id == it.first }!!) }

        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }

    }

    fun subscribeToChannel(channel: String): Pair<Boolean, String> {

        val isId = try {
            channel.toLong()
            true
        } catch (e: Exception) {
            false
        }

        val streamerId = if(!isId)
            (getStreamer(channel) ?: return Pair(false, "Streamer not found")).id
        else channel

        val body = JSONObject()
            .put("type", "stream.online")
            .put("version", "1")
            .put("condition", JSONObject().put("broadcaster_user_id", streamerId))
            .put("transport", JSONObject().put("method", "webhook").put("callback", "${Env.API_URL}/twitch/gateway").put("secret", Env.TWITCH_CLIENT_SECRET!!))

        val request: Request = Request.Builder()
            .url("https://api.twitch.tv/helix/eventsub/subscriptions")
            .addHeader("Authorization", "Bearer ${this.accessToken}")
            .addHeader("Client-Id", Env.TWITCH_CLIENT_ID!!)
            .addHeader("content-type", "application/json")
            .post(body.toString().toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        return try {
            val response = httpClient.newCall(request).execute()

            Pair(response.isSuccessful, response.body!!.string())
        } catch (e: Exception) {
            Pair(false, e.message!!)
        }
    }

    fun unsubscribeFromChannel(eventId: String): Pair<Boolean, String> {

        val request: Request = Request.Builder()
            .url("https://api.twitch.tv/helix/eventsub/subscriptions?id=$eventId")
            .addHeader("Authorization", "Bearer ${this.accessToken}")
            .addHeader("Client-Id", Env.TWITCH_CLIENT_ID!!)
            .addHeader("Content-Type", "application/json")
            .delete()
            .build()

        return try {
            val response = httpClient.newCall(request).execute()

            Pair(response.isSuccessful, response.body!!.string())
        } catch (e: Exception) {
            Pair(false, e.message!!)
        }
    }
}