package commands.message.moderation

import config.DefaultConfig
import database.schema.Guild
import database.schema.Infraction
import enums.InfractionType
import interfaces.Command
import interfaces.CommandResponse
import logger.InfractionLogger
import messages.Formatter
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import utils.Emojis

class Warn: Command {
    override fun execute(event: MessageReceivedEvent, args: List<String>, config: Guild): CommandResponse {

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
            return CommandResponse.error("No puedes avisar a un miembro con un rol superior o igual al tuyo")

        if (event.author.id == user.id)
            return CommandResponse.error("No puedes avisarte a ti mismo")

        if (!event.guild.selfMember.canInteract(member))
            return CommandResponse.error("No puedo avisar a un miembro con un rol superior al mio")

        if (event.jda.selfUser.id == user.id)
            return CommandResponse.error("No puedo avisarme a mi mismo")

        val infraction = Infraction(
            userId = user.id,
            userName = user.asTag,
            guildId = event.guild.id,
            moderatorId = event.author.id,
            type = InfractionType.WARN,
            reason = reason,
            duration = 0,
            ended = true,
            succeeded = true,
            date = System.currentTimeMillis()
        )

        event.message.reply("${Emojis.loading}  Aplicando sanción...").queue({ message ->

            infraction.save()
            if (isNoMd) {
                message.editMessage("${Emojis.success}  Has avisado al usuario ${user.asMention} con la razón: `$reason`")
                    .setAllowedMentions(emptyList())
                    .queue()
            } else {
                if (config.sanctionMessage.isNotEmpty() && config.sanctionMessage.isNotBlank()) {
                    user.openPrivateChannel().queue({ channel ->
                        channel.sendMessage(
                            Formatter.formatSanctionMessage(
                                config.sanctionMessage,
                                infraction,
                                event.guild
                            )
                        )
                            .queue(
                                {
                                    message.editMessage("${Emojis.success}  Has avisado al usuario ${user.asMention} con la razón: `$reason`")
                                        .setAllowedMentions(emptyList())
                                        .queue()
                                }, {
                                    message.editMessage("${Emojis.success}  Has avisado al usuario ${user.asMention} con la razón: `$reason` pero no ha podido ser notificado")
                                        .setAllowedMentions(emptyList())
                                        .queue()
                                })
                    }, {
                        message.editMessage("${Emojis.success}  Has avisado al usuario ${user.asMention} con la razón: `$reason` pero no ha podido ser notificado")
                            .setAllowedMentions(emptyList())
                            .queue()
                    })
                } else {
                    message.editMessage("${Emojis.success}  Has avisado al usuario ${user.asMention} con la razón: `$reason`")
                        .setAllowedMentions(emptyList())
                        .queue()
                }
            }
        }, {})

        InfractionLogger(event.guild, Guild.get(event.guild.id) ?: DefaultConfig.get()).log(infraction)
        return CommandResponse.success()
    }

    override val name: String
        get() = "warn"
    override val description: String
        get() = "Avisar a un usuario con una razón"
    override val aliases: List<String>
        get() = listOf("avisar", "aviso", "avisar")
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
        get() = listOf(Permission.MESSAGE_MANAGE, Permission.MODERATE_MEMBERS)
    override val permissionLevel: Int
        get() = 2
    override val botPermissions: List<Permission>
        get() = listOf(Permission.MESSAGE_EMBED_LINKS)
}