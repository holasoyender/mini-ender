package commands.message.moderation

import interfaces.Command
import interfaces.CommandResponse
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import utils.Emojis

class Unban: Command {
    override fun execute(event: MessageReceivedEvent, args: List<String>): CommandResponse {

        val user = try {
            event.message.mentions.users.firstOrNull() ?: args.getOrNull(1)
                ?.let { event.jda.retrieveUserById(it).complete() } ?: args.getOrNull(1)
                ?.let { event.jda.getUserByTag(it.split("#")[0], it.split("#")[1] ) }
            ?: return CommandResponse.error("Debes de especificar un usuario valido")
        } catch (e: Exception) {
            return CommandResponse.error("No se ha podido encontrar al usuario con ID ${args.getOrNull(1) ?: "null"}")
        }

        event.guild.unban(user).queue({

            event.message.reply("${Emojis.success} El usuario **${user.asTag}** ha sido desbaneado").queue()

        }, {
            event.message.reply("${Emojis.warning}  El usuario **${user.asTag}** no esta baneado o no he podido desbanearlo")
                .queue()
        })
        return CommandResponse.success()
    }

    override val name: String
        get() = "unban"
    override val description: String
        get() = "Desbanear a un usuario del servidor"
    override val aliases: List<String>
        get() = listOf("desbanear", "unbanuser", "desbanearusuario", "desban")
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
        get() = listOf(Permission.BAN_MEMBERS)
    override val permissionLevel: Int
        get() = 2
    override val botPermissions: List<Permission>
        get() = listOf(Permission.BAN_MEMBERS)
}