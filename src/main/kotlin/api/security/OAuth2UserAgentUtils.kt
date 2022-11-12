package api.security

import org.springframework.http.HttpHeaders
import org.springframework.http.RequestEntity

internal object OAuth2UserAgentUtils {
    fun withUserAgent(request: RequestEntity<*>): RequestEntity<*> {
        val headers = HttpHeaders()
        headers.putAll(request.headers)
        headers.add(HttpHeaders.USER_AGENT, "mini-ender discord")
        return RequestEntity(request.body, headers, request.method, request.url)
    }
}