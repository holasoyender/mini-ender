package http

import java.net.HttpURLConnection

object FakeChrome {

    fun fakeChrome(con: HttpURLConnection) {
        con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Safari/537.36")
        con.setRequestProperty("accept-language", "en-US")
        con.setRequestProperty("Content-Type", "application/json")
        con.setRequestProperty("Accept", "application/json")
        con.setRequestProperty("Connection", "keep-alive")
        con.setRequestProperty("DNT", "1")
        con.setRequestProperty("Upgrade-Insecure-Requests", "1")
        con.setRequestProperty("Accept-Encoding", "none")
        con.setRequestProperty("Sec-Ch-Ua", "\" Not;A Brand\";v=\"99\", \"Google Chrome\";v=\"105\", \"Chromium\";v=\"105\"")
        con.setRequestProperty("Sec-Ch-Ua-Mobile", "?0")
        con.setRequestProperty("Sec-Fetch-Dest", "document")
        con.setRequestProperty("Sec-Fetch-Mode", "no-cors")
        con.setRequestProperty("Sec-Fetch-Site", "cross-site")
    }

}