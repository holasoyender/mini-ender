package cache

import net.dv8tion.jda.api.entities.Message

object MessageCache {

    val cache = mutableMapOf<String, List<MessageCacheObject>>()

    fun addMessage(rawMessage: Message) {

        val currentCache = cache[rawMessage.channel.id]
        val message = MessageCacheObject(
            rawMessage.contentRaw,
            rawMessage.contentDisplay,
            rawMessage.id,
            rawMessage.author.asTag,
            rawMessage.author.id,
            rawMessage.author.effectiveAvatarUrl,
            rawMessage.channel.id
        )

        if(currentCache == null) {
            cache[rawMessage.channel.id] = listOf(message)
        } else {

            if(currentCache.size >= 100) {
                cache[rawMessage.channel.id] = currentCache.drop(1) + message
            } else {
                cache[rawMessage.channel.id] = currentCache + message
            }
        }
    }

    fun getMessage(channelId: String, messageId: String): MessageCacheObject? {
        val currentCache = cache[channelId] ?: return null
        return currentCache.find { it.id == messageId }
    }

    fun removeMessage(channelId: String, messageId: String) {
        val currentCache = cache[channelId] ?: return
        cache[channelId] = currentCache.filter { it.id != messageId }
    }

    fun editMessage(channelId: String, messageId: String, newMessage: Message) {
        val currentCache = cache[channelId] ?: return
        val message = MessageCacheObject(
            newMessage.contentRaw,
            newMessage.contentDisplay,
            newMessage.id,
            newMessage.author.asTag,
            newMessage.author.id,
            newMessage.author.effectiveAvatarUrl,
            newMessage.channel.id
        )
        cache[channelId] = currentCache.map { if(it.id == messageId) message else it }
    }
}