package services

//import config.Env

object ServiceManager {

    fun load () {
        database.Postgres.load()
        //ws.WebSocket.load("mini-ender", Env.TOKEN!!)
    }
}