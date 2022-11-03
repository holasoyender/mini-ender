package services

import net.dv8tion.jda.api.sharding.ShardManager

//import config.Env

object ServiceManager {

    fun load (shardManager: ShardManager) {
        database.Postgres.load()
        GiveawayService(shardManager)
        //ws.WebSocket.load("mini-ender", Env.TOKEN!!)
    }
}