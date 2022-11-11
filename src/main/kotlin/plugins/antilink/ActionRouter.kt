package plugins.antilink

import database.schema.Infraction
import database.schema.Links
import enums.InfractionType
import enums.Severity
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import plugins.warnings.WarningsManager
import java.util.concurrent.TimeUnit

object ActionRouter {

    fun ban(user: User, guild: Guild, link: Links): Boolean {

        val infraction = Infraction(
            user.id,
            user.asTag,
            guild.id,
            guild.selfMember.user.id,
            InfractionType.BAN,
            "Sistema de anti-links",
            0,
            true
        )

        return try {
            guild.ban(user, 60, TimeUnit.SECONDS).reason("Sistema de anti-links").queue()
            infraction.save()

            try {
                user.openPrivateChannel().queue { channel ->
                    channel.sendMessage("**Hola ${user.asMention}! :wave:**\n\nHas sido baneado del servidor **${guild.name}** debido a que has enviado un link que ha sido considerado como spam.\n`Si crees que esto es un error, por favor, contacta con un el soporte del servidor.`\nDominio identificado como spam:```${link.domain}```")
                        .queue()
                }
            } catch (_: Exception) {
                // ignore
            }
            true
        } catch (e: Exception) {
            infraction.succeeded = false
            infraction.save()
            WarningsManager.createWarning(
                guild,
                "No se pudo banear a ${user.asTag} por el sistema de anti-links",
                Severity.MEDIUM
            )
            false
        }
    }

    fun kick(user: User, guild: Guild, link: Links): Boolean {

        val infraction = Infraction(
            user.id,
            user.asTag,
            guild.id,
            guild.selfMember.user.id,
            InfractionType.KICK,
            "Sistema de anti-links",
            0,
            true
        )

        return try {
            guild.kick(user).reason("Sistema de anti-links").queue()
            infraction.save()

            try {
                user.openPrivateChannel().queue { channel ->
                    channel.sendMessage("**Hola ${user.asMention}! :wave:**\n\nHas sido expulsado del servidor **${guild.name}** debido a que has enviado un link que ha sido considerado como spam.\n`Si crees que esto es un error, por favor, contacta con un el soporte del servidor.`\nDominio identificado como spam:```${link.domain}```")
                        .queue()
                }
            } catch (_: Exception) {
                // ignore
            }
            true
        } catch (e: Exception) {
            infraction.succeeded = false
            infraction.save()
            WarningsManager.createWarning(
                guild,
                "No se pudo kickear a ${user.asTag} por el sistema de anti-links",
                Severity.MEDIUM
            )
            false
        }
    }

    fun mute(user: User, guild: Guild, link: Links): Boolean {

        val infraction = Infraction(
            user.id,
            user.asTag,
            guild.id,
            guild.selfMember.user.id,
            InfractionType.MUTE,
            "Sistema de anti-links",
            0,
            true
        )

        return try {
            val config = database.schema.Guild.get(guild.id)

            if (config == null)
                WarningsManager.createWarning(
                    guild,
                    "El servidor no tiene configurado el rol de mute o no es valido",
                    Severity.HIGH
                )
            else {
                try {
                    val muteRole =
                        guild.getRoleById(config.muteRoleId) ?: throw Exception("El rol de mute no es valido")
                    guild.addRoleToMember(user, muteRole).reason("Sistema de anti-links").queue()
                } catch (e: Exception) {
                    WarningsManager.createWarning(
                        guild,
                        "El servidor no tiene configurado el rol de mute o no es valido",
                        Severity.HIGH
                    )
                }
            }
            infraction.save()

            try {
                user.openPrivateChannel().queue { channel ->
                    channel.sendMessage("**Hola ${user.asMention}! :wave:**\n\nHas sido silenciado en el servidor **${guild.name}** debido a que has enviado un link que ha sido considerado como spam.\n`Si crees que esto es un error, por favor, contacta con un el soporte del servidor.`\nDominio identificado como spam:```${link.domain}```")
                        .queue()
                }
            } catch (_: Exception) {
                // ignore
            }
            true
        } catch (e: Exception) {
            infraction.succeeded = false
            infraction.save()
            WarningsManager.createWarning(
                guild,
                "No se pudo silenciar a ${user.asTag} por el sistema de anti-links",
                Severity.MEDIUM
            )
            false
        }

    }

    fun warn(user: User, guild: Guild, link: Links): Boolean {

        val infraction = Infraction(
            user.id,
            user.asTag,
            guild.id,
            guild.selfMember.user.id,
            InfractionType.WARN,
            "Sistema de anti-links",
            0,
            true
        )

        infraction.save()

        try {
            user.openPrivateChannel().queue { channel ->
                channel.sendMessage("**Hola ${user.asMention}! :wave:**\n\nHas sido advertido en el servidor **${guild.name}** debido a que has enviado un link que ha sido considerado como spam.\n`Si crees que esto es un error, por favor, contacta con un el soporte del servidor.`\nDominio identificado como spam:```${link.domain}```")
                    .queue()
            }
        } catch (_: Exception) {
            // ignore
        }

        return true

    }

    fun tempBan(user: User, guild: Guild, link: Links): Boolean {

        val infraction = Infraction(
            user.id,
            user.asTag,
            guild.id,
            guild.selfMember.user.id,
            InfractionType.TEMP_BAN,
            "Sistema de anti-links",
            link.duration,
            true
        )

        return try {
            guild.ban(user, 60, TimeUnit.SECONDS).reason("Sistema de anti-links").queue()
            infraction.save()

            try {
                user.openPrivateChannel().queue { channel ->
                    channel.sendMessage("**Hola ${user.asMention}! :wave:**\n\nHas sido baneado temporalmente del servidor **${guild.name}** debido a que has enviado un link que ha sido considerado como spam.\n`Si crees que esto es un error, por favor, contacta con un el soporte del servidor.`\nDominio identificado como spam:```${link.domain}```")
                        .queue()
                }
            } catch (_: Exception) {
                // ignore
            }
            true
        } catch (e: Exception) {
            infraction.succeeded = false
            infraction.save()
            WarningsManager.createWarning(
                guild,
                "No se pudo banear a ${user.asTag} por el sistema de anti-links",
                Severity.MEDIUM
            )
            false
        }

    }

    fun tempMute(user: User, guild: Guild, link: Links): Boolean {

        val infraction = Infraction(
            user.id,
            user.asTag,
            guild.id,
            guild.selfMember.user.id,
            InfractionType.TEMP_MUTE,
            "Sistema de anti-links",
            link.duration,
            true
        )

        return try {

            if (link.duration >= 7 * 24 * 60 * 60 * 1000) {

                val config = database.schema.Guild.get(guild.id)

                if (config == null)
                    WarningsManager.createWarning(
                        guild,
                        "El servidor no tiene configurado el rol de mute o no es valido",
                        Severity.HIGH
                    )
                else {
                    try {
                        val muteRole =
                            guild.getRoleById(config.muteRoleId) ?: throw Exception("El rol de mute no es valido")
                        guild.addRoleToMember(user, muteRole).reason("Sistema de anti-links").queue()
                    } catch (e: Exception) {
                        WarningsManager.createWarning(
                            guild,
                            "El servidor no tiene configurado el rol de mute o no es valido",
                            Severity.HIGH
                        )
                    }
                }

            } else {
                guild.timeoutFor(user, link.duration, TimeUnit.MILLISECONDS).reason("Sistema de anti-links").queue()
            }

            infraction.save()

            try {
                user.openPrivateChannel().queue { channel ->
                    channel.sendMessage("**Hola ${user.asMention}! :wave:**\n\nHas sido silenciado temporalmente en el servidor **${guild.name}** debido a que has enviado un link que ha sido considerado como spam.\n`Si crees que esto es un error, por favor, contacta con un el soporte del servidor.`\nDominio identificado como spam:```${link.domain}```")
                        .queue()
                }
            } catch (_: Exception) {
                // ignore
            }
            true
        } catch (e: Exception) {
            infraction.succeeded = false
            infraction.save()
            WarningsManager.createWarning(
                guild,
                "No se pudo silenciar temporalmente a ${user.asTag} por el sistema de anti-links",
                Severity.MEDIUM
            )
            false
        }

    }
}