package plugins.twitch

import config.Env
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
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

    /*fun getActiveSubscriptions(): List<String> {
        val request: Request = Request.Builder()
            .url("https://api.twitch.tv/helix/webhooks/subscriptions")
            .addHeader("Authorization ", "Bearer $accessToken")
            .addHeader("Client-Id", Env.TWITCH_CLIENT_ID!!)
            .addHeader("Content-Type", "application/json")
            .build()

    }*/
}