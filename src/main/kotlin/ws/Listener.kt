package ws


import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.slf4j.LoggerFactory

class Listener(
    private val clientName: String,
    private val token: String
    ) : WebSocketListener() {

    private val logger = LoggerFactory.getLogger(ws.WebSocket::class.java)

    override fun onOpen(webSocket: WebSocket, response: Response) {
        logger.info("Conectado al WebSocket de la API central")
        ws.WebSocket.acknowledge()
        WebSocketConnection.setConnected()
    }

    override fun onMessage(webSocket: WebSocket, text: String) {

        println(text)
        var json: JSONObject? = null
        try {
            json = JSONParser().parse(text) as JSONObject
        } catch (e: Exception) {
            logger.error("Error al parsear el JSON recibido del WebSocket: ${e.message}")
        }

        json//todo
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        println("WebSocket cerrado code: $code, reason: $reason")
        ws.WebSocket.startReconnectLoop(clientName, token)
        WebSocketConnection.setDisconnected()
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        if (code == 1003 && reason == "Unauthorized") {
            logger.error("El token proporcionado no es válido y no se podrá conectar al WebSocket")
        } else {
            logger.error("Se ha perdido la conexión con el WebSocket de la API central. Código: $code, Razón: $reason")
        }
        WebSocketConnection.setDisconnected()
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        if(t.message?.contains("Failed to connect to localhost") == false)
            logger.error("Error en el WebSocket: ${t.message}")

        ws.WebSocket.startReconnectLoop(clientName, token)
        WebSocketConnection.setDisconnected()
    }
}