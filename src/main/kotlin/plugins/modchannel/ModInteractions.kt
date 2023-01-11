package plugins.modchannel

import database.schema.Infraction
import enums.InfractionType
import enums.Severity
import net.dv8tion.jda.api.entities.UserSnowflake
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import plugins.warnings.WarningsManager
import utils.Emojis
import utils.Emojis.f
import utils.Time
import java.util.concurrent.TimeUnit

object ModInteractions {

    fun handleMuteButton(event: ButtonInteractionEvent) {

        if(event.message.member?.roles?.map { it.id }?.contains("735191377184292935") == false) {
            event.reply("No puedes usar este botón").setEphemeral(true).queue()
            return
        }

        val parsed = ModChannel.parse(event.message)
        if (parsed.users.isEmpty()) {
            event.reply("${f(Emojis.error)} No he encontrado ningún usuario en ese mensaje!").setEphemeral(true).queue()
            return
        }

        val reason = parsed.reason
        val duration = Time.ms(parsed.duration)

        val config = database.schema.Guild.get(event.guild!!.id) ?: return

        if(config.muteRoleId.isEmpty() || config.muteRoleId == "") {
            event.reply("${f(Emojis.error)} No hay rol de mute configurado en este servidor!").setEphemeral(true).queue()
            return
        }

        for (user in parsed.users) {

            try {
                val muteRole =
                    event.guild!!.getRoleById(config.muteRoleId) ?: throw Exception("El rol de mute no es valido")
                event.guild!!.addRoleToMember(UserSnowflake.fromId(user), muteRole).reason("Sistema de anti-links").queue({ run {} },
                    {
                        WarningsManager.createWarning(
                            event.guild!!,
                            "No se pudo silenciar a \"${user}\" en el canal de moderación",
                            Severity.MEDIUM
                        )
                    })
            } catch (e: Exception) {
                WarningsManager.createWarning(
                    event.guild!!,
                    "El servidor no tiene configurado el rol de mute o no es valido",
                    Severity.HIGH
                )
            }

            val completeUser = try {
                event.jda.retrieveUserById(user).complete() ?: null
            } catch (e: Exception) {
                null
            }

            completeUser?.openPrivateChannel()?.queue ({ channel ->
                channel.sendMessage("${Emojis.warning}  Has sido silenciado del servidor **${event.guild!!.name}** con la razón: `$reason`")
                    .queue({}, {})
            }, {})

            val infraction = Infraction(
                user,
                completeUser?.asTag ?: "unknown#0000",
                event.guild!!.id,
                event.guild!!.selfMember.user.id,
                if (duration == 0L) InfractionType.MUTE else InfractionType.TEMP_MUTE,
                reason,
                duration,
                duration == 0L,
                true,
                System.currentTimeMillis()
            )
            infraction.save()
        }

        event.editComponents(
            ActionRow.of(
                Button.secondary(
                    "cmd::mod:ban",
                    "Banear" + if (parsed.duration != "0") " (${parsed.duration})" else " (Permanente)"
                ).asDisabled(),
                Button.success(
                    "cmd::mod:mute",
                    "Mutear" + if (parsed.duration != "0") " (${parsed.duration})" else " (Permanente)"
                ).asDisabled(),
                Button.secondary("cmd::empty", "${parsed.users.size} Usuario" + if(parsed.users.size != 1) "s" else "").asDisabled()
            )
        ).queue()
    }

    fun handleBanButton(event: ButtonInteractionEvent) {

        if(event.message.member?.roles?.map { it.id }?.contains("735191377184292935") == false) {
            event.reply("No puedes usar este botón").setEphemeral(true).queue()
            return
        }

        val parsed = ModChannel.parse(event.message)
        if (parsed.users.isEmpty()) {
            event.reply("${f(Emojis.error)} No he encontrado ningún usuario en ese mensaje!").setEphemeral(true).queue()
            return
        }

        val reason = parsed.reason
        val duration = Time.ms(parsed.duration)

        for (user in parsed.users) {
            event.guild!!.ban(UserSnowflake.fromId(user), 0, TimeUnit.DAYS).reason(reason).queue({}, {
                WarningsManager.createWarning(
                    event.guild!!,
                    "No se pudo banear a \"${user}\" en el canal de moderación",
                    Severity.MEDIUM
                )
            })

            val completeUser = try {
                event.jda.retrieveUserById(user).complete() ?: null
            } catch (e: Exception) {
                null
            }

            completeUser?.openPrivateChannel()?.queue ({ channel ->
                channel.sendMessage("${Emojis.warning}  Has sido baneado del servidor **${event.guild!!.name}** con la razón: `$reason`")
                    .queue({}, {})
            }, {})

            val infraction = Infraction(
                user,
                completeUser?.asTag ?: "unknown#0000",
                event.guild!!.id,
                event.guild!!.selfMember.user.id,
                if (duration == 0L) InfractionType.BAN else InfractionType.TEMP_BAN,
                reason,
                duration,
                duration == 0L,
                true,
                System.currentTimeMillis()
            )
            infraction.save()
        }

        event.editComponents(
            ActionRow.of(
                Button.success(
                    "cmd::mod:ban",
                    "Banear" + if (parsed.duration != "0") " (${parsed.duration})" else " (Permanente)"
                ).asDisabled(),
                Button.secondary(
                    "cmd::mod:mute",
                    "Mutear" + if (parsed.duration != "0") " (${parsed.duration})" else " (Permanente)"
                ).asDisabled(),
                Button.secondary("cmd::empty", "${parsed.users.size} Usuario" + if(parsed.users.size != 1) "s" else "").asDisabled()
            )
        ).queue()
    }

}