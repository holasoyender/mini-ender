package cache

import net.dv8tion.jda.api.entities.Message

object MessageCache {

    val cache = mutableMapOf<String, List<Message>>()

    fun addMessage(message: Message) {

        val currentCache = cache[message.channel.id]
        if(currentCache == null) {
            cache[message.channel.id] = listOf(message)
        } else {

            if(currentCache.size >= 100) {
                cache[message.channel.id] = currentCache.drop(1) + message
            } else {
                cache[message.channel.id] = currentCache + message
            }
        }
    }

    fun getMessage(channelId: String, messageId: String): Message? {
        val currentCache = cache[channelId] ?: return null
        return currentCache.find { it.id == messageId }
    }

    fun removeMessage(channelId: String, messageId: String) {
        val currentCache = cache[channelId] ?: return
        cache[channelId] = currentCache.filter { it.id != messageId }
    }
}