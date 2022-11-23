package events

import managers.SlashCommandManager
import net.dv8tion.jda.api.events.guild.GuildAvailableEvent
import net.dv8tion.jda.api.events.guild.GuildUnavailableEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.events.session.ShutdownEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.slf4j.LoggerFactory

class ShardHandler: ListenerAdapter() {

    private val logger = LoggerFactory.getLogger(ShardHandler::class.java)

    override fun onReady(event: ReadyEvent) {
        logger.info("Shard ${event.jda.shardInfo.shardId} lanzada!")

        val allCommands: MutableList<SlashCommandData> = mutableListOf()
        SlashCommandManager().getCommands().forEach {
            allCommands.add(it.metadata ?: Commands.slash(it.name, it.description))
        }

        event.jda.updateCommands().addCommands(allCommands).queue()

    }

    override fun onShutdown(event: ShutdownEvent) {
        logger.info("Shard ${event.jda.shardInfo.shardId} apagada!")
    }

    override fun onGuildAvailable(event: GuildAvailableEvent) {
        logger.info("Servidor ${event.guild.name} disponible")
    }

    override fun onGuildUnavailable(event: GuildUnavailableEvent) {
        logger.info("Servidor ${event.guild.name} no disponible")
    }
}

