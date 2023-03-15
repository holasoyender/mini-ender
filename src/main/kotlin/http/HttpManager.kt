package http

import java.net.HttpURLConnection
import java.net.URL
import java.util.*

object HttpManager {

    fun request(uri: URL): String {
        val http = uri.openConnection() as HttpURLConnection
        http.requestMethod = "GET"
        FakeChrome.fakeChrome(http)
        if (http.responseCode != 200) {
            http.disconnect()
            throw RuntimeException("Failed : HTTP error code : " + http.responseCode)
        }

        var inline = ""
        val scanner = Scanner(uri.openStream())
        while (scanner.hasNext()) {
            inline += scanner.nextLine()
        }
        scanner.close()
        http.disconnect()
        return inline
    }

}