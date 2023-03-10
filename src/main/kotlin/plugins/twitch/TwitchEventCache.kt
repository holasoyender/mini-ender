package plugins.twitch

object TwitchEventCache {

    private val cache = mutableMapOf<String, TwitchEventBody>()

    fun addEvent(messageId: String, event: TwitchEventBody) {
        cache[messageId] = event
        if(cache.size > 100) {
            cache.remove(cache.keys.first())
        }
    }

    fun getEvent(id: String): TwitchEventBody? {
        return cache[id]
    }
}