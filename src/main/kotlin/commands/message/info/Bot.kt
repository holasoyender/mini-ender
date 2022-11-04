package commands.message.info

import interfaces.Command
import interfaces.CommandResponse
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import java.awt.Color
import java.time.Instant

class Bot: Command {
    override fun execute(event: MessageReceivedEvent, args: List<String>): CommandResponse {

        val embed: EmbedBuilder = EmbedBuilder()
            .setColor(Color.decode("#2f3136"))
            .setAuthor("Información sobre ${event.jda.selfUser.name}", null, event.jda.selfUser.avatarUrl)
            .setThumbnail(event.jda.selfUser.avatarUrl)
            .addField("Propietario", "`holasoyender#8934`", true)
            .addField("Página web", "[KenaBot](https://kenabot.xyz) | [Open source](https://github.com/holasoyender/mini-ender)", true)
            .addField("Servidor de soporte", "[Soporte](https://discord.gg/WgRBDFk63s)", true)
            .addField("Servidores actuales", "${event.jda.guilds.size} Servidores", true)
            .addField("Shards", "${event.jda.shardInfo.shardId + 1}/${event.jda.shardInfo.shardTotal}", true)
            .setFooter("> " + event.author.asTag, event.author.avatarUrl ?: "")
            .setTimestamp(Instant.now())
            .setTimestamp(Instant.now())

        event.message.replyEmbeds(embed.build()).addActionRow(
            Button.primary("cmd::help:" + event.author.id, "\uD83D\uDCC3  Lista de comandos"),
            Button.link("https://kenabot.xyz", "\uD83D\uDCBB  Web del proyecto"),
            Button.link("https://github.com/holasoyender/mini-ender", "\uD83D\uDCC3  Repositorio")
        ).queue()

        return CommandResponse.success()
    }

    override val name: String
        get() = "bot"
    override val description: String
        get() = "Muestra información sobre el bot"
    override val aliases: List<String>
        get() = listOf("botinfo")
    override val usage: String
        get() = ""
    override val category: String
        get() = "Info"
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