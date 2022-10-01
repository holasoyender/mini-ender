package commands.info

import interfaces.Command
import interfaces.CommandResponse
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.utils.TimeFormat
import java.awt.Color
import java.time.Instant

class Server: Command {
    override fun execute(event: MessageReceivedEvent, args: List<String>): CommandResponse {


        /*
        * Port de https://github.com/holasoyender/Libra/blob/main/src/main/java/libra/Commands/Info/Server.java para kotlin
        */


        val embed = EmbedBuilder()
            .setColor(Color.decode("#2f3136"))
            .setAuthor(event.guild.name, null, event.guild.iconUrl ?: "")
            .addField(
                "ID del servidor",
                "```yml\nID: ${event.guild.id}```",
                false
            )
            .setDescription(
                "**Propietario**: <@!${event.guild.ownerId}> (${event.guild.ownerId})\n**Creado el**: ${TimeFormat.DEFAULT.format(event.guild.timeCreated)} (${TimeFormat.RELATIVE.format(event.guild.timeCreated)})"
            )
            .addField("Nivel de seguridad", levelName(event.guild), true)
            .addField("Miembros totales", "${event.guild.memberCount} miembros", true)
            .addField("Número de canales", "${event.guild.channels.size} canales", true)
            .addField("Número de Roles", "${event.guild.roles.size} roles", true)
            .addField("Shard", "${event.guild.jda.shardInfo.shardId + 1}/${event.guild.jda.shardInfo.shardTotal}", true)
            .setFooter("> " + event.author.asTag, event.author.avatarUrl ?: "")
            .setTimestamp(Instant.now())
            .setThumbnail(event.guild.iconUrl ?: "")

        event.message.replyEmbeds(embed.build()).queue()
        return CommandResponse.success()
    }

    private fun levelName(guild: Guild): String {

        return when (guild.verificationLevel) {
            Guild.VerificationLevel.NONE -> "Ninguno"
            Guild.VerificationLevel.LOW -> "Bajo"
            Guild.VerificationLevel.MEDIUM -> "Medio"
            Guild.VerificationLevel.HIGH -> "Alto"
            Guild.VerificationLevel.VERY_HIGH -> "Muy alto"
            Guild.VerificationLevel.UNKNOWN -> "Desconocido"
        }

    }

    override val name: String
        get() = "server"
    override val description: String
        get() = "Muestra información del servidor"
    override val aliases: List<String>
        get() = listOf("guild", "srv", "servidor")
    override val usage: String
        get() = ""
    override val category: String
        get() = "Info"
    override val enabled: Boolean
        get() = true
    override val ownerOnly: Boolean
        get() = false
    override val guildOnly: Boolean
        get() = true
    override val permissions: List<Permission>
        get() = listOf()
    override val botPermissions: List<Permission>
        get() = listOf()
}