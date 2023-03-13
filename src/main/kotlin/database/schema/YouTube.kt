package database.schema

import database.Redis
import interfaces.RedisSchema

class YouTube(
    channelId: String,
    latestVideo: String
): RedisSchema {

    val channelId: String
    val latestVideo: String

    init {
        this.channelId = channelId
        this.latestVideo = latestVideo
    }


    override fun save(): YouTube {
        Redis.connection.use { connection ->
            connection!!.del("$databaseName:$channelId")
            val res = connection.setex("$databaseName:$channelId", 86400, latestVideo)
            if (res != "OK") {
                throw Exception("Error al guardar el ultimo video del canal $channelId")
            }
        }
        return this
    }

    override fun delete(): YouTube? {
        Redis.connection.use { connection ->
            val res = connection!!.del("$databaseName:$channelId")
            return if (res == 1L) this else null
        }
    }

    override fun exists(): Boolean {
        Redis.connection.use { connection ->
            return connection!!.exists("$databaseName:$channelId")
        }
    }

    companion object {
        private const val databaseName = "youtube"

        fun get(channelId: String): YouTube? {
            Redis.connection.use { connection ->
                val res = connection!!.get("$databaseName:$channelId")
                return if (res == null) null else YouTube(channelId, res)
            }
        }
    }
}