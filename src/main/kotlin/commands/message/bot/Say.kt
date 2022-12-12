package commands.message.bot

import interfaces.Command
import interfaces.CommandResponse
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import utils.Emojis

class Say: Command {
    override fun execute(event: MessageReceivedEvent, args: List<String>): CommandResponse {

        val channel = try {
            event.message.mentions.channels.firstOrNull() ?: args.getOrNull(1)?.let { event.guild.getGuildChannelById(it) } ?: event.channel
        } catch (e: Exception) {
            event.channel
        }

        var message = args.drop(2).joinToString(" ")

        if(channel == event.channel)
           message = args.drop(1).joinToString(" ")

        if(channel.type == ChannelType.TEXT) {

            val textChannel = channel as TextChannel
            if(!textChannel.canTalk())
                return CommandResponse.error("No tengo permisos para hablar en #${textChannel.name}")


            if(!textChannel.canTalk(event.member!!))
                return CommandResponse.error("No tienes permisos para hablar en #${textChannel.name}")


            textChannel.sendMessage(message).queue()

            if(channel != event.channel)
                event.message.reply("${Emojis.success}  Mensaje enviado correctamente en #${textChannel.name}").queue()
            return CommandResponse.success()
        } else {
            return CommandResponse.error("Solo puedo enviar mensajes a canales de texto!")
        }

    }

    override val name: String
        get() = "say"
    override val description: String
        get() = "Repite el mensaje que le envíes"
    override val aliases: List<String>
        get() = listOf()
    override val usage: String
        get() = "[canal] <mensaje>"
    override val category: String
        get() = "Bot"
    override val enabled: Boolean
        get() = true
    override val ownerOnly: Boolean
        get() = false
    override val guildOnly: Boolean
        get() = false
    override val global: Boolean
        get() = false
    override val permissions: List<Permission>
        get() = listOf(Permission.MESSAGE_SEND)
    override val permissionLevel: Int
        get() = 1
    override val botPermissions: List<Permission>
        get() = listOf(Permission.MESSAGE_SEND)
}