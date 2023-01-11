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

    fun check(message: Message, config: database.schema.Guild): Boolean {

        val content =
            if (message.contentRaw.length > 1024) message.contentRaw.substring(0, 1020) + "..." else message.contentRaw
        val checker = Checker(message.contentRaw)

        return if (checker.isLink || checker.isDiscordInvite) {

            var silent = false
            if (!message.member!!.roles.map { it.id }.contains("703321891833774090")) {
                message.delete().queue()
                message.member!!.user.openPrivateChannel().queue({
                    it.sendMessage("**¡Hey ${message.member!!.user.asTag}!** <:ibaiSonrisa:899666729452572723>\n" +
                            "Parece que has intentado enviar un link por el canal **${message.channel.name}**, pero solo los usuarios de más de nivel 10 pueden enviar links!\n" +
                            "Puedes subir de nivel participando activamente en el servidor enviando mensajes a los canales de texto\n" +
                            "¿Por que no pruebas a mandar un mensaje por el <#701444109667270660>? <:ibaiWillynice:899666727179280465>"
                    ).queue({}, {})
                }, {})

                silent = true
            }

            if (config.antiLinksAllowedLinks.isNotEmpty()) {
                val allowedLinks = config.antiLinksAllowedLinks.map { it.lowercase() }
                if (allowedLinks.any {
                        val link = checker.link.lowercase()
                            .replace("https://", "")
                            .replace("http://", "")
                            .split("?")[0]

                        link.contains(it)
                    }) {
                    return false
                }
            }

            handleFoundLink(content, message.guild, message.channel.id, message, checker, silent)
            true
        } else {
            false
        }
    }

    private fun handleFoundLink(
        content: String,
        guild: Guild,
        channelId: String,
        message: Message,
        checker: Checker,
        silent: Boolean
    ) {

        if (message.member?.hasPermission(Permission.MESSAGE_MANAGE) == true) return

        val link = Links.get(checker.domain, guild.id)
        if (link != null) {

            if (link.underRevision) {
                try {
                    message.delete().queue({}, {})
                } catch (_: Exception) {
                    WarningsManager.createWarning(
                        guild,
                        "El bot no tiene permisos para eliminar mensajes en el canal $channelId",
                        Severity.HIGH
                    )
                }

                if (!silent)
                    try {
                        message.author.openPrivateChannel().queue({ channel ->
                            channel.sendMessage("Tu mensaje del canal **${message.channel.asMention}** ha sido borrado debido a que el link que has enviado se encuentra bajo revisión por parte del equipo de moderación.\n`Si crees que esto es un error, por favor, contacta con un el soporte del servidor.`\n\n```${message.contentStripped}```")
                                .queue({}, {})
                        }, {})
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
                    message.delete().queue({}, {})
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
                .addField("Moderador que añadió el dominio", "<@${link.moderatorId}> (${link.moderatorId})", true)
                .setThumbnail("https://cdn.discordapp.com/attachments/839400943517827092/1038135823650013255/sentinel.png")
                .setColor(0x2f3136)

            val config = database.schema.Guild.get(guild.id) ?: return
            val channel =
                if (config.moderationLogsChannelId.isNotBlank()) {
                    guild.getTextChannelById(config.moderationLogsChannelId)
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

            if (link.action != Actions.NONE)
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
                message.delete().queue({}, {})
            } catch (_: Exception) {
                WarningsManager.createWarning(
                    guild,
                    "El bot no tiene permisos para eliminar mensajes en el canal $channelId",
                    Severity.HIGH
                )
            }

            if (!silent)
                try {
                    message.author.openPrivateChannel().queue({ channel ->
                        channel.sendMessage("Tu mensaje del canal **${message.channel.asMention}** ha sido borrado debido a que el link que contiene el mensaje no está registrado en nuestra lista de links\n\nSe ha enviado el link a los moderadores del servidor para que lo revisen. En caso de que se apruebe podrás enviarlo otra vez..\n`Si crees que ha sido un fallo o quieres reclamar, contacto con` <@835642946962718731> (**Soporte Kena#8961**) \n\n```${message.contentStripped}```")
                            .queue({}, {})
                    }, {})
                } catch (_: Exception) {
                    // ignore
                }

            val logEmbed = EmbedBuilder()
                .setAuthor("Link desconocido detectado", null, message.author.effectiveAvatarUrl)
                .setDescription("He eliminado el siguiente mensaje:```$content```\nYa que el dominio `${checker.domain}` no se encuentra registrado en la base de datos.\n\nLa acción que se tome con los botones será la que se aplique a todos los casos de este dominio.")
                .addField("Canal", "<#$channelId>", true)
                //.addField("Usuario", "${message.author.asMention} (${message.author.id})", true)
                .setThumbnail("https://cdn.discordapp.com/attachments/839400943517827092/1038135823650013255/sentinel.png")
                .setColor(0xED4245)

            val config = database.schema.Guild.get(guild.id) ?: return
            val channel =
                if (config.antiLinksChannelId.isNotBlank()) {
                    guild.getTextChannelById(config.antiLinksChannelId)
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