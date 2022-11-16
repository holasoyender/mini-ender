package plugins.antilink

import database.schema.Links
import enums.Actions
import enums.Severity
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import plugins.warnings.WarningsManager

object LinkManager {

    fun check(message: Message): Boolean {

        val content = if(message.contentRaw.length > 1024) message.contentRaw.substring(0, 1020) + "..." else message.contentRaw
        val checker = Checker(message.contentRaw)

        return if (checker.isLink || checker.isDiscordInvite) {
            handleFoundLink(content, message.guild, message.channel.id, message, checker)
            true
        } else {
            false
        }
    }

    private fun handleFoundLink(content: String, guild: Guild, channelId: String, message: Message, checker: Checker) {

        if(message.member?.hasPermission(Permission.MESSAGE_MANAGE) == true) return

        val link = Links.get(checker.domain, guild.id)
        if (link != null) {

            if (link.underRevision) {
                try {
                    message.delete().queue()
                } catch (_: Exception) {
                    WarningsManager.createWarning(
                        guild,
                        "El bot no tiene permisos para eliminar mensajes en el canal $channelId",
                        Severity.HIGH
                    )
                }

                try {
                    message.author.openPrivateChannel().queue { channel ->
                        channel.sendMessage("**Hola ${message.author.asMention}! :wave:**\n\nTu mensaje del canal **${channel.asMention}** ha sido eliminado debido a que el link que has enviado se encuentra bajo revisión por parte del equipo de moderación.\n`Si crees que esto es un error, por favor, contacta con un el soporte del servidor.`\n\n```${message.contentStripped}```")
                            .queue()
                    }
                } catch (_: Exception) {
                    // ignore
                }

                link.timesAppeared += 1
                link.save()
                return
            }


            var deletedMessage = true

            if (link.action != Actions.NONE) {
                try {
                    message.delete().queue()
                } catch (_: Exception) {
                    deletedMessage = false
                    WarningsManager.createWarning(
                        guild,
                        "El bot no tiene permisos para eliminar mensajes en el canal $channelId",
                        Severity.HIGH
                    )
                }
            } else {
                deletedMessage = false
            }

            val actionTaken = when (link.action) {
                Actions.BAN -> ActionRouter.ban(message.author, guild, link)
                Actions.KICK -> ActionRouter.kick(message.author, guild, link)
                Actions.MUTE -> ActionRouter.mute(message.author, guild, link)
                Actions.WARN -> ActionRouter.warn(message.author, guild, link)
                Actions.TEMP_BAN -> ActionRouter.tempBan(message.author, guild, link)
                Actions.TEMP_MUTE -> ActionRouter.tempMute(message.author, guild, link)
                Actions.NONE -> false
                Actions.DELETE -> deletedMessage
            }


            val logEmbed = EmbedBuilder()
                .setAuthor("Link detectado", null, message.author.effectiveAvatarUrl)
                .setDescription("He impedido que el usuario ${message.author.asMention} enviara el siguiente mensaje:```$content```\nYa que el dominio `${checker.domain}` está en la lista negra.")
                .addField(
                    "Acción tomada",
                    "`${if (!actionTaken) "No se ha podido ejecutar la acción" else (link.action.name + if (link.duration > 1) " (${link.durationRaw})" else "")}`",
                    true
                )
                .addField(
                    "Mensaje eliminado",
                    "`${if (!deletedMessage) "No se ha eliminado el mensaje" else "Se ha borrado el mensaje"}`",
                    true
                )
                .addField("Canal", "<#$channelId>", true)
                .addField("Usuario", "${message.author.asMention} (${message.author.id})", true)
                .setThumbnail("https://cdn.discordapp.com/attachments/839400943517827092/1038135823650013255/sentinel.png")
                .setColor(0x2f3136)

            val config = database.schema.Guild.get(guild.id) ?: return
            val channel =
                if (config.logChannelId.isNotBlank()) {
                    guild.getTextChannelById(config.logChannelId)
                } else {
                    WarningsManager.createWarning(
                        guild,
                        "El canal de logs no está configurado y es requerido para el sistema de anti-link",
                        Severity.VERY_HIGH
                    )
                    return
                }

            if (channel == null) {
                WarningsManager.createWarning(
                    guild,
                    "El canal de logs no existe o no se ha podido encontrar y es requerido para el sistema de anti-link",
                    Severity.VERY_HIGH
                )
                return
            }

            if(link.action != Actions.NONE)
                channel.sendMessageEmbeds(logEmbed.build()).queue()

        } else {
            val newLink = Links(
                domain = checker.domain,
                guildId = guild.id,
                action = Actions.NONE,
                duration = 0,
                durationRaw = "",
                reason = "",
                moderatorId = "",
                blockedAt = System.currentTimeMillis(),
                timesAppeared = 1,
                underRevision = true
            )

            newLink.save()

            try {
                message.delete().queue()
            } catch (_: Exception) {
                WarningsManager.createWarning(
                    guild,
                    "El bot no tiene permisos para eliminar mensajes en el canal $channelId",
                    Severity.HIGH
                )
            }

            try {
                message.author.openPrivateChannel().queue { channel ->
                    channel.sendMessage("**Hola ${message.author.asMention}! :wave:**\n\nTu mensaje del canal **${channel.asMention}** ha sido eliminado debido a que el link que has enviado no se encuentra en la lista de links permitidos\n\nLos moderadores del servidor revisarán este enlace y, en caso de ser aprobado podrás enviarlo otra vez.\n`Si crees que esto es un error, por favor, contacta con un el soporte del servidor.`\n\n```${message.contentStripped}```").queue()
                }
            } catch (_: Exception) {
                // ignore
            }

            val logEmbed = EmbedBuilder()
                .setAuthor("Link desconocido detectado", null, message.author.effectiveAvatarUrl)
                .setDescription("He impedido que el usuario ${message.author.asMention} enviara el siguiente mensaje:```$content```\nYa que el dominio `${checker.domain}` no se encuentra registrado en la base de datos.\n\nLa acción que se tome con los botones será la que se aplique a todos los casos de este dominio.")
                .addField("Canal", "<#$channelId>", true)
                .addField("Usuario", "${message.author.asMention} (${message.author.id})", true)
                .setThumbnail("https://cdn.discordapp.com/attachments/839400943517827092/1038135823650013255/sentinel.png")
                .setColor(0xED4245)

            val config = database.schema.Guild.get(guild.id) ?: return
            val channel =
                if (config.logChannelId.isNotBlank()) {
                    guild.getTextChannelById(config.logChannelId)
                } else {
                    WarningsManager.createWarning(
                        guild,
                        "El canal de logs no está configurado y es requerido para el sistema de anti-link",
                        Severity.VERY_HIGH
                    )
                    return
                }

            if (channel == null) {
                WarningsManager.createWarning(
                    guild,
                    "El canal de logs no existe o no se ha podido encontrar y es requerido para el sistema de anti-link",
                    Severity.VERY_HIGH
                )
                return
            }

            channel.sendMessageEmbeds(logEmbed.build()).addComponents(
                ActionRow.of(
                    Button.success("cmd::links:accept:${checker.domain}", "Aprobar link"),
                    Button.danger("cmd::links:ban:${checker.domain}", "Banear permanentemente"),
                    Button.primary("cmd::links:kick:${checker.domain}", "Expulsar"),
                    Button.secondary("cmd::links:mute:${checker.domain}", "Silenciar permanentemente"),
                ),
                ActionRow.of(
                    Button.primary("cmd::links:warn:${checker.domain}", "Avisar"),
                    Button.danger("cmd::links:tempban:${checker.domain}", "Banear temporalmente"),
                    Button.secondary("cmd::links:tempmute:${checker.domain}", "Silenciar temporalmente"),
                    Button.primary("cmd::links:delete:${checker.domain}", "Eliminar mensaje")
                )
            ).queue()

        }
    }
}