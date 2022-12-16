package plugins.antilink

import database.schema.Links
import enums.Actions
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.dv8tion.jda.api.interactions.modals.Modal
import utils.Emojis
import utils.Emojis.f
import utils.Time

object LinksInteractions {

    fun handleAcceptButton(event: ButtonInteractionEvent) {

        disableAllButtons(event, Actions.NONE)
        val domain = event.componentId.split("::")[1].split(":")[2]

        val link = Links.get(domain, event.guild!!.id)
        if (link != null) {
            event.reply("${Emojis.success}  El dominio `${domain}` ha sido aceptado y no será bloqueado").setEphemeral(true).queue()

            link.underRevision = false
            link.action = Actions.NONE
            link.blockedAt = System.currentTimeMillis()
            link.moderatorId = event.user.id
            link.reason = "Aceptado por el moderador ${event.user.asTag}"
            link.save()
        } else {
            event.reply("${f(Emojis.error)}  El dominio `${domain}` no existe en la base de datos").setEphemeral(true).queue()
        }

    }

    fun handleBanButton(event: ButtonInteractionEvent) {

        disableAllButtons(event, Actions.BAN)
        val domain = event.componentId.split("::")[1].split(":")[2]

        val link = Links.get(domain, event.guild!!.id)
        if (link != null) {
            event.reply("${Emojis.success}  El dominio `${domain}` ha sido marcado con la acción `BAN` y será bloqueado").setEphemeral(true).queue()

            link.underRevision = false
            link.action = Actions.BAN
            link.blockedAt = System.currentTimeMillis()
            link.moderatorId = event.user.id
            link.reason = "Marcado como BAN por el moderador ${event.user.asTag}"
            link.save()
        } else {
            event.reply("${f(Emojis.error)}  El dominio `${domain}` no existe en la base de datos").setEphemeral(true).queue()
        }
    }

    fun handleKickButton(event: ButtonInteractionEvent) {

        disableAllButtons(event, Actions.KICK)
        val domain = event.componentId.split("::")[1].split(":")[2]

        val link = Links.get(domain, event.guild!!.id)
        if (link != null) {
            event.reply("${Emojis.success}  El dominio `${domain}` ha sido marcado con la acción `KICK` y será bloqueado").setEphemeral(true).queue()

            link.underRevision = false
            link.action = Actions.KICK
            link.blockedAt = System.currentTimeMillis()
            link.moderatorId = event.user.id
            link.reason = "Marcado como KICK por el moderador ${event.user.asTag}"
            link.save()
        } else {
            event.reply("${f(Emojis.error)}  El dominio `${domain}` no existe en la base de datos").setEphemeral(true).queue()
        }
    }

    fun handleMuteButton(event: ButtonInteractionEvent) {

        disableAllButtons(event, Actions.MUTE)
        val domain = event.componentId.split("::")[1].split(":")[2]

        val link = Links.get(domain, event.guild!!.id)
        if (link != null) {
            event.reply("${Emojis.success}  El dominio `${domain}` ha sido marcado con la acción `MUTE` y será bloqueado").setEphemeral(true).queue()

            link.underRevision = false
            link.action = Actions.MUTE
            link.blockedAt = System.currentTimeMillis()
            link.moderatorId = event.user.id
            link.reason = "Marcado como MUTE por el moderador ${event.user.asTag}"
            link.save()
        } else {
            event.reply("${f(Emojis.error)}  El dominio `${domain}` no existe en la base de datos").setEphemeral(true).queue()
        }
    }

    fun handleWarnButton(event: ButtonInteractionEvent) {

        disableAllButtons(event, Actions.WARN)
        val domain = event.componentId.split("::")[1].split(":")[2]

        val link = Links.get(domain, event.guild!!.id)
        if (link != null) {
            event.reply("${Emojis.success}  El dominio `${domain}` ha sido marcado con la acción `WARN` y será bloqueado").setEphemeral(true).queue()

            link.underRevision = false
            link.action = Actions.WARN
            link.blockedAt = System.currentTimeMillis()
            link.moderatorId = event.user.id
            link.reason = "Marcado como WARN por el moderador ${event.user.asTag}"
            link.save()
        } else {
            event.reply("${f(Emojis.error)}  El dominio `${domain}` no existe en la base de datos").setEphemeral(true).queue()
        }
    }

    fun handleTempBanButton(event: ButtonInteractionEvent) {

        val domain = event.componentId.split("::")[1].split(":")[2]
        val body: TextInput = TextInput.create("body", "Duración de la sanción", TextInputStyle.SHORT)
            .setPlaceholder("Cuando debe de durar el baneo, ej: 1d, 6h, 30m, etc....")
            .setMinLength(1)
            .setMaxLength(10)
            .build()

        val modal: Modal = Modal.create("links.tempban::${domain}", "Duración de la sanción")
            .addActionRows(ActionRow.of(body))
            .build()

        event.replyModal(modal).queue()
    }

