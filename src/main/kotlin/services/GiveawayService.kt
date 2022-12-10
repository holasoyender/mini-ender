package services

import database.schema.Sorteo
import enums.Severity
import net.dv8tion.jda.api.sharding.ShardManager
import org.slf4j.LoggerFactory
import plugins.giveaway.GiveawayManager
import plugins.warnings.WarningsManager

class GiveawayService(shardManager: ShardManager) {

    init {

        val logger = LoggerFactory.getLogger(GiveawayService::class.java)
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

                val allGiveaways = Sorteo.getAll().filter { !it.ended }
                if (allGiveaways.isNotEmpty()) {

                    val now = System.currentTimeMillis()
                    allGiveaways.forEach { giveaway ->

                        val whenToEnd = giveaway.startedAt + giveaway.endAfter
                        if (now >= whenToEnd) {

                            val guild = shardManager.getGuildById(giveaway.guildId)
                            if (guild != null) {

                                val channel = guild.getTextChannelById(giveaway.channelId)
                                if (channel != null) {

                                    channel.retrieveMessageById(giveaway.messageId).queue { message ->

                                        if (message != null) {

                                            channel.retrieveWebhooks().queue { webhooks ->
                                                val webhook = webhooks.firstOrNull { w -> w.name == "Sorteo" }

                                                if (webhook != null)
                                                    GiveawayManager.endGiveaway(webhook, giveaway, null, guild)
                                                else {
                                                    WarningsManager.createWarning(guild, "No se ha encontrado el webhook del sorteo con ID ${giveaway.messageId}, el sorteo ha sido eliminado", Severity.MEDIUM)
                                                    giveaway.delete()
                                                }
                                            }

                                        } else {
                                            WarningsManager.createWarning(guild, "El mensaje del sorteo con ID ${giveaway.messageId} no existe, el sorteo ha sido eliminado", Severity.MEDIUM)
                                            giveaway.delete()
                                        }
                                    }

                                } else {
                                    WarningsManager.createWarning(guild, "El canal ${giveaway.channelId} no existe, el sorteo con ID ${giveaway.messageId} ha sido eliminado", Severity.MEDIUM)
                                    giveaway.delete()
                                }

                            } else giveaway.delete()
                        }
                    }
                }
                Thread.sleep(1 * 60 * 1000)
            }
        }
        thread.isDaemon = true
        thread.name = "GiveawayServiceThread"
        thread.start()
    }
}