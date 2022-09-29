package events

import commandManager
import config.Env.PREFIX
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class MessageHandler: ListenerAdapter() {

    override fun onMessageReceived(event: MessageReceivedEvent) {

        val author = event.author
        val message = event.message
        val content = message.contentRaw
        val prefix = PREFIX ?: "-"

        if(message.mentions.getMentions().isNotEmpty()) {
            val mentioned = message.mentions.getMentions().first()
            if(mentioned.id == event.jda.selfUser.id) {
                message.reply("**¡Hola :wave:!**\nMi prefijo es `$prefix`").queue()
                return
            }
        }

        if(author.isBot) return
        if(message.isWebhookMessage) return
        if(!content.startsWith(prefix)) return

        commandManager!!.run(event)

    }
}