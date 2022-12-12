package database.schema

import interfaces.Schema
import org.json.JSONObject

class Regalo(
    userId: String,
    lastThrow: Long,
    gifts: Array<JSONObject>
): Schema {

    var userId: String
    var lastThrow: Long
    var gifts: Array<JSONObject>

    private var isSaved = false
    private var isDeleted = false

    init {
        this.userId = userId
        this.lastThrow = lastThrow
        this.gifts = gifts

        if (exists()) {
            isSaved = true
        }
    }
    override val tableName: String
        get() = "regalos"

    override fun dropTable() {
        database.Postgres.dataSource?.connection.use { connection ->
            val statement = connection!!.prepareStatement("DROP TABLE IF EXISTS regalos")
            statement.execute()
        }
    }

    override fun save(): Regalo {
        if (exists()) {
            database.Postgres.dataSource?.connection.use { connection ->
                val statement =
                    connection!!.prepareStatement("UPDATE regalos SET last_throw = ?, gifts = ? WHERE user_id = ?")
                statement.setLong(1, lastThrow)
                statement.setArray(2, connection.createArrayOf("jsonb", gifts))
                statement.setString(3, userId)
                statement.execute()
            }
        } else {
            database.Postgres.dataSource?.connection.use { connection ->
                val statement =
                    connection!!.prepareStatement("INSERT INTO regalos (user_id, last_throw, gifts) VALUES (?, ?, ?)")
                statement.setString(1, userId)
                statement.setLong(2, lastThrow)
                statement.setArray(3, connection.createArrayOf("json", gifts))
                statement.execute()
            }
        }

        isSaved = true
        return this
    }

    override fun delete(): Regalo {
        if (isDeleted)
            return this

        if(!exists())
            return this

        database.Postgres.dataSource?.connection.use { connection ->
            val statement = connection!!.prepareStatement("DELETE FROM regalos WHERE user_id = ?")
            statement.setString(1, userId)
            statement.execute()
        }
        isSaved = false
        isDeleted = true
        return this
    }

    override fun exists(): Boolean {
        database.Postgres.dataSource?.connection.use { connection ->
            val statement = connection!!.prepareStatement("SELECT * FROM regalos WHERE user_id = ?")
            statement.setString(1, userId)
            val result = statement.executeQuery()
            return result.next()
        }
    }

    @Suppress("UNCHECKED_CAST")
    companion object {
        fun createTable() {

            database.Postgres.dataSource?.connection.use { connection ->
                val statement = connection!!.prepareStatement(
                    """
                    CREATE TABLE IF NOT EXISTS regalos (
                        user_id TEXT NOT NULL,
                        last_throw BIGINT NOT NULL,
                        gifts JSON[] NOT NULL,
                        PRIMARY KEY (user_id)
                    )
                    """.trimIndent()
                )
                statement.execute()
            }
        }

        fun get(userId: String): Regalo? {
            database.Postgres.dataSource?.connection.use { connection ->
                val statement = connection!!.prepareStatement("SELECT * FROM regalos WHERE user_id = ?")
                statement.setString(1, userId)
                val result = statement.executeQuery()
                if (result.next()) {
                    return Regalo(
                        result.getString("user_id"),
                        result.getLong("last_throw"),
                        (result.getArray("gifts")?.array as Array<String>?)?.map { JSONObject(it) }?.toTypedArray() ?: arrayOf(),
                    )
                }
            }
            return null
        }

        fun getAll(): List<Regalo> {
            val regalos = mutableListOf<Regalo>()
            database.Postgres.dataSource?.connection.use { connection ->
                val statement = connection!!.prepareStatement("SELECT * FROM regalos")
                val result = statement.executeQuery()
                while (result.next()) {
                    regalos.add(
                        Regalo(
                            result.getString("user_id"),
                            result.getLong("last_throw"),
                            (result.getArray("gifts")?.array as Array<String>?)?.map { JSONObject(it) }?.toTypedArray() ?: arrayOf(),
                        )
                    )
                }
            }
            return regalos
        }
    }
}