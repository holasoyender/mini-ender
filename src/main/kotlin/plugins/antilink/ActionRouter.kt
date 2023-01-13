package plugins.antilink

import database.schema.Infraction
import database.schema.Links
import enums.InfractionType
import enums.Severity
import messages.Formatter
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import plugins.warnings.WarningsManager
import java.util.concurrent.TimeUnit

object ActionRouter {

    fun ban(user: User, guild: Guild, link: Links, config: database.schema.Guild): Boolean {

        var success = true

        if (config.antiLinksSanctionMessage.isNotBlank())
            user.openPrivateChannel().queue { channel ->
                channel.sendMessage(
                    Formatter.formatLinksSanctionMessage(
                        config.antiLinksSanctionMessage,
                        "baneado",
                        guild,
                        user,
                        link.domain
                    )
                )
                    .queue({
                        guild.ban(user, 60, TimeUnit.SECONDS).reason("Sistema de anti-links").queue({ run {} },
                            {
                                success = false
                                WarningsManager.createWarning(
                                    guild,
                                    "No se pudo banear a \"${user.asTag}\" por el sistema de anti-links",
                                    Severity.MEDIUM
                                )
                            })
                    }, {
                        guild.ban(user, 60, TimeUnit.SECONDS).reason("Sistema de anti-links").queue({ run {} },
                            {
                                success = false
                                WarningsManager.createWarning(
                                    guild,
                                    "No se pudo banear a \"${user.asTag}\" por el sistema de anti-links",
                                    Severity.MEDIUM
                                )
                            })
                    })
            }
        else
            guild.ban(user, 60, TimeUnit.SECONDS).reason("Sistema de anti-links").queue({ run {} },
                {
                    success = false
                    WarningsManager.createWarning(
                        guild,
                        "No se pudo banear a \"${user.asTag}\" por el sistema de anti-links",
                        Severity.MEDIUM
                    )
                })

        val infraction = Infraction(
            user.id,
            user.asTag,
            guild.id,
            guild.selfMember.user.id,
            InfractionType.BAN,
            "Sistema de anti-links",
            0,
            true,
            success,
            System.currentTimeMillis()
        )
        infraction.save()

        return success
    }

    fun ban(user: User, guild: Guild, checker: Checker, config: database.schema.Guild): Boolean {

        var success = true

        if (config.antiLinksSanctionMessage.isNotBlank())
            user.openPrivateChannel().queue { channel ->
                channel.sendMessage(
                    Formatter.formatLinksSanctionMessage(
                        config.antiLinksSanctionMessage,
                        "baneado",
                        guild,
                        user,
                        checker.domain
                    )
                )
                    .queue({
                        guild.ban(user, 60, TimeUnit.SECONDS).reason("Sistema de anti-phishing").queue({ run {} },
                            {
                                success = false
                                WarningsManager.createWarning(
                                    guild,
                                    "No se pudo banear a \"${user.asTag}\" por el sistema de anti-phishing",
                                    Severity.MEDIUM
                                )
                            })
                    }, {
                        guild.ban(user, 60, TimeUnit.SECONDS).reason("Sistema de anti-phishing").queue({ run {} },
                            {
                                success = false
                                WarningsManager.createWarning(
                                    guild,
                                    "No se pudo banear a \"${user.asTag}\" por el sistema de anti-phishing",
                                    Severity.MEDIUM
                                )
                            })
                    })
            }
        else
            guild.ban(user, 60, TimeUnit.SECONDS).reason("Sistema de anti-phishing").queue({ run {} },
                {
                    success = false
                    WarningsManager.createWarning(
                        guild,
                        "No se pudo banear a \"${user.asTag}\" por el sistema de anti-phishing",
                        Severity.MEDIUM
                    )
                })


        val infraction = Infraction(
            user.id,
            user.asTag,
            guild.id,
            guild.selfMember.user.id,
            InfractionType.BAN,
            "Sistema de anti-phishing",
            0,
            true,
            success,
            System.currentTimeMillis()
        )
        infraction.save()

        return success
    }

    fun kick(user: User, guild: Guild, link: Links, config: database.schema.Guild): Boolean {

        var success = true

        if (config.antiLinksSanctionMessage.isNotBlank())
            user.openPrivateChannel().queue { channel ->
                channel.sendMessage(
                    Formatter.formatLinksSanctionMessage(
                        config.antiLinksSanctionMessage,
                        "expulsado",
                        guild,
                        user,
                        link.domain
                    )
                )
                    .queue({
                        guild.kick(user).reason("Sistema de anti-links").queue({ run {} },
                            {
                                success = false
                                WarningsManager.createWarning(
                                    guild,
                                    "No se pudo expulsar a \"${user.asTag}\" por el sistema de anti-links",
                                    Severity.MEDIUM
                                )
                            })
                    }, {
                        guild.kick(user).reason("Sistema de anti-links").queue({ run {} },
                            {
                                success = false
                                WarningsManager.createWarning(
                                    guild,
                                    "No se pudo expulsar a \"${user.asTag}\" por el sistema de anti-links",
                                    Severity.MEDIUM
                                )
                            })
                    })
            }
        else
            guild.kick(user).reason("Sistema de anti-links").queue({ run {} },
                {
                    success = false
                    WarningsManager.createWarning(
                        guild,
                        "No se pudo expulsar a \"${user.asTag}\" por el sistema de anti-links",
                        Severity.MEDIUM
                    )
                })


        val infraction = Infraction(
            user.id,
            user.asTag,
            guild.id,
            guild.selfMember.user.id,
            InfractionType.KICK,
            "Sistema de anti-links",
            0,
            true,
            success,
            System.currentTimeMillis()
        )
        infraction.save()

        return success
    }

    fun mute(user: User, guild: Guild, link: Links, config: database.schema.Guild): Boolean {

        var success = true

        try {
            val muteRole =
                guild.getRoleById(config.muteRoleId) ?: throw Exception("El rol de mute no es valido")
            guild.addRoleToMember(user, muteRole).reason("Sistema de anti-links").queue({ run {} },
                {
                    success = false
                    WarningsManager.createWarning(
                        guild,
                        "No se pudo silenciar a \"${user.asTag}\" por el sistema de anti-links",
                        Severity.MEDIUM
                    )
                })
        } catch (e: Exception) {
            WarningsManager.createWarning(
                guild,
                "El servidor no tiene configurado el rol de mute o no es valido",
                Severity.HIGH
            )
            success = false
        }

        if (config.antiLinksSanctionMessage.isNotBlank())
            user.openPrivateChannel().queue { channel ->
                channel.sendMessage(
                    Formatter.formatLinksSanctionMessage(
                        config.antiLinksSanctionMessage,
                        "silenciado",
                        guild,
                        user,
                        link.domain
                    )
                )
                    .queue({}, {})
            }

        val infraction = Infraction(
            user.id,
            user.asTag,
            guild.id,
            guild.selfMember.user.id,
            InfractionType.MUTE,
            "Sistema de anti-links",
            0,
            true,
            success,
            System.currentTimeMillis()
        )
        infraction.save()

        return success

    }

    fun warn(user: User, guild: Guild, link: Links, config: database.schema.Guild): Boolean {

        val infraction = Infraction(
            user.id,
            user.asTag,
            guild.id,
            guild.selfMember.user.id,
            InfractionType.WARN,
            "Sistema de anti-links",
            0,
            ended = true,
            succeeded = true,
            System.currentTimeMillis()
        )

        infraction.save()

        if (config.antiLinksSanctionMessage.isNotBlank())
            user.openPrivateChannel().queue { channel ->
                channel.sendMessage(
                    Formatter.formatLinksSanctionMessage(
                        config.antiLinksSanctionMessage,
                        "avisado",
                        guild,
                        user,
                        link.domain
                    )
                )
                    .queue({}, {})
            }

        return true

    }

    fun tempBan(user: User, guild: Guild, link: Links, config: database.schema.Guild): Boolean {

        var success = true

        if (config.antiLinksSanctionMessage.isNotBlank())
            user.openPrivateChannel().queue { channel ->
                channel.sendMessage(
                    Formatter.formatLinksSanctionMessage(
                        config.antiLinksSanctionMessage,
                        "baneado temporalmente",
                        guild,
                        user,
                        link.domain
                    )
                )
                    .queue({
                        guild.ban(user, 60, TimeUnit.SECONDS).reason("Sistema de anti-links").queue({ run {} },
                            {
                                success = false
                                WarningsManager.createWarning(
                                    guild,
                                    "No se pudo banear temporalmente a \"${user.asTag}\" por el sistema de anti-links",
                                    Severity.MEDIUM
                                )
                            })
                    }, {
                        guild.ban(user, 60, TimeUnit.SECONDS).reason("Sistema de anti-links").queue({ run {} },
                            {
                                success = false
                                WarningsManager.createWarning(
                                    guild,
                                    "No se pudo banear temporalmente a \"${user.asTag}\" por el sistema de anti-links",
                                    Severity.MEDIUM
                                )
                            })
                    })
            } else
            guild.ban(user, 60, TimeUnit.SECONDS).reason("Sistema de anti-links").queue({ run {} },
                {
                    success = false
                    WarningsManager.createWarning(
                        guild,
                        "No se pudo banear temporalmente a \"${user.asTag}\" por el sistema de anti-links",
                        Severity.MEDIUM
                    )
                })


        val infraction = Infraction(
            user.id,
            user.asTag,
            guild.id,
            guild.selfMember.user.id,
            InfractionType.TEMP_BAN,
            "Sistema de anti-links",
            link.duration,
            false,
            success,
            System.currentTimeMillis()
        )
        infraction.save()

        return success

    }

    fun tempMute(user: User, guild: Guild, link: Links, config: database.schema.Guild): Boolean {

        var success = true

        try {

            if (link.duration >= 7 * 24 * 60 * 60 * 1000) {

                val muteRole = guild.getRoleById(config.muteRoleId)
                if (muteRole == null) {
                    WarningsManager.createWarning(
                        guild,
                        "El servidor no tiene configurado el rol de mute o no es valido",
                        Severity.HIGH
                    )
                    success = false
                } else {
                    guild.addRoleToMember(user, muteRole).reason("Sistema de anti-links").queue({ run {} },
                        {
                            success = false
                            WarningsManager.createWarning(
                                guild,
                                "No se pudo silenciar a \"${user.asTag}\" por el sistema de anti-links",
                                Severity.MEDIUM
                            )
                        })
                }

            } else {

                guild.timeoutFor(user, link.duration, TimeUnit.MILLISECONDS).reason("Sistema de anti-links")
                    .queue({ run {} },
                        {
                            success = false
                            WarningsManager.createWarning(
                                guild,
                                "No se pudo silenciar temporalmente a \"${user.asTag}\" por el sistema de anti-links",
                                Severity.MEDIUM
                            )
                        })

            }

            if (config.antiLinksSanctionMessage.isNotBlank())
                user.openPrivateChannel().queue { channel ->
                    channel.sendMessage(
                        Formatter.formatLinksSanctionMessage(
                            config.antiLinksSanctionMessage,
                            "silenciado temporalmente",
                            guild,
                            user,
                            link.domain
                        )
                    )
                        .queue({}, {})
                }

        } catch (e: Exception) {
            success = false
            WarningsManager.createWarning(
                guild,
                "No se pudo silenciar temporalmente a \"${user.asTag}\" por el sistema de anti-links",
                Severity.MEDIUM
            )
        }

        val infraction = Infraction(
            user.id,
            user.asTag,
            guild.id,
            guild.selfMember.user.id,
            InfractionType.TEMP_MUTE,
            "Sistema de anti-links",
            link.duration,
            false,
            success,
            System.currentTimeMillis()
        )
        infraction.save()

        return success

    }
}