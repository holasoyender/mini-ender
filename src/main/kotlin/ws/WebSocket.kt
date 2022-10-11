@file:Suppress("DEPRECATION")

package ws

import config.Env
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit


object WebSocket {

    private var websocket: WebSocket? = null
    private var aknowledged = false
    private var reconnectTries = 0
    private var thread: Thread? = null

    private val logger = LoggerFactory.getLogger(WebSocket::class.java)

    fun load(clientName: String, token: String) {

        val client: OkHttpClient = OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .build()

        /*
        * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        *
        * Cloudflare tira los websockets cada cierto tiempo, por lo que si se va a usar esto en producción
        * hay que ponerlo en localhost
        *
        * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        * */

        val request: Request = Request.Builder()
            .url("wss://api.kenabot.xyz")
            .addHeader("Authorization", Env.API_TOKEN!!)
            .addHeader("Client-Name", clientName)
            .build()

        websocket = client.newWebSocket(request, Listener(clientName, token))

        client.dispatcher.executorService.shutdown()
    }

    fun startReconnectLoop(clientName: String, token: String) {
        if (!aknowledged) {
            aknowledged = true
            thread = Thread {
                while (true) {
                    if(reconnectTries == 0)
                        logger.error("Parece que la conexión con el WebSocket se ha perdido, reintentando cada 10 segundos...")

                    reconnectTries++
                    load(clientName, token)
                    Thread.sleep(10000)
                }
            }
            thread?.start()
        }
    }

    fun acknowledge() {
        thread?.stop()
        thread = null

        aknowledged = false
        reconnectTries = 0
    }
}