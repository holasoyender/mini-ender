package ws

object WebSocketConnection {

    var isConnected: Boolean = false

    fun setConnected() {
        isConnected = true
    }
    fun setDisconnected() {
        isConnected = false
    }
}