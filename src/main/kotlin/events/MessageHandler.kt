package events

import commandManager
import config.Env.PREFIX
import database.schema.Guild
import managers.GlobalCommandManager
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.internal.entities.emoji.CustomEmojiImpl
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

        if(!content.startsWith(prefix)) {

            val globalPrefixes = listOf("!", "-", "/", "!!", ".", "$", "¡", "?", ",", ";", ">", "@", "#", "&").filter { it != prefix }

            if(globalPrefixes.any { content.startsWith(it) }) {
                val globalPrefix = globalPrefixes.first { content.startsWith(it) }
                val args = content.slice(globalPrefix.length until content.length).split(" ")
                val invoker = args[0]

                val command = commandManager?.getCommands()?.firstOrNull { it.name == invoker.lowercase() || it.aliases.contains(invoker.lowercase()) }
                if(command != null) {

                    if(command.global) {
                        message.addReaction(Emoji.fromCustom(CustomEmojiImpl("slash", 941024012270710874, false)))
                            .queue()
                        GlobalCommandManager.add(event)

                        val removeThread = Thread {
                            Thread.sleep(30000)
                            GlobalCommandManager.remove(message.id)
                        }
                        removeThread.isDaemon = true
                        removeThread.name = "GlobalCommandManager-remove"
                        removeThread.start()
                    }
                }
            }

            return
        }

        commandManager!!.run(event)

    }

    override fun onMessageReactionAdd(event: MessageReactionAddEvent) {

        if(event.emoji.type == Emoji.Type.CUSTOM) {
            val emoji = event.emoji.asCustom()
            if(emoji.id == "941024012270710874") {

                event.retrieveMessage().queue {

                    if(it.author.id != event.userId) return@queue

                    val content = it.contentRaw
                    val globalPrefixes = listOf("!", "-", "/", "!!", ".", "$", "¡", "?", ",", ";", ">", "@", "#", "&")

                    if(globalPrefixes.any { content.startsWith(it) }) {
                        val globalPrefix = globalPrefixes.first { content.startsWith(it) }
                        val args = content.slice(globalPrefix.length until content.length).split(" ")
                        val invoker = args[0]

                        val command = commandManager?.getCommands()?.firstOrNull { it.name == invoker.lowercase() || it.aliases.contains(invoker.lowercase()) }
                        if(command != null) {

                            if(command.global) {
                                val messageEvent = GlobalCommandManager.get(it.id)
                                if(messageEvent != null) {
                                    commandManager!!.run(messageEvent)
                                    GlobalCommandManager.remove(it.id)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}