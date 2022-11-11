package database.schema

import enums.InfractionType
import interfaces.Schema

class Infraction(
    userId: String,
    userName: String,
    guildId: String,
    moderatorId: String,
    type: InfractionType,
    reason: String,
    duration: Long = 0,
    succeeded: Boolean,
    id: Int? = null
): Schema {

    var userId: String
    var userName: String
    var guildId: String
    var moderatorId: String
    var type: String
    var reason: String
    var duration: Long
    var succeeded: Boolean
    var date: Long
    var id: Long

    private var isSaved = false
    private var isDeleted = false


    init {
        this.userId = userId
        this.userName = userName
        this.guildId = guildId
        this.moderatorId = moderatorId
        this.type = type.name
        this.reason = reason
        this.duration = duration
        this.succeeded = succeeded
        this.date = System.currentTimeMillis()
        this.id = id?.toLong() ?: generateId(guildId)

        if (exists()) {
            isSaved = true
        }
    }

    override val tableName: String
        get() = "infractions"

    override fun dropTable() {
        database.Postgres.dataSource?.connection.use { connection ->
            val statement = connection!!.prepareStatement("DROP TABLE IF EXISTS infractions")
            statement.execute()
        }
    }

    override fun save(): Infraction {

        if (exists()) {
            database.Postgres.dataSource?.connection.use { connection ->
                val statement = connection!!.prepareStatement("UPDATE infractions SET user_id = ?, user_name = ?, moderator_id = ?, type = ?, reason = ?, duration = ?, succeeded = ?, date = ? WHERE guild_id = ? AND id = ?")
                statement.setString(1, userId)
                statement.setString(2, userName)
                statement.setString(3, moderatorId)
                statement.setString(4, type)
                statement.setString(5, reason)
                statement.setLong(6, duration)
                statement.setBoolean(7, succeeded)
                statement.setLong(8, date)
                statement.setString(9, guildId)
                statement.setLong(10, id)

                statement.execute()
            }
        } else {
            database.Postgres.dataSource?.connection.use { connection ->
                val statement =
                    connection!!.prepareStatement("INSERT INTO infractions (user_id, user_name, guild_id, moderator_id, type, reason, duration, succeeded, date, id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")
                statement.setString(1, userId)
                statement.setString(2, userName)
                statement.setString(3, guildId)
                statement.setString(4, moderatorId)
                statement.setString(5, type)
                statement.setString(6, reason)
                statement.setLong(7, duration)
                statement.setBoolean(8, succeeded)
                statement.setLong(9, date)
                statement.setLong(10, id)

                statement.execute()
            }
        }

        isSaved = true
        return this
    }

    override fun delete(): Infraction {

        if (isDeleted)
            return this

        if(!exists())
            return this

        database.Postgres.dataSource?.connection.use { connection ->
            val statement = connection!!.prepareStatement("DELETE FROM infractions WHERE guild_id = ? AND id = ?")
            statement.setString(1, guildId)
            statement.setLong(2, id)
            statement.execute()
        }
        isSaved = false
        isDeleted = true
        return this
    }

    override fun exists(): Boolean {
        database.Postgres.dataSource?.connection.use { connection ->
            val statement = connection!!.prepareStatement("SELECT * FROM infractions WHERE guild_id = ? AND id = ?")
            statement.setString(1, guildId)
            statement.setLong(2, id)
            val result = statement.executeQuery()
            return result.next()
        }
    }

    companion object {
        fun createTable() {

            database.Postgres.dataSource?.connection.use { connection ->
                val statement = connection!!.prepareStatement(
                    """
            CREATE TABLE IF NOT EXISTS infractions (
                user_id TEXT NOT NULL,
                user_name TEXT NOT NULL,
                guild_id TEXT NOT NULL,
                moderator_id TEXT NOT NULL,
                type TEXT NOT NULL,
                reason TEXT NOT NULL,
                duration BIGINT NOT NULL,
                succeeded BOOLEAN NOT NULL,
                date BIGINT NOT NULL,
                id BIGINT NOT NULL,
                PRIMARY KEY (guild_id, id)
            );""".trimIndent())
                statement.execute()
            }
        }

        fun get(id: Long, guildId: String): Infraction? {
            database.Postgres.dataSource?.connection.use { connection ->
                val statement = connection!!.prepareStatement("SELECT * FROM infractions WHERE guild_id = ? AND id = ?")
                statement.setString(1, guildId)
                statement.setLong(2, id)
                val result = statement.executeQuery()
                if (result.next()) {
                    return Infraction(
                        result.getString("user_id"),
                        result.getString("user_name"),
                        result.getString("guild_id"),
                        result.getString("moderator_id"),
                        InfractionType.valueOf(result.getString("type")),
                        result.getString("reason"),
                        result.getLong("duration"),
                        result.getBoolean("succeeded"),
                        result.getInt("id")
                    )
                }
            }
            return null
        }

        @Suppress("unused")
        fun getAll(): List<Infraction> {
            database.Postgres.dataSource?.connection.use {connection ->
                val statement = connection!!.prepareStatement("SELECT * FROM infractions")
                val result = statement.executeQuery()

                val infractions = mutableListOf<Infraction>()
                while (result.next()) {
                    infractions.add(
                        Infraction(
                            result.getString("user_id"),
                            result.getString("user_name"),
                            result.getString("guild_id"),
                            result.getString("moderator_id"),
                            InfractionType.valueOf(result.getString("type")),
                            result.getString("reason"),
                            result.getLong("duration"),
                            result.getBoolean("succeeded"),
                            result.getInt("id")
                        )
                    )
                }
                return infractions
            }
        }

        @Suppress("unused")
        fun getAll(guildId: String): List<Infraction> {
            database.Postgres.dataSource?.connection.use {connection ->
                val statement = connection!!.prepareStatement("SELECT * FROM warnings WHERE guild_id = ?")
                statement.setString(1, guildId)
                val result = statement.executeQuery()

                val infractions = mutableListOf<Infraction>()
                while (result.next()) {
                    infractions.add(
                        Infraction(
                            result.getString("user_id"),
                            result.getString("user_name"),
                            result.getString("guild_id"),
                            result.getString("moderator_id"),
                            InfractionType.valueOf(result.getString("type")),
                            result.getString("reason"),
                            result.getLong("duration"),
                            result.getBoolean("succeeded")
                        )
                    )
                }
                return infractions
            }
        }

        fun generateId(guildId: String): Long {
            val id = (1..100000).random().toLong()
            while (idExists(id, guildId)) {
                generateId(guildId)
            }
            return id
        }

        private fun idExists(id: Long, guildId: String): Boolean {
            database.Postgres.dataSource?.connection.use { connection ->
                val statement = connection!!.prepareStatement("SELECT * FROM infractions WHERE guild_id = ? AND id = ?")
                statement.setString(1, guildId)
                statement.setLong(2, id)
                val result = statement.executeQuery()
                return result.next()
            }
        }
    }
}