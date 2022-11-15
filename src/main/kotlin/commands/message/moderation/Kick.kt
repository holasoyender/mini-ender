package commands.message.moderation

import database.schema.Infraction
import enums.InfractionType
import interfaces.Command
import interfaces.CommandResponse
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import utils.Emojis

class Kick: Command {
    override fun execute(event: MessageReceivedEvent, args: List<String>): CommandResponse {

        val user = try {
            event.message.mentions.users.firstOrNull() ?: args.getOrNull(1)
                ?.let { event.jda.retrieveUserById(it).complete() }
            ?: return CommandResponse.error("Debes de especificar un usuario valido")
        } catch (e: Exception) {
            return CommandResponse.error("No se ha podido encontrar al usuario con ID ${args.getOrNull(1) ?: "null"}")
        }

        var reason = args.drop(2).joinToString(" ")
        if (reason.isBlank())
            reason = "Sin motivo"

        var isNoMd = false
        if (reason.lowercase().contains("-nomd") || reason.lowercase().contains("-nodm")) {
            isNoMd = true
            reason = reason
                .replace("-nomd", "")
                .replace("-nodm", "")
                .trim()
        }

        val member = try {
            event.guild.retrieveMember(user).complete()
        } catch (e: Exception) {
            return CommandResponse.error("No se ha podido encontrar al miembro con ID ${args.getOrNull(1) ?: "null"}")
        }

        if (event.member?.canInteract(member) == false)
            return CommandResponse.error("No puedes expulsar a un miembro con un rol superior o igual al tuyo")

        if (event.author.id == user.id)
            return CommandResponse.error("No puedes expulsarte a ti mismo")

        if (!event.guild.selfMember.canInteract(member))
            return CommandResponse.error("No puedo expulsar a un miembro con un rol superior al mio")

        if (event.jda.selfUser.id == user.id)
            return CommandResponse.error("No puedo expulsarme a mi mismo")

        val infraction = Infraction(
            userId = user.id,
            userName = user.asTag,
            guildId = event.guild.id,
            moderatorId = event.author.id,
            type = InfractionType.KICK,
            reason = reason,
            duration = 0,
            ended = true,
            succeeded = true,
            date = System.currentTimeMillis()
        )

        if (isNoMd) {
            event.message.reply("${Emojis.success}  Has expulsado al usuario ${user.asMention} con la razón: `$reason`")
                .queue()

            member.kick().reason(reason).queue({
                infraction.save()
            }, {
                infraction.succeeded = false
                infraction.save()
            })

        } else {
            user.openPrivateChannel().queue { channel ->
                channel.sendMessage("${Emojis.warning}  Has sido expulsado del servidor **${event.guild.name}** con la razón: `$reason`")
                    .queue(
                        {
                            member.kick().reason(reason).queue({
                                infraction.save()
                                event.message.reply("${Emojis.success}  Has expulsado al usuario ${user.asMention} con la razón: `$reason`")
                                    .queue()
                            }, {
                                infraction.succeeded = false
                                infraction.save()
                                event.message.reply("${Emojis.warning}  No se ha podido expulsar al usuario ${user.asMention}, comprueba que tenga los permisos necesarios necesarios y que no tenga un rol superior al mio")
                                    .queue()
                            })
                        }, {
                            member.kick().reason(reason).queue({
                                infraction.save()
                                event.message.reply("${Emojis.success}  Has expulsado al usuario ${user.asMention} con la razón: `$reason` pero no ha podido ser notificado")
                                    .queue()
                            }, {
                                infraction.succeeded = false
                                infraction.save()
                                event.message.reply("${Emojis.warning}  No se ha podido expulsar al usuario ${user.asMention}, comprueba que tenga los permisos necesarios necesarios y que no tenga un rol superior al mio")
                                    .queue()
                            })
                        })
            }
        }
        return CommandResponse.success()
    }

    override val name: String
        get() = "kick"
    override val description: String
        get() = "Expulsar a un usuario del servidor con una razón"
    override val aliases: List<String>
        get() = listOf("expulsar", "expulsarusuario", "kickuser", "userkick", "kik")
    override val usage: String
        get() = "<usuario> [razón]"
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
        get() = listOf(Permission.KICK_MEMBERS)
    override val botPermissions: List<Permission>
        get() = listOf(Permission.KICK_MEMBERS)
}