    fun handleTempBanModal(event: ModalInteractionEvent) {

        val duration = event.getValue("body")?.asString ?: return event.reply("No has escrito nada!").setEphemeral(true).queue()

        val formattedTime = Time.ms(duration)
        if(formattedTime < 1) return event.reply("${f(Emojis.error)}  La duración no es válida!").setEphemeral(true).queue()

        val domain = event.modalId.split("::")[1]
        val link = Links.get(domain, event.guild!!.id)
        if (link != null) {
            link.underRevision = false
            link.action = Actions.TEMP_BAN
            link.blockedAt = System.currentTimeMillis()
            link.moderatorId = event.user.id
            link.reason = "Marcado como TEMPBAN por el moderador ${event.user.asTag}"
            link.duration = formattedTime
            link.durationRaw = duration
            link.save()

            disableAllButtons(event, Actions.TEMP_BAN)

            event.reply("${Emojis.success}  El dominio `${domain}` ha sido marcado con la acción `TEMPBAN` y será bloqueado").setEphemeral(true).queue()
        } else {
            event.reply("${f(Emojis.error)}  El dominio `${domain}` no existe en la base de datos").setEphemeral(true).queue()
        }
    }

    fun handleTempMuteButton(event: ButtonInteractionEvent) {

        val domain = event.componentId.split("::")[1].split(":")[2]
        val body: TextInput = TextInput.create("body", "Duración de la sanción", TextInputStyle.SHORT)
            .setPlaceholder("Cuando debe de durar el muteo, ej: 1d, 6h, 30m, etc....")
            .setMinLength(1)
            .setMaxLength(10)
            .build()

        val modal: Modal = Modal.create("links.tempmute::${domain}", "Duración de la sanción")
            .addActionRows(ActionRow.of(body))
            .build()

        event.replyModal(modal).queue()
    }

    fun handleTempMuteModal(event: ModalInteractionEvent) {

        val duration = event.getValue("body")?.asString ?: return event.reply("No has escrito nada!").setEphemeral(true).queue()

        val formattedTime = Time.ms(duration)
        if(formattedTime < 1) return event.reply("${f(Emojis.error)}  La duración no es válida!").setEphemeral(true).queue()

        val domain = event.modalId.split("::")[1]
        val link = Links.get(domain, event.guild!!.id)
        if (link != null) {
            link.underRevision = false
            link.action = Actions.TEMP_MUTE
            link.blockedAt = System.currentTimeMillis()
            link.moderatorId = event.user.id
            link.reason = "Marcado como TEMPMUTE por el moderador ${event.user.asTag}"
            link.duration = formattedTime
            link.durationRaw = duration
            link.save()

            disableAllButtons(event, Actions.TEMP_MUTE)

            event.reply("${Emojis.success}  El dominio `${domain}` ha sido marcado con la acción `TEMPMUTE` y será bloqueado").setEphemeral(true).queue()
        } else {
            event.reply("${f(Emojis.error)}  El dominio `${domain}` no existe en la base de datos").setEphemeral(true).queue()
        }
    }

    fun handleDeleteButton(event: ButtonInteractionEvent) {

        disableAllButtons(event, Actions.DELETE)
        val domain = event.componentId.split("::")[1].split(":")[2]

        val link = Links.get(domain, event.guild!!.id)
        if (link != null) {
            event.reply("${Emojis.success}  El dominio `${domain}` ha sido marcado con la acción `DELETE` y será borrado").setEphemeral(true).queue()

            link.underRevision = false
            link.action = Actions.DELETE
            link.blockedAt = System.currentTimeMillis()
            link.moderatorId = event.user.id
            link.reason = "Marcado como DELETE por el moderador ${event.user.asTag}"
            link.save()
        } else {
            event.reply("${f(Emojis.error)}  El dominio `${domain}` no existe en la base de datos").setEphemeral(true).queue()
        }
    }

    fun handleDeleteLinkButton(event: ButtonInteractionEvent) {

        val domain = event.componentId.split("::")[1].split(":")[2]

        val link = Links.get(domain, event.guild!!.id)
        if (link != null) {
            link.delete()

            event.editMessage("${Emojis.success}  El dominio `${domain}` ha sido eliminado de la base de datos")
                .setComponents()
                .setEmbeds()
                .queue()

        } else {
            event.reply("${f(Emojis.error)}  El dominio `${domain}` no existe en la base de datos").setEphemeral(true).queue()
        }
    }

    fun handleEditButton(event: ButtonInteractionEvent) {

        val domain = event.componentId.split("::")[1].split(":")[2]

        val link = Links.get(domain, event.guild!!.id)
        if (link != null) {

            event
                .editMessage("${Emojis.right}  Elige la acción que deseas realizar con el dominio `${domain}`")
                .setEmbeds()
                .setComponents(
                ActionRow.of(
                    Button.success("cmd::links:accept:${link.domain}", "Aprobar link"),
                    Button.danger("cmd::links:ban:${link.domain}", "Banear permanentemente"),
                    Button.primary("cmd::links:kick:${link.domain}", "Expulsar"),
                    Button.secondary("cmd::links:mute:${link.domain}", "Silenciar permanentemente"),
                ),
                ActionRow.of(
                    Button.primary("cmd::links:warn:${link.domain}", "Avisar"),
                    Button.danger("cmd::links:tempban:${link.domain}", "Banear temporalmente"),
                    Button.secondary("cmd::links:tempmute:${link.domain}", "Silenciar temporalmente"),
                    Button.primary("cmd::links:delete:${link.domain}", "Eliminar mensaje")
                )
            ).queue()

        } else {
            event.reply("${f(Emojis.error)}  El dominio `${domain}` no existe en la base de datos").setEphemeral(true).queue()
        }
    }

    private fun disableAllButtons(event: ButtonInteractionEvent, type: Actions) {

        event.message.editMessageComponents(
            ActionRow.of(
                Button.of(if(type == Actions.NONE) ButtonStyle.PRIMARY else ButtonStyle.SECONDARY, "cmd::links:accept:0", "Aprobar link").asDisabled(),
                Button.of(if(type == Actions.BAN) ButtonStyle.PRIMARY else ButtonStyle.SECONDARY,"cmd::links:ban:0", "Banear permanentemente").asDisabled(),
                Button.of(if(type == Actions.KICK) ButtonStyle.PRIMARY else ButtonStyle.SECONDARY,"cmd::links:kick:0", "Expulsar").asDisabled(),
                Button.of(if(type == Actions.MUTE) ButtonStyle.PRIMARY else ButtonStyle.SECONDARY,"cmd::links:mute:0", "Silenciar permanentemente").asDisabled(),
            ),
            ActionRow.of(
                Button.of(if(type == Actions.WARN) ButtonStyle.PRIMARY else ButtonStyle.SECONDARY,"cmd::links:warn:0", "Avisar").asDisabled(),
                Button.of(if(type == Actions.TEMP_BAN) ButtonStyle.PRIMARY else ButtonStyle.SECONDARY,"cmd::links:tempban:0", "Banear temporalmente").asDisabled(),
                Button.of(if(type == Actions.TEMP_MUTE) ButtonStyle.PRIMARY else ButtonStyle.SECONDARY,"cmd::links:tempmute:0", "Silenciar temporalmente").asDisabled(),
                Button.of(if(type == Actions.DELETE) ButtonStyle.PRIMARY else ButtonStyle.SECONDARY,"cmd::links:delete:0", "Eliminar mensaje").asDisabled()
            )
        ).queue()
    }

    private fun disableAllButtons(event: ModalInteractionEvent, type: Actions) {

        event.message?.editMessageComponents(
            ActionRow.of(
                Button.of(if(type == Actions.NONE) ButtonStyle.PRIMARY else ButtonStyle.SECONDARY, "cmd::links:accept:0", "Aprobar link").asDisabled(),
                Button.of(if(type == Actions.BAN) ButtonStyle.PRIMARY else ButtonStyle.SECONDARY,"cmd::links:ban:0", "Banear permanentemente").asDisabled(),
                Button.of(if(type == Actions.KICK) ButtonStyle.PRIMARY else ButtonStyle.SECONDARY,"cmd::links:kick:0", "Expulsar").asDisabled(),
                Button.of(if(type == Actions.MUTE) ButtonStyle.PRIMARY else ButtonStyle.SECONDARY,"cmd::links:mute:0", "Silenciar permanentemente").asDisabled(),
            ),
            ActionRow.of(
                Button.of(if(type == Actions.WARN) ButtonStyle.PRIMARY else ButtonStyle.SECONDARY,"cmd::links:warn:0", "Avisar").asDisabled(),
                Button.of(if(type == Actions.TEMP_BAN) ButtonStyle.PRIMARY else ButtonStyle.SECONDARY,"cmd::links:tempban:0", "Banear temporalmente").asDisabled(),
                Button.of(if(type == Actions.TEMP_MUTE) ButtonStyle.PRIMARY else ButtonStyle.SECONDARY,"cmd::links:tempmute:0", "Silenciar temporalmente").asDisabled(),
                Button.of(if(type == Actions.DELETE) ButtonStyle.PRIMARY else ButtonStyle.SECONDARY,"cmd::links:delete:0", "Eliminar mensaje").asDisabled()
            )
        )?.queue()
    }
}