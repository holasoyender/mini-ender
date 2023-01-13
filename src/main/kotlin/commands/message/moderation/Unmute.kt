package commands.message.moderation

import config.DefaultConfig
import database.schema.Guild
import database.schema.Infraction
import interfaces.Command
import interfaces.CommandResponse
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import utils.Emojis

class Unmute: Command {
    override fun execute(event: MessageReceivedEvent, args: List<String>, config: Guild): CommandResponse {

        val user = try {
            event.message.mentions.users.firstOrNull() ?: args.getOrNull(1)
                ?.let { event.jda.retrieveUserById(it).complete() } ?: args.getOrNull(1)
                ?.let { event.jda.getUserByTag(it.split("#")[0], it.split("#")[1]) }
            ?: return CommandResponse.error("Debes de especificar un usuario valido")
        } catch (e: Exception) {
            return CommandResponse.error("No se ha podido encontrar al usuario con ID ${args.getOrNull(1) ?: "null"}")
        }

        val guild = Guild.get(event.guild.id) ?: DefaultConfig.get()
        if (guild.muteRoleId.isEmpty()) return CommandResponse.error("No se ha podido encontrar el rol de mute, comprueba que lo tienes configurado correctamente")
        val muteRole = event.guild.getRoleById(guild.muteRoleId)
            ?: return CommandResponse.error("No se ha podido encontrar el rol de mute, comprueba que lo tienes configurado correctamente")

        val member = try {
            event.guild.retrieveMember(user).complete()
        } catch (e: Exception) {
            return CommandResponse.error("No se ha podido encontrar al miembro con ID ${args.getOrNull(1) ?: "null"}")
        }

        if(!member.roles.contains(muteRole))
            return CommandResponse.error("El usuario no está silenciado")

        val infraction = Infraction.getAllByUserId(event.guild.id, user.id).firstOrNull {
            (it.type.name == "MUTE" || it.type.name == "TEMP_MUTE") && !it.ended
        }

        if(infraction != null) {
            infraction.ended = true
            infraction.save()
        }

        event.guild.removeRoleFromMember(member, muteRole).queue({
            event.message.reply("${Emojis.success} ${member.asMention} ha sido desmuteado correctamente").queue()
        }, {
            event.message.reply("${Emojis.error} No se ha podido desmutear a ${member.asMention}: ${it.message ?: "Error desconocido"}").queue()
        })

        return CommandResponse.success()
    }

    override val name: String
        get() = "unmute"
    override val description: String
        get() = "Des-silenciar a un usuario del servidor"
    override val aliases: List<String>
        get() = listOf("des-silenciar", "unmuteuser", "des-silenciarusuario", "des-silenciar", "unmuteuser", "um")
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
        get() = listOf(Permission.MODERATE_MEMBERS)
    override val permissionLevel: Int
        get() = 2
    override val botPermissions: List<Permission>
        get() = listOf(Permission.MANAGE_ROLES)
}