package commands.message.moderation

import database.schema.Infraction
import interfaces.Command
import interfaces.CommandResponse
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.utils.TimeFormat

class Infraction: Command {
    override fun execute(event: MessageReceivedEvent, args: List<String>): CommandResponse {

        val id = args.getOrNull(1)?.toLongOrNull() ?: return CommandResponse.error("Debes de especificar una ID de infracción valida")

        val infraction = Infraction.get(id, event.guild.id) ?: return CommandResponse.error("No se ha podido encontrar una infracción con ID $id")
        val infractions = Infraction.getAllByUserId(event.guild.id, infraction.userId)

        val embed = EmbedBuilder()
            .setTitle("Infracción #${infraction.id}")
            .setAuthor("${infraction.userName} (${infraction.userId})")
            .setThumbnail("https://cdn.discordapp.com/attachments/839400943517827092/1038135823650013255/sentinel.png")
            .setColor(0x2f3136)
            .addField("Tipo", "`${infraction.type.name}`", true)
            .addField("Moderador", "<@!${infraction.moderatorId}> (${infraction.moderatorId})", true)
            .addField("Fecha", TimeFormat.DEFAULT.format(infraction.date), true)
            .addField("Acción ejecutada correctamente", if (infraction.succeeded) "Si" else "No", true)
            .addField("Usuario", "<@!${infraction.userId}> (${infraction.userId})", true)
            .setDescription("Razón: ```${infraction.reason}```")

        if(infraction.duration.toInt() != 0) {
            if(infraction.ended) {
                embed.addField("Acabó el", "${TimeFormat.DEFAULT.format(infraction.date + infraction.duration)} (${TimeFormat.RELATIVE.format(infraction.date + infraction.duration)})", true)

            } else {
                embed.addField("Acaba el", "${TimeFormat.DEFAULT.format(infraction.date + infraction.duration)} (${TimeFormat.RELATIVE.format(infraction.date + infraction.duration)})", true)
            }
        }

        event.message.replyEmbeds(embed.build()).addActionRow(
            Button.primary(
                "cmd::infrs:${event.author.id}:reload:0:${infraction.userId}",
                "Todos los documentos"
            ).withDisabled(infractions.size == 1),
            Button.secondary(
                "cmd::infrs:page",
                "Documento ${infractions.map { it.id }.indexOf(infraction.id) + 1}/${infractions.size}"
            ).asDisabled()
        ).queue()

        return CommandResponse.success()
    }

    override val name: String
        get() = "infraction"
    override val description: String
        get() = "Muestra información sobre una infracción"
    override val aliases: List<String>
        get() = listOf("infr", "inf", "infraccion", "infracción", "infrinfo")
    override val usage: String
        get() = "<ID infracción>"
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
        get() = listOf(Permission.MANAGE_SERVER)
    override val botPermissions: List<Permission>
        get() = listOf(Permission.MESSAGE_EMBED_LINKS)
}