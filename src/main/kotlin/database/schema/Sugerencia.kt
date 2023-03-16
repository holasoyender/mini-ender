package database.schema

import com.google.gson.Gson
import database.Redis
import interfaces.RedisSchema

class Sugerencia(
    id: String,
    acceptVotes: List<String>,
    denyVotes: List<String>,
    createdAt: Long = System.currentTimeMillis(),
        ): RedisSchema {

    var id: String
    var acceptVotes: List<String>
    var denyVotes: List<String>
    private var createdAt: Long

    init {
        this.id = id
        this.acceptVotes = acceptVotes
        this.denyVotes = denyVotes
        this.createdAt = createdAt
    }

    override fun save(): Sugerencia {
        try {
            Redis.connection.use { connection ->
                val json = Gson().toJson(this)
                val res = connection!!.setex("$databaseName:$id", 259200, json)
                if (res != "OK") {
                    throw Exception("Error al guardar la sugerencia")
                }
            }
        } catch (_: Exception) {
            Redis.usingRedis = false
        }
        return this
    }

    override fun delete(): Sugerencia? {
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

        const val databaseName = "suggestions"

        fun get(id: String): Sugerencia? {
            try {
                Redis.connection.use { connection ->
                    val res = connection!!.get("$databaseName:$id")
                    return if (res != null) {
                        Gson().fromJson(res, Sugerencia::class.java)
                    } else null
                }
            } catch (_: Exception) {
                Redis.usingRedis = false
                return null
            }
        }
    }

}