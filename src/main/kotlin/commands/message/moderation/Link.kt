package commands.message.moderation

import database.schema.Guild
import database.schema.Links
import interfaces.Command
import interfaces.CommandResponse
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.utils.TimeFormat

class Link: Command {
    override fun execute(event: MessageReceivedEvent, args: List<String>, config: Guild): CommandResponse {

        val domain = args.getOrNull(1) ?: return CommandResponse.error("Debes especificar un dominio del sistema de anti-links")
        val link = Links.get(domain, event.guild.id) ?: return CommandResponse.error("No se ha encontrado ningún dominio con ese nombre en la base de datos")

        val embed = EmbedBuilder()
            .setColor(0xED4245)
            .setAuthor("Dominio del sistema anti-links", null, event.message.author.effectiveAvatarUrl)
            .setDescription("Este es el gestor de configuración del dominio `${link.domain}`, selecciona la acción que deseas realizar con los botones de abajo")
            .addField("Acción actual", link.action.name, true)
            .addField("Fecha", "${TimeFormat.DEFAULT.format(link.blockedAt)} (${TimeFormat.RELATIVE.format(link.blockedAt)})", true)
            .addField("Veces aparecido", link.timesAppeared.toString(), true)
            .addField("En revisión", if (link.underRevision) "Sí" else "No", true)
            .setThumbnail("https://cdn.discordapp.com/attachments/839400943517827092/1038135823650013255/sentinel.png")
            .setColor(0xED4245)

        if(link.duration > 1)
            embed.addField("Duración", link.durationRaw, true)

        event.message.replyEmbeds(embed.build()).addComponents(
            ActionRow.of(
                Button.success("cmd::links:edit:${link.domain}", "Editar la sanción"),
                Button.danger("cmd::links:delete-link:${link.domain}", "Eliminar el dominio")
            ),
        ).queue()

        return CommandResponse.success()
    }

    override val name: String
        get() = "link"
    override val description: String
        get() = "Gestor del sistema anti-links"
    override val aliases: List<String>
        get() = listOf("links", "antilinks", "antilink")
    override val usage: String
        get() = "<Dominio>"
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
    override val permissionLevel: Int
        get() = 2
    override val botPermissions: List<Permission>
        get() = listOf()
}