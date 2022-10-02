package database.schema

import interfaces.Schema
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser

class Guild(
    id: String,
    prefix: String,
    customCommands: Array<JSONObject>?,
    logChannelId: String?,
): Schema {

    var id: String
    var prefix: String
    var customCommands: Array<JSONObject>?
    var logChannelId: String?

    private var isSaved = false
    private var isDeleted = false


    init {
        this.id = id
        this.prefix = prefix
        this.customCommands = customCommands
        this.logChannelId = logChannelId

        if (exists()) {
            isSaved = true
        }
    }

    override val tableName: String
        get() = "guilds"

    override fun dropTable() {
        database.Postgres.dataSource?.connection.use { connection ->
            val statement = connection!!.prepareStatement("DROP TABLE IF EXISTS guilds")
            statement.execute()
        }
    }

    override fun save(): Guild {

        if (exists()) {
            database.Postgres.dataSource?.connection.use { connection ->
                val statement = connection!!.prepareStatement("UPDATE guilds SET prefix = ?, custom_commands = ?, log_channel_id = ? WHERE id = ?")
                statement.setString(1, prefix)
                statement.setArray(2, connection.createArrayOf("jsonb", customCommands))
                statement.setString(3, logChannelId)
                statement.setString(4, id)
                statement.execute()
            }
        } else {
            database.Postgres.dataSource?.connection.use { connection ->
                val statement =
                    connection!!.prepareStatement("INSERT INTO guilds (id, prefix, custom_commands, log_channel_id) VALUES (?, ?, ?, ?)")
                statement.setString(1, id)
                statement.setString(2, prefix)
                statement.setArray(3, connection.createArrayOf("json", customCommands))
                statement.setString(4, logChannelId)
                statement.execute()
            }
        }

        isSaved = true
        return this
    }

    override fun delete(): Guild {

        if (isDeleted)
            return this

        database.Postgres.dataSource?.connection.use { connection ->
            val statement = connection!!.prepareStatement("DELETE FROM guilds WHERE id = ?")
            statement.setString(1, id)
            statement.execute()
        }
        isSaved = false
        isDeleted = true
        return this
    }

    override fun exists(): Boolean {
        database.Postgres.dataSource?.connection.use { connection ->
            val statement = connection!!.prepareStatement("SELECT * FROM guilds WHERE id = ?")
            statement.setString(1, id)
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
            CREATE TABLE IF NOT EXISTS guilds ( 
                id TEXT PRIMARY KEY NOT NULL,
                prefix TEXT NOT NULL,
                custom_commands JSON[],
                log_channel_id TEXT
            );"""
                )

                statement.execute()
            }
        }

        fun get(id: String): Guild? {
            database.Postgres.dataSource?.connection.use { connection ->
                val statement = connection!!.prepareStatement("SELECT * FROM guilds WHERE id = ?")
                statement.setString(1, id)
                val result = statement.executeQuery()
                if (result.next()) {
                    return Guild(
                        result.getString("id"),
                        result.getString("prefix"),
                        //esta linea ha causado un daño permanente en mi cerebro
                        (result.getArray("custom_commands")?.array as Array<String>?)?.map { JSONParser().parse(it) as JSONObject }?.toTypedArray() ?: arrayOf(),
                        result.getString("log_channel_id"),
                    )
                }
            }
            return null
        }

        fun getAll(): List<Guild> {
            database.Postgres.dataSource?.connection.use {connection ->
                val statement = connection!!.prepareStatement("SELECT * FROM guilds")
                val result = statement.executeQuery()

                val guilds = mutableListOf<Guild>()
                while (result.next()) {
                    guilds.add(
                        Guild(
                            result.getString("id"),
                            result.getString("prefix"),
                            (result.getArray("custom_commands")?.array as Array<String>?)?.map { JSONParser().parse(it) as JSONObject }?.toTypedArray() ?: arrayOf(),
                            result.getString("log_channel_id"),
                        )
                    )
                }
                return guilds
            }
        }
    }
}