package events

import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.ShutdownEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.LoggerFactory

class ShardHandler: ListenerAdapter() {

    private val logger = LoggerFactory.getLogger(ShardHandler::class.java)

    override fun onReady(event: ReadyEvent) {
        logger.info("Shard ${event.jda.shardInfo.shardId} lanzada!")
    }

    override fun onShutdown(event: ShutdownEvent) {
        logger.info("Shard ${event.jda.shardInfo.shardId} apagada!")
    }
}