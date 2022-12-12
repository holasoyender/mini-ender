package commands.message.moderation

import database.schema.Infraction
import interfaces.Command
import interfaces.CommandResponse
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import utils.Emojis

class Delinfr: Command {
    override fun execute(event: MessageReceivedEvent, args: List<String>): CommandResponse {

        val id = args.getOrNull(1)?.toLongOrNull() ?: return CommandResponse.error("Debes de especificar una ID de infracción valida")

        val infraction = Infraction.get(id, event.guild.id) ?: return CommandResponse.error("No se ha podido encontrar una infracción con ID $id")

        event.message.reply("${Emojis.warning}  Estas a punto de eliminar la infracción `#${infraction.id}` de **${infraction.userName}** (`${infraction.userId}`). ¿Estás seguro?").setActionRow(
            Button.danger(
                "cmd::delinfr-confirm:${event.author.id}:${infraction.id}",
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
        get() = "delinfr"
    override val description: String
        get() = "Elimina una infracción de un usuario"
    override val aliases: List<String>
        get() = listOf("delinfraccion", "delinfra", "delinfracción", "delinf")
    override val usage: String
        get() = "<id>"
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
        get() = listOf(Permission.MODERATE_MEMBERS)
    override val permissionLevel: Int
        get() = 2
    override val botPermissions: List<Permission>
        get() = listOf()
}