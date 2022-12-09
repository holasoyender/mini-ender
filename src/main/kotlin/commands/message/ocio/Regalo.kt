package commands.message.ocio

import interfaces.Command
import interfaces.CommandResponse
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import plugins.regalos.GiftManager
import java.awt.Color

class Regalo: Command {
    override fun execute(event: MessageReceivedEvent, args: List<String>): CommandResponse {
        val response = GiftManager.run(event.author)

        if(response.embed == null) {
            event.message.reply(response.message).queue()
        } else {
            val embed = response.embed
            embed.setColor(Color.decode("#2f3136"))
                .setThumbnail("https://twemoji.maxcdn.com/v/latest/72x72/1f381.png")
            event.message.replyEmbeds(embed.build()).queue()
        }
        return CommandResponse.success()
    }

    override val name: String
        get() = "regalo"
    override val description: String
        get() = "Mira a ver que te ha tocado en el regalo de hoy!"
    override val aliases: List<String>
        get() = listOf("regalos", "regalito", "regalitos", "gift", "gifts")
    override val usage: String
        get() = ""
    override val category: String
        get() = "Ocio"
    override val enabled: Boolean
        get() = true
    override val ownerOnly: Boolean
        get() = false
    override val guildOnly: Boolean
        get() = false
    override val global: Boolean
        get() = false
    override val permissions: List<Permission>
        get() = listOf()
    override val botPermissions: List<Permission>
        get() = listOf()
}