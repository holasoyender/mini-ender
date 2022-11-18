package commands.message.moderation

import database.schema.Infraction
import interfaces.Command
import interfaces.CommandResponse
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import utils.Emojis
import utils.Time

class Modinf: Command {
    override fun execute(event: MessageReceivedEvent, args: List<String>): CommandResponse {


        val id = args.getOrNull(1)?.toLongOrNull() ?: return CommandResponse.error("Debes de especificar una ID de infracción valida")
        val input = args.getOrNull(2) ?: return CommandResponse.error("Debes de especificar un tiempo valido")

        val infraction = Infraction.get(id, event.guild.id) ?: return CommandResponse.error("No se ha podido encontrar una infracción con ID $id")

        val time = Time.ms(input)

        if(time <= 0) {
            val reason = args.drop(2).joinToString(" ")
            infraction.reason = reason
            infraction.save()

            event.message.reply("${Emojis.success}  Se ha modificado la razón de la infracción `#${infraction.id}` de **${infraction.userName}** (`${infraction.userId}`) a ```$reason```").queue()
            return CommandResponse.success()
        } else {
            if(infraction.ended) return CommandResponse.error("Esta infracción ya ha terminado")
            if(infraction.duration == 0L) return CommandResponse.error("Esta infracción no admite duración")
            if(infraction.duration == time) return CommandResponse.error("Esta infracción ya tiene ese tiempo")
            if(!infraction.succeeded) return CommandResponse.error("Esta infracción no ha sido aplicada")

            infraction.duration = time
            infraction.save()

            event.message.reply("${Emojis.success}  La duración de la infracción `#${infraction.id}` de **${infraction.userName}** (`${infraction.userId}`) ha sido modificada a **${input}**").queue()
            return CommandResponse.success()
        }
    }

    override val name: String
        get() = "modinf"
    override val description: String
        get() = "Modificar una infracción del servidor"
    override val aliases: List<String>
        get() = listOf("modinfraction", "modinfraccion", "modinfracción", "modificarinfraccion", "modificarinfracción")
    override val usage: String
        get() = "<id> <tiempo/razón>"
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
        get() = listOf()
}