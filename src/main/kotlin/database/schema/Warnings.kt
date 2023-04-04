package database.schema

import enums.Severity
import interfaces.Schema

class Warnings(
    guildId: String,
    id: String,
    message: String,
    resolved: Boolean,
    ignored: Boolean,
    severity: Severity,
    repeats: Int,
    lastSeen: Long,
    firstSeen: Long
): Schema {

    var guildId: String
    var id: String
    var message: String
    var resolved: Boolean
    var ignored: Boolean
    var severity: Severity
    var repeats: Int
    var lastSeen: Long
    var firstSeen: Long

    private var isSaved = false
    private var isDeleted = false


    init {
        this.guildId = guildId
        this.id = id
        this.message = message
        this.resolved = resolved
        this.ignored = ignored
        this.severity = severity
        this.repeats = repeats
        this.lastSeen = lastSeen
        this.firstSeen = firstSeen

        if (exists()) {
            isSaved = true
        }
    }

    override val tableName: String
        get() = "warnings"

    override fun dropTable() {
        database.Database.dataSource?.connection.use { connection ->
            val statement = connection!!.prepareStatement("DROP TABLE IF EXISTS warnings")
            statement.execute()
        }
    }

    override fun save(): Warnings {

        if (exists()) {
            database.Database.dataSource?.connection.use { connection ->
                val statement = connection!!.prepareStatement("UPDATE warnings SET guild_id = ?, id = ?, message = ?, resolved = ?, ignored = ?, severity = ?, repeats = ?, last_seen = ?, first_seen = ? WHERE guild_id = ? AND id = ?")
                statement.setString(1, guildId)
                statement.setString(2, id)
                statement.setString(3, message)
                statement.setBoolean(4, resolved)
                statement.setBoolean(5, ignored)
                statement.setString(6, severity.name)
                statement.setInt(7, repeats)
                statement.setLong(8, lastSeen)
                statement.setLong(9, firstSeen)
                statement.setString(10, guildId)
                statement.setString(11, id)

                statement.execute()
            }
        } else {
            database.Database.dataSource?.connection.use { connection ->
                val statement =
                    connection!!.prepareStatement("INSERT INTO warnings (guild_id, id, message, resolved, ignored, severity, repeats, last_seen, first_seen) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")
                statement.setString(1, guildId)
                statement.setString(2, id)
                statement.setString(3, message)
                statement.setBoolean(4, resolved)
                statement.setBoolean(5, ignored)
                statement.setString(6, severity.name)
                statement.setInt(7, repeats)
                statement.setLong(8, lastSeen)
                statement.setLong(9, firstSeen)
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

        database.Database.dataSource?.connection.use { connection ->
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
        database.Database.dataSource?.connection.use { connection ->
            val statement = connection!!.prepareStatement("SELECT * FROM warnings WHERE guild_id = ? AND id = ?")
            statement.setString(1, guildId)
            statement.setString(2, id)
            val result = statement.executeQuery()
            return result.next()
        }
    }

    companion object {
        fun createTable() {

            database.Database.dataSource?.connection.use { connection ->

                val isPostgres = connection!!.metaData.databaseProductName == "PostgreSQL"
                val statement = connection.prepareStatement(
                    """
            CREATE TABLE IF NOT EXISTS warnings (
                guild_id ${if (isPostgres) "TEXT" else "VARCHAR(21)"} NOT NULL,
                id ${if (isPostgres) "TEXT" else "VARCHAR(21)"} NOT NULL,
                message TEXT NOT NULL,
                resolved BOOLEAN NOT NULL,
                ignored BOOLEAN NOT NULL,
                severity TEXT NOT NULL,
                repeats INTEGER NOT NULL,
                last_seen BIGINT NOT NULL,
                first_seen BIGINT NOT NULL,
                PRIMARY KEY (guild_id, id)
            );""".trimIndent())
                statement.execute()
            }
        }

        fun get(id: String, guildId: String): Warnings? {
            database.Database.dataSource?.connection.use { connection ->
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
                        result.getBoolean("ignored"),
                        Severity.valueOf(result.getString("severity")),
                        result.getInt("repeats"),
                        result.getLong("last_seen"),
                        result.getLong("first_seen")
                    )
                }
            }
            return null
        }

        fun get(message: String, guildId: Long): Warnings? {
            database.Database.dataSource?.connection.use { connection ->
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
                        result.getBoolean("ignored"),
                        Severity.valueOf(result.getString("severity")),
                        result.getInt("repeats"),
                        result.getLong("last_seen"),
                        result.getLong("first_seen")
                    )
                }
            }
            return null
        }

        @Suppress("unused")
        fun getAll(): List<Warnings> {
            database.Database.dataSource?.connection.use { connection ->
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
                            result.getBoolean("ignored"),
                            Severity.valueOf(result.getString("severity")),
                            result.getInt("repeats"),
                            result.getLong("last_seen"),
                            result.getLong("first_seen")
                        )
                    )
                }
                return warnings
            }
        }

        @Suppress("unused")
        fun getAll(guildId: String): List<Warnings> {
            database.Database.dataSource?.connection.use { connection ->
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
                            result.getBoolean("ignored"),
                            Severity.valueOf(result.getString("severity")),
                            result.getInt("repeats"),
                            result.getLong("last_seen"),
                            result.getLong("first_seen")
                        )
                    )
                }
                return warnings
            }
        }
    }
}