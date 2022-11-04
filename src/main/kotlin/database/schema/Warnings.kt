package database.schema

import enums.Severity
import interfaces.Schema

class Warnings(
    guildId: String,
    id: String,
    message: String,
    resolved: Boolean,
    severity: Severity,
    repeats: Int
): Schema {

    var guildId: String
    var id: String
    var message: String
    var resolved: Boolean
    var severity: Severity
    var repeats: Int

    private var isSaved = false
    private var isDeleted = false


    init {
        this.guildId = guildId
        this.id = id
        this.message = message
        this.resolved = resolved
        this.severity = severity
        this.repeats = repeats

        if (exists()) {
            isSaved = true
        }
    }

    override val tableName: String
        get() = "warnings"

    override fun dropTable() {
        database.Postgres.dataSource?.connection.use { connection ->
            val statement = connection!!.prepareStatement("DROP TABLE IF EXISTS warnings")
            statement.execute()
        }
    }

    override fun save(): Warnings {

        if (exists()) {
            database.Postgres.dataSource?.connection.use { connection ->
                val statement = connection!!.prepareStatement("UPDATE warnings SET guild_id = ?, id = ?, message = ?, resolved = ?, severity = ?, repeats = ? WHERE guild_id = ? AND id = ?")
                statement.setString(1, guildId)
                statement.setString(2, id)
                statement.setString(3, message)
                statement.setBoolean(4, resolved)
                statement.setString(5, severity.name)
                statement.setInt(6, repeats)
                statement.setString(7, guildId)
                statement.setString(8, id)

                statement.execute()
            }
        } else {
            database.Postgres.dataSource?.connection.use { connection ->
                val statement =
                    connection!!.prepareStatement("INSERT INTO warnings (guild_id, id, message, resolved, severity, repeats) VALUES (?, ?, ?, ?, ?, ?)")
                statement.setString(1, guildId)
                statement.setString(2, id)
                statement.setString(3, message)
                statement.setBoolean(4, resolved)
                statement.setString(5, severity.name)
                statement.setInt(6, repeats)
                statement.execute()
            }
        }

        isSaved = true
        return this
    }

    override fun delete(): Warnings {

        if (isDeleted)
            return this

        if(!exists())
            return this

        database.Postgres.dataSource?.connection.use { connection ->
            val statement = connection!!.prepareStatement("DELETE FROM warnings WHERE guild_id = ? AND id = ?")
            statement.setString(1, guildId)
            statement.setString(2, id)
            statement.execute()
        }
        isSaved = false
        isDeleted = true
        return this
    }

    override fun exists(): Boolean {
        database.Postgres.dataSource?.connection.use { connection ->
            val statement = connection!!.prepareStatement("SELECT * FROM warnings WHERE guild_id = ? AND id = ?")
            statement.setString(1, guildId)
            statement.setString(2, id)
            val result = statement.executeQuery()
            return result.next()
        }
    }

    companion object {
        fun createTable() {

            database.Postgres.dataSource?.connection.use { connection ->
                val statement = connection!!.prepareStatement(
                    """
            CREATE TABLE IF NOT EXISTS warnings (
                guild_id TEXT NOT NULL,
                id TEXT NOT NULL,
                message TEXT NOT NULL,
                resolved BOOLEAN NOT NULL,
                severity TEXT NOT NULL,
                PRIMARY KEY (guild_id, id)
            );""".trimIndent())
                statement.execute()
            }
        }

        fun get(id: String, guildId: String): Warnings? {
            database.Postgres.dataSource?.connection.use { connection ->
                val statement = connection!!.prepareStatement("SELECT * FROM warnings WHERE guild_id = ? AND id = ?")
                statement.setString(1, guildId)
                statement.setString(2, id)
                val result = statement.executeQuery()
                if (result.next()) {
                    return Warnings(
                        result.getString("guild_id"),
                        result.getString("id"),
                        result.getString("message"),
                        result.getBoolean("resolved"),
                        Severity.valueOf(result.getString("severity")),
                        result.getInt("repeats")
                    )
                }
            }
            return null
        }

        fun get(message: String, guildId: Long): Warnings? {
            database.Postgres.dataSource?.connection.use { connection ->
                val statement = connection!!.prepareStatement("SELECT * FROM warnings WHERE guild_id = ? AND message = ?")
                statement.setString(1, guildId.toString())
                statement.setString(2, message)

                val result = statement.executeQuery()
                if (result.next()) {
                    return Warnings(
                        result.getString("guild_id"),
                        result.getString("id"),
                        result.getString("message"),
                        result.getBoolean("resolved"),
                        Severity.valueOf(result.getString("severity")),
                        result.getInt("repeats")
                    )
                }
            }
            return null
        }

        @Suppress("unused")
        fun getAll(): List<Warnings> {
            database.Postgres.dataSource?.connection.use {connection ->
                val statement = connection!!.prepareStatement("SELECT * FROM warnings")
                val result = statement.executeQuery()

                val warnings = mutableListOf<Warnings>()
                while (result.next()) {
                    warnings.add(
                        Warnings(
                            result.getString("guild_id"),
                            result.getString("id"),
                            result.getString("message"),
                            result.getBoolean("resolved"),
                            Severity.valueOf(result.getString("severity")),
                            result.getInt("repeats")
                        )
                    )
                }
                return warnings
            }
        }

        @Suppress("unused")
        fun getAll(guildId: String): List<Warnings> {
            database.Postgres.dataSource?.connection.use {connection ->
                val statement = connection!!.prepareStatement("SELECT * FROM warnings WHERE guild_id = ?")
                statement.setString(1, guildId)
                val result = statement.executeQuery()

                val warnings = mutableListOf<Warnings>()
                while (result.next()) {
                    warnings.add(
                        Warnings(
                            result.getString("guild_id"),
                            result.getString("id"),
                            result.getString("message"),
                            result.getBoolean("resolved"),
                            Severity.valueOf(result.getString("severity")),
                            result.getInt("repeats")
                        )
                    )
                }
                return warnings
            }
        }
    }
}