package managers

import net.dv8tion.jda.api.events.message.MessageReceivedEvent

object GlobalCommandManager {

    val cache = mutableMapOf<String, MessageReceivedEvent>()

    fun add(event: MessageReceivedEvent) {
        cache[event.messageId] = event
    }

    fun get(messageId: String): MessageReceivedEvent? {
        return cache[messageId]
    }

    fun remove(messageId: String) {
        cache.remove(messageId)
    }
}