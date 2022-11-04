package events

import commandManager
import config.Env.PREFIX
import database.schema.Guild
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import plugins.antilink.LinkManager

class MessageHandler: ListenerAdapter() {

    override fun onMessageReceived(event: MessageReceivedEvent) {

        val author = event.author
        val message = event.message
        val content = message.contentRaw

        if(author.isBot) return
        if(message.isWebhookMessage) return

        if(event.isFromGuild && Guild.get(event.guild.id)?.antiLinksEnabled == true)
            LinkManager.check(message)

        val prefix = if(event.isFromGuild) Guild.get(event.guild.id)?.prefix ?: PREFIX ?: "-" else PREFIX ?: "-"

        if(message.mentions.getMentions().isNotEmpty()) {
            val mentioned = message.mentions.getMentions().first()
            if(mentioned.id == event.jda.selfUser.id) {
                message.reply("**¡Hola :wave:!**\nMi prefijo es `$prefix`").queue()
                return
            }
        }

        if(!content.startsWith(prefix)) return

        commandManager!!.run(event)

    }
}