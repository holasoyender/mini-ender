package events

import commandManager
import config.DefaultConfig
import database.Redis
import database.schema.Guild
import database.schema.Message
import logger.EventLogger
import managers.GlobalCommandManager
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.message.MessageDeleteEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.MessageUpdateEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.internal.entities.emoji.CustomEmojiImpl
import org.json.JSONArray
import plugins.antilink.LinkManager
import plugins.antilink.Phishing
import plugins.modchannel.ModChannel
import utils.PingCalc
import java.awt.Color

class MessageHandler: ListenerAdapter() {

    override fun onMessageReceived(event: MessageReceivedEvent) {

        if (event.channel.id == "838900371262799883" && event.channel is NewsChannel) {
            (event.channel as NewsChannel).crosspostMessageById(event.messageId).queue()
        }

        PingCalc.start()
        val author = event.author
        val message = event.message
        val content = message.contentRaw

        if (author.isBot) return
        if (message.isWebhookMessage) return
        val guild = if (event.isFromGuild) Guild.get(event.guild.id) ?: DefaultConfig.get() else DefaultConfig.get()

        val args = content.slice(guild.prefix.length until content.length).split(" ")
        var invoker = args[0]

        if (event.isFromGuild) {

            if (Redis.usingRedis)
                Message(
                    message.contentRaw,
                    message.contentDisplay,
                    message.id,
                    message.author.asTag,
                    message.author.id,
                    message.author.effectiveAvatarUrl,
                    message.channel.id
                ).save()

            if (
                !guild.antiLinksIgnoredChannels.contains(message.channel.id) &&
                !guild.antiLinksIgnoredRoles.any { message.member?.roles?.map { r -> r.id }?.contains(it) == true }
            ) {
                if (guild.antiLinksEnabled) {
                    if (guild.antiPhishingEnabled) {
                        if (Phishing.isPhishing(message))
                            Phishing.checkPhishing(message)
                        else
                            LinkManager.check(message, guild)
                    } else {
                        LinkManager.check(message, guild)
                    }
                }
            }

            if (ModChannel.isModChannel(message, guild)) {
                val parsed = ModChannel.parse(message)
                if (parsed.users.isEmpty()) {
                    message.addReaction(Emoji.fromUnicode("❌")).queue({}, {})
                } else {
                    ModChannel.doAction(parsed, message)
                }
            }
        }

        val prefix = guild.prefix

        /*if (content == "<@!${event.jda.selfUser.id}>" || content == "<@${event.jda.selfUser.id}>") {
            val mentioned = message.mentions.getMentions().first()
            if (mentioned.id == event.jda.selfUser.id) {
                val embed = EmbedBuilder()
                    .setColor(Color.decode("#2f3136"))
                    .setAuthor("¡Hola, soy ${event.jda.selfUser.name}!", null, event.jda.selfUser.avatarUrl)
                    .setDescription("Mi prefijo en este servidor es `$prefix`, usa `${prefix}help` para ver la lista de comandos disponibles")
                message.replyEmbeds(embed.build()).setActionRow(
                    Button.link(
                        "https://discord.com/api/oauth2/authorize?client_id=${event.jda.selfUser.id}&permissions=8&scope=bot%20applications.commands",
                        "Invítame"
                    ),
                    Button.link("https://discord.gg/WgRBDFk63s", "Soporte"),
                    Button.link("https://kenabot.xyz", "Sitio web"),
                    Button.link("https://github.com/holsoyender/mini-ender", "Repositorio"),
                    Button.primary("cmd::help:${event.author.id}", "Comandos")
                ).queue()
                return
            }
        } else {
            if (!event.isFromGuild && !content.startsWith(prefix)) {
                val embed = EmbedBuilder()
                    .setColor(Color.decode("#2f3136"))
                    .setAuthor("¡Hola, soy ${event.jda.selfUser.name}!", null, event.jda.selfUser.avatarUrl)
                    .setDescription("Mi prefijo en este servidor es `$prefix`, usa `${prefix}help` para ver la lista de comandos disponibles")
                message.replyEmbeds(embed.build()).setActionRow(
                    Button.link(
                        "https://discord.com/api/oauth2/authorize?client_id=${event.jda.selfUser.id}&permissions=8&scope=bot%20applications.commands",
                        "Invítame"
                    ),
                    Button.link("https://discord.gg/WgRBDFk63s", "Soporte"),
                    Button.link("https://kenabot.xyz", "Sitio web"),
                    Button.link("https://github.com/holsoyender/mini-ender", "Repositorio"),
                    Button.primary("cmd::help:${event.author.id}", "Comandos")
                ).queue()
                return
            }
        }*/

        if (!content.startsWith(prefix)) {

            val globalPrefixes =
                listOf("!", "-", "/", "!!", ".", "$", "¡", "?", ",", ";", ">", "@", "#", "&").filter { it != prefix }

            if (globalPrefixes.any { content.startsWith(it) }) {
                val globalPrefix = globalPrefixes.first { content.startsWith(it) }
                val globalArgs = content.slice(globalPrefix.length until content.length).split(" ")
                val globalInvoker = globalArgs[0]

                val command = commandManager?.getCommands()
                    ?.firstOrNull { it.name == globalInvoker.lowercase() || it.aliases.contains(invoker.lowercase()) }
                if (command != null) {

                    if (command.global) {
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

        val customCommand = guild.customCommands.firstOrNull {
            val name = it["name"] as String
            val aliases = it["aliases"] as JSONArray
            content.startsWith("$prefix$name") || aliases.any { alias -> content.startsWith("$prefix$alias") }
        }
        if (customCommand != null) {
            val response = customCommand["response"] as String
            val isCommand = commandManager?.getCommands()
                ?.firstOrNull { it.name == response.lowercase() || it.aliases.contains(response.lowercase()) } != null
            if (isCommand) {
                invoker = response
            } else {
                message.reply(response).queue()
                return
            }
        }

        commandManager!!.run(invoker, event)

    }

    override fun onMessageReactionAdd(event: MessageReactionAddEvent) {

        if (event.emoji.type == Emoji.Type.CUSTOM) {
            val emoji = event.emoji.asCustom()
            if (emoji.id == "941024012270710874") {

                event.retrieveMessage().queue {

                    if (it.author.id != event.userId) return@queue

                    val content = it.contentRaw
                    val globalPrefixes = listOf("!", "-", "/", "!!", ".", "$", "¡", "?", ",", ";", ">", "@", "#", "&")

                    if (globalPrefixes.any { content.startsWith(it) }) {
                        val globalPrefix = globalPrefixes.first { content.startsWith(it) }
                        val args = content.slice(globalPrefix.length until content.length).split(" ")
                        val invoker = args[0]

                        val command = commandManager?.getCommands()
                            ?.firstOrNull { it.name == invoker.lowercase() || it.aliases.contains(invoker.lowercase()) }
                        if (command != null) {
                            if (command.global) {
                                val messageEvent = GlobalCommandManager.get(it.id)
                                if (messageEvent != null) {
                                    commandManager!!.run(invoker, messageEvent)
                                    GlobalCommandManager.remove(it.id)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onMessageDelete(event: MessageDeleteEvent) {
        if (event.isFromGuild)
            EventLogger(event.guild, Guild.get(event.guild.id) ?: DefaultConfig.get()).log(event)
        super.onMessageDelete(event)
    }

    override fun onMessageUpdate(event: MessageUpdateEvent) {

        if (event.author.isBot) return

        if (event.isFromGuild)
            EventLogger(event.guild, Guild.get(event.guild.id) ?: DefaultConfig.get()).log(event)

        if (event.isFromGuild) {

            val message = event.message

            val guild = Guild.get(event.guild.id) ?: DefaultConfig.get()

            if (
                !guild.antiLinksIgnoredChannels.contains(message.channel.id) &&
                !guild.antiLinksIgnoredRoles.any { message.member?.roles?.map { r -> r.id }?.contains(it) == true }
            ) {
                if (guild.antiLinksEnabled) {
                    if (guild.antiPhishingEnabled) {
                        if (Phishing.isPhishing(message))
                            Phishing.checkPhishing(message)
                        else
                            LinkManager.check(message, guild)
                    } else {
                        LinkManager.check(message, guild)
                    }
                }
            }
        }

        super.onMessageUpdate(event)
    }
}