package commands.message.moderation

import database.schema.Infraction
import interfaces.Command
import interfaces.CommandResponse
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.internal.entities.emoji.CustomEmojiImpl
import java.time.Instant

class Infrs: Command {
    override fun execute(event: MessageReceivedEvent, args: List<String>): CommandResponse {

        val user = try {
            event.message.mentions.users.firstOrNull() ?: args.getOrNull(1)
                ?.let { event.jda.retrieveUserById(it).complete() }
            ?: return CommandResponse.error("Debes de especificar un usuario valido")
        } catch (e: Exception) {
            return CommandResponse.error("No se ha podido encontrar al usuario con ID ${args.getOrNull(1) ?: "null"}")
        }

        val infractions = Infraction.getAllByUserId(event.guild.id, user.id)
        if (infractions.isEmpty()) return CommandResponse.error("El usuario ${user.asTag} no tiene ninguna infracción")

        val chunks = infractions.chunked(10)

        val message = chunks.firstOrNull()?.map {
            val date =
                Instant.ofEpochMilli(it.date).toString().split("T")[0] + " " + Instant.ofEpochMilli(it.date).toString()
                    .split("T")[1].split(".")[0]
            "  ${it.type}  | ${it.id} | ${it.userName} (${it.userId}) | ${it.moderatorId} | $date | ${if (it.duration.toInt() == 0) "N/A" else it.duration} | ${if (it.ended) "No" else "Si"} | ${it.reason}"
        }

        val msg = event.message.reply(
            "```  Tipo  |  ID  |  Usuario  |  Moderador  |  Fecha  |  Duración  |  Activa  |  Motivo\n---------------------------------------------------------------------------------\n${
                message?.joinToString("\n") ?: "null"
            }```"
        )

        msg.addActionRow(
            Button.primary(
                "cmd::infrs:${event.author.id}:prev:0:${user.id}",
                Emoji.fromCustom(CustomEmojiImpl("left", 940316141983764494, false))
            ).asDisabled(),
            Button.primary(
                "cmd::infrs:${event.author.id}:next:0:${user.id}",
                Emoji.fromCustom(CustomEmojiImpl("rigth", 940316141782458418, false))
            ).withDisabled(chunks.size == 1),
            Button.primary(
                "cmd::infrs:${event.author.id}:reload:0:${user.id}",
                Emoji.fromCustom(CustomEmojiImpl("loop", 952242523521294456, false))
            ),
        ).queue()

        return CommandResponse.success()
    }

    override val name: String
        get() = "infrs"
    override val description: String
        get() = "Muestra las infracciones de un usuario"
    override val aliases: List<String>
        get() = listOf("infractions", "infr", "infracciones")
    override val usage: String
        get() = "<usuario>"
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