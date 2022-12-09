package commands.slash.ocio

import interfaces.CommandResponse
import interfaces.SlashCommand
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import plugins.regalos.GiftManager
import java.awt.Color

class Regalo: SlashCommand {
    override fun execute(event: SlashCommandInteractionEvent): CommandResponse {
        val response = GiftManager.run(event.user)

        if(response.embed == null) {
            event.reply(response.message).setEphemeral(response.ephemeral).queue()
        } else {
            val embed = response.embed
            embed.setColor(Color.decode("#2f3136"))
                .setThumbnail("https://twemoji.maxcdn.com/v/latest/72x72/1f381.png")
            event.replyEmbeds(embed.build()).setEphemeral(response.ephemeral).queue()
        }
        return CommandResponse.success()
    }

    override val name: String
        get() = "regalo"
    override val description: String
        get() = "Mira a ver que te ha tocado en el regalo de hoy!"
    override val category: String
        get() = "Ocio"
    override val enabled: Boolean
        get() = true
    override val ownerOnly: Boolean
        get() = false
    override val guildOnly: Boolean
        get() = false
    override val permissions: List<Permission>
        get() = listOf()
    override val botPermissions: List<Permission>
        get() = listOf()
    override val metadata: SlashCommandData?
        get() = null
}