package commands.message.moderation

import interfaces.Command
import interfaces.CommandResponse
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import utils.Emojis
import utils.Time
import java.util.concurrent.TimeUnit

class Slow: Command {
    override fun execute(event: MessageReceivedEvent, args: List<String>): CommandResponse {

        var rawTime = args.getOrNull(1)
            ?: return CommandResponse.error("Debes de especificar un tiempo valido")

        var time = Time.ms(rawTime)


        if(time < 0 || rawTime.lowercase() == "min") {
            rawTime = "min"
            time = 0
        }
        if(time > TimeUnit.HOURS.toMillis(6)  || rawTime.lowercase() == "max") {
            rawTime = "max"
            time = TimeUnit.HOURS.toMillis(6)
        }

        if (event.channelType != ChannelType.TEXT) {
            return CommandResponse.error("Este comando solo funciona en canales de texto")
        } else {
            val channel = event.channel.asTextChannel()

            if(channel.slowmode == time.toInt() / 1000)
                return CommandResponse.error("El canal ya tiene ese tiempo de slowmode")

            channel.manager.setSlowmode(time.toInt() / 1000).queue({
                event.message.reply("${Emojis.success}  El slowmode del canal ha sido cambiado a **$rawTime**")
                    .setAllowedMentions(listOf()).queue()
            }, {
                event.message.reply("${Emojis.warning}  No he podido cambiar el slowmode del canal, comprueba que tengo los permisos necesarios")
                    .queue()
            })

            return CommandResponse.success()
        }
    }

    override val name: String
        get() = "slow"
    override val description: String
        get() = "Establece un intervalo de tiempo entre mensajes"
    override val aliases: List<String>
        get() = listOf("slowmode", "slowmo", "limit", "limitar", "limitarmensajes")
    override val usage: String
        get() = "<tiempo>"
    override val category: String
        get() = "Moderación"
    override val enabled: Boolean
        get() = true
    override val ownerOnly: Boolean
        get() = false
    override val guildOnly: Boolean
        get() = true
    override val global: Boolean
        get() = false
    override val permissions: List<Permission>
        get() = listOf(Permission.MANAGE_CHANNEL)
    override val permissionLevel: Int
        get() = 2
    override val botPermissions: List<Permission>
        get() = listOf(Permission.MANAGE_CHANNEL)
}