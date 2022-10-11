package events

import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.events.session.ShutdownEvent
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