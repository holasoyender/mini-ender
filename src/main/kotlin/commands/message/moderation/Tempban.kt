package commands.message.moderation

import database.schema.Infraction
import enums.InfractionType
import interfaces.Command
import interfaces.CommandResponse
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.utils.TimeFormat
import utils.Emojis
import utils.Time
import java.util.concurrent.TimeUnit


class Tempban: Command {
    override fun execute(event: MessageReceivedEvent, args: List<String>): CommandResponse {

        val user = try {
            event.message.mentions.users.firstOrNull() ?: args.getOrNull(1)
                ?.let { event.jda.retrieveUserById(it).complete() }
            ?: return CommandResponse.error("Debes de especificar un usuario valido")
        } catch (e: Exception) {
            return CommandResponse.error("No se ha podido encontrar al usuario con ID ${args.getOrNull(1) ?: "null"}")
        }

        val rawTime = args.getOrNull(2) ?: return CommandResponse.error("Debes de especificar un tiempo valido (tempban ${this.usage})")
        val time = Time.ms(rawTime)
        if (time <= 0 || time > TimeUnit.DAYS.toMillis(365)) {
            return CommandResponse.error("Debes de especificar un tiempo valido entre 1 segundo y 365 dias")
        }

        var reason = args.drop(3).joinToString(" ")
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
            return CommandResponse.error("No puedes banear a un miembro con un rol superior o igual al tuyo")

        if (event.author.id == user.id)
            return CommandResponse.error("No puedes banearte a ti mismo")

        if (!event.guild.selfMember.canInteract(member))
            return CommandResponse.error("No puedo banear a un miembro con un rol superior al mio")

        if (event.jda.selfUser.id == user.id)
            return CommandResponse.error("No puedo banearme a mi mismo")

        val infraction = Infraction(
            userId = user.id,
            userName = user.asTag,
            guildId = event.guild.id,
            moderatorId = event.author.id,
            type = InfractionType.TEMP_BAN,
            reason = reason,
            duration = time,
            ended = false,
            succeeded = true,
            date = System.currentTimeMillis()
        )

        if (isNoMd) {
            event.message.reply("${Emojis.success}  Has baneado al usuario ${user.asMention} con la razón: `$reason` hasta el ${TimeFormat.DEFAULT.format(time + System.currentTimeMillis())} (${TimeFormat.RELATIVE.format(time + System.currentTimeMillis())})").queue()

            member.ban(0, TimeUnit.SECONDS).reason(reason).queue({
                infraction.save()
            }, {
                infraction.succeeded = false
                infraction.save()
            })

        } else {
            user.openPrivateChannel().queue { channel ->
                channel.sendMessage("${Emojis.warning}  Has sido baneado durante **${rawTime}** del servidor **${event.guild.name}** con la razón: `$reason`. El baneo finalizara el ${TimeFormat.DEFAULT.format(time + System.currentTimeMillis())} (${TimeFormat.RELATIVE.format(time + System.currentTimeMillis())})")
                    .queue(
                        {
                            member.ban(0, TimeUnit.SECONDS).reason(reason).queue({
                                infraction.save()
                                event.message.reply("${Emojis.success}  Has baneado al usuario ${user.asMention} con la razón: `$reason` hasta el ${TimeFormat.DEFAULT.format(time + System.currentTimeMillis())} (${TimeFormat.RELATIVE.format(time + System.currentTimeMillis())})").queue()
                            }, {
                                infraction.succeeded = false
                                infraction.save()
                                event.message.reply("${Emojis.warning}  No se ha podido banear al usuario ${user.asMention}, comprueba que tenga los permisos necesarios necesarios y que no tenga un rol superior al mio")
                                    .queue()
                            })
                        }, {
                            member.ban(0, TimeUnit.SECONDS).reason(reason).queue({
                                infraction.save()
                                event.message.reply("${Emojis.success}  Has baneado al usuario ${user.asMention} con la razón: `$reason` hasta el ${TimeFormat.DEFAULT.format(time + System.currentTimeMillis())} (${TimeFormat.RELATIVE.format(time + System.currentTimeMillis())}), pero no ha podido ser notificado").queue()
                            }, {
                                infraction.succeeded = false
                                infraction.save()
                                event.message.reply("${Emojis.warning}  No se ha podido banear al usuario ${user.asMention}, comprueba que tenga los permisos necesarios necesarios y que no tenga un rol superior al mio")
                                    .queue()
                            })
                        })
            }
        }
        return CommandResponse.success()
    }

    override val name: String
        get() = "tempban"
    override val description: String
        get() = "Expulsar temporalmente a un usuario del servidor con una razón"
    override val aliases: List<String>
        get() = listOf("tempban", "tban", "tb")
    override val usage: String
        get() = "<usuario> <duración> [razón]"
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
    override val botPermissions: List<Permission>
        get() = listOf(Permission.BAN_MEMBERS)
}