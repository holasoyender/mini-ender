package commands.message.moderation

import database.schema.Infraction
import interfaces.Command
import interfaces.CommandResponse
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import utils.Emojis

class Delinfrs: Command {
    override fun execute(event: MessageReceivedEvent, args: List<String>): CommandResponse {

        val user = try {
            event.message.mentions.users.firstOrNull() ?: args.getOrNull(1)
                ?.let { event.jda.retrieveUserById(it).complete() }
            ?: return CommandResponse.error("Debes de especificar un usuario valido")
        } catch (e: Exception) {
            return CommandResponse.error("No se ha podido encontrar al usuario con ID ${args.getOrNull(1) ?: "null"}")
        }

        val infractions = Infraction.getAllByUserId(event.guild.id, user.id)
        if (infractions.isEmpty())
            return CommandResponse.error("El usuario **${user.asTag}** no tiene ninguna infracción")

        event.message.reply("${Emojis.warning}  Estas a punto de eliminar todas las infracciones (`${infractions.size}`) del usuario **${user.asTag}** (`${user.id}`). ¿Estás seguro?").setActionRow(
            Button.danger(
                "cmd::delinfrs-confirm:${event.author.id}:${user.id}",
                "Confirmar"
            ),
            Button.secondary(
                "cmd::cancel:${event.author.id}",
                "Cancelar"
            )
        ).queue()
        return CommandResponse.success()
    }

    override val name: String
        get() = "delinfrs"
    override val description: String
        get() = "Elimina todas las infracciones de un usuario"
    override val aliases: List<String>
        get() = listOf("delinfracciones", "delinfras", "delinfracciónes", "delinfs")
    override val usage: String
        get() = "<usuario>"
    override val category: String
        get() = "Moderación"
    override val enabled: Boolean
        get() = true
    override val ownerOnly: Boolean
        get() = true
    override val guildOnly: Boolean
        get() = true
    override val global: Boolean
        get() = false
    override val permissions: List<Permission>
        get() = listOf(Permission.ADMINISTRATOR)
    override val botPermissions: List<Permission>
        get() = listOf()
}