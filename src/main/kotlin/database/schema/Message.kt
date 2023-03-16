package database.schema

import com.google.gson.Gson
import database.Redis
import interfaces.RedisSchema

class Message(
    content: String,
    contentDisplay: String,
    id: String,
    authorTag: String,
    authorId: String,
    authorAvatar: String,
    channelId: String,
): RedisSchema {

    var content: String
    var contentDisplay: String
    var id: String
    var authorTag: String
    var authorId: String
    var authorAvatar: String
    var channelId: String

    init {
        this.content = content
        this.contentDisplay = contentDisplay
        this.id = id
        this.authorTag = authorTag
        this.authorId = authorId
        this.authorAvatar = authorAvatar
        this.channelId = channelId
    }

    override fun save(): Message {
        try {
        Redis.connection.use { connection ->
            val json = Gson().toJson(this)
            val res = connection!!.setex("$databaseName:$id", 172800, json)
            if (res != "OK") {
                throw Exception("Error al guardar el mensaje")
            }

            val allMessages = connection.keys("$databaseName:*")
            if (allMessages.size > 100) {
                val oldestMessage = allMessages.minBy { connection.ttl(it) }
                if (oldestMessage != null) {
                    connection.del(oldestMessage)
                }
            }
        } }
        catch (_: Exception) {
            Redis.usingRedis = false
        }
        return this
    }

    override fun delete(): Message? {
        try {
            Redis.connection.use { connection ->
                val res = connection!!.del("$databaseName:$id")
                return if (res == 1L) this else null
            }
        } catch (_: Exception) {
            Redis.usingRedis = false
            return null
        }
    }

    override fun exists(): Boolean {
        try {
            Redis.connection.use { connection ->
                return connection!!.exists("$databaseName:$id")
            }
        } catch (_: Exception) {
            Redis.usingRedis = false
            return false
        }
    }

    companion object {
        private const val databaseName = "messages"

        fun get(id: String): Message? {
            try {
                Redis.connection.use { connection ->
                    val json = connection!!.get("$databaseName:$id")
                    return if (json != null) {
                        Gson().fromJson(json, Message::class.java)
                    } else null
                }
            } catch (_: Exception) {
                Redis.usingRedis = false
                return null
            }
        }
    }
}