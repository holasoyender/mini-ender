package services

import database.schema.Guild
import database.schema.Infraction
import enums.InfractionType
import net.dv8tion.jda.api.entities.UserSnowflake
import net.dv8tion.jda.api.sharding.ShardManager
import org.slf4j.LoggerFactory

class InfractionsService(shardManager: ShardManager) {

    init {

        val logger = LoggerFactory.getLogger(InfractionsService::class.java)
        val thread = Thread {
            while (true) {

                var shouldCheck = true

                shardManager.shards.forEach {
                    if (it.status != net.dv8tion.jda.api.JDA.Status.CONNECTED) {
                        shouldCheck = false
                    }
                }

                if (!shouldCheck) {
                    logger.info("Not all shards are connected, skipping for now...")
                    Thread.sleep(1000 * 10)
                    continue
                }

                val allInfractions = Infraction.getAll(false)
                if (allInfractions.isNotEmpty()) {

                    val now = System.currentTimeMillis()
                    allInfractions.forEach { infraction ->

                        val endTime = infraction.date + infraction.duration
                        if (now >= endTime) {

                            val guild = shardManager.getGuildById(infraction.guildId)
                            if (guild != null) {

                                if (infraction.type == InfractionType.TEMP_MUTE) {

                                    if (infraction.duration >= 7 * 24 * 60 * 60 * 1000) {
                                        try {
                                            val member = guild.getMemberById(infraction.userId)
                                                ?: throw Exception("No se ha encontrado el usuario con ID ${infraction.userId}")

                                            val config = Guild.get(guild.id)
                                                ?: throw Exception("No se ha encontrado la configuración del servidor con ID ${guild.id}")
                                            val mutedRole = guild.getRoleById(config.muteRoleId)
                                                ?: throw Exception("No se ha encontrado el rol de silenciado con ID ${config.muteRoleId}")

                                            guild.removeRoleFromMember(member, mutedRole).queue({ }, { })

                                            infraction.ended = true
                                            infraction.save()
                                        } catch (e: Exception) {
                                            infraction.ended = true
                                            infraction.save()
                                        }
                                    } else {
                                        infraction.ended = true
                                        infraction.save()
                                    }
                                }

                                if (infraction.type == InfractionType.TEMP_BAN) {

                                    try {
                                        val user = UserSnowflake.fromId(infraction.userId)
                                        guild.retrieveBan(user).queue({

                                            guild.unban(user).queue({ }, { })
                                            infraction.ended = true
                                            infraction.save()

                                        }, {

                                            infraction.ended = true
                                            infraction.save()

                                        })
                                    } catch (e: Exception) {
                                        infraction.ended = true
                                        infraction.save()
                                    }

                                }

                            } else {
                                infraction.ended = true
                                infraction.save()
                            }
                        }
                    }
                }
                Thread.sleep(3 * 60 * 1000)
            }
        }
        thread.isDaemon = true
        thread.name = "InfractionsServiceThread"
        thread.start()
    }
}