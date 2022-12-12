package commands.message.moderation

import interfaces.Command
import interfaces.CommandResponse
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import utils.Emojis

class Unlock: Command {
    override fun execute(event: MessageReceivedEvent, args: List<String>): CommandResponse {

        val role = try {
            event.message.mentions.roles.firstOrNull() ?: args.getOrNull(1)
                ?.let { event.guild.getRoleById(it) } ?: event.guild.publicRole
        } catch (e: Exception) {
            event.guild.publicRole
        }

        if (event.channelType != ChannelType.TEXT) {
            return CommandResponse.error("Este comando solo funciona en canales de texto")
        } else {
            val channel = event.channel.asTextChannel()

            if(channel.getPermissionOverride(role)?.allowed?.contains(Permission.MESSAGE_SEND) == true)
                return CommandResponse.error("Este canal no está bloqueado para el rol ${role.name}")

            val allowedPermissions = channel.getPermissionOverride(role)?.allowed ?: mutableListOf()
            val deniedPermissions = channel.getPermissionOverride(role)?.denied ?: mutableListOf()
            allowedPermissions.add(Permission.MESSAGE_SEND)
            deniedPermissions.remove(Permission.MESSAGE_SEND)

            channel.manager.putRolePermissionOverride(
                role.idLong,
                allowedPermissions,
                deniedPermissions
            ).queue({
                event.message.reply("${Emojis.success}  El canal ha sido desbloqueado para el rol ${role.asMention}")
                    .setAllowedMentions(listOf()).queue()
            }, {
                event.message.reply("${Emojis.warning}  No he podido desbloquear el canal, comprueba que tengo los permisos necesarios")
                    .queue()
            })
            return CommandResponse.success()
        }
    }

    override val name: String
        get() = "unlock"
    override val description: String
        get() = "Desbloquea el canal de texto para el rol @everyone o el rol especificado"
    override val aliases: List<String>
        get() = listOf("desbloquear", "unlockchannel", "desbloquearchannel", "unblock", "desbloquearcanal")
    override val usage: String
        get() = "[rol]"
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
        get() = listOf(Permission.MANAGE_CHANNEL)
    override val permissionLevel: Int
        get() = 2
    override val botPermissions: List<Permission>
        get() = listOf(Permission.MANAGE_CHANNEL)
}