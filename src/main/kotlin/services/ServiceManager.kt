package services

import net.dv8tion.jda.api.sharding.ShardManager
import plugins.twitch.TwitchManager
import plugins.twitch.TwitchSubscriptionManager

//import config.Env

object ServiceManager {

    fun load (shardManager: ShardManager) {

        database.Postgres.load()
        database.Redis.load()
        GiveawayService(shardManager)
        InfractionsService(shardManager)
        //ws.WebSocket.load("mini-ender", Env.API_TOKEN!!)
        TwitchSubscriptionManager.auth()

        Thread {
            Thread.sleep(1000 * 5)
            TwitchManager.doChecks(true)
        }.start()

    }
}