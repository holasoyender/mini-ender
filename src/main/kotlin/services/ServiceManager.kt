package services

object ServiceManager {

    fun load () {
        database.Postgres.load()
    }
}