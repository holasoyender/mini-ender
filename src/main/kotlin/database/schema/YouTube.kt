package database.schema

import database.Redis
import interfaces.RedisSchema

class YouTube(
    channelId: String,
    links: List<String>
): RedisSchema {

    val channelId: String
    val links: List<String>

    init {
        this.channelId = channelId
        this.links = links
    }


    override fun save(): YouTube {
        Redis.connection.use { connection ->
            connection!!.del("$databaseName:$channelId")
            val res = connection.lpush("$databaseName:$channelId", *links.toTypedArray())
            if (res != links.size.toLong()) {
                throw Exception("Error al guardar los links")
            }
            connection.expire("$databaseName:$channelId", 86400)
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
                val links = connection!!.lrange("$databaseName:$channelId", 0, -1)
                return if (links.isEmpty()) null else YouTube(channelId, links)
            }
        }
    }
}