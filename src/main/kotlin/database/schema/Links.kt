package database.schema

import interfaces.Schema
import enums.Actions

class Links(
    guildId: String,
    domain: String,
    moderatorId: String,
    action: Actions,
    reason: String,
    duration: Long,
    durationRaw: String,
    blockedAt: Long,
    timesAppeared: Int,
    underRevision: Boolean,
): Schema {

    var guildId: String
    var domain: String
    var moderatorId: String
    var action: Actions
    var reason: String
    var duration: Long
    var durationRaw: String
    var blockedAt: Long
    var timesAppeared: Int
    var underRevision: Boolean

    private var isSaved = false
    private var isDeleted = false


    init {
        this.guildId = guildId
        this.domain = domain
        this.moderatorId = moderatorId
        this.action = action
        this.reason = reason
        this.duration = duration
        this.durationRaw = durationRaw
        this.blockedAt = blockedAt
        this.timesAppeared = timesAppeared
        this.underRevision = underRevision

        if (exists()) {
            isSaved = true
        }
    }

    override val tableName: String
        get() = "links"

    override fun dropTable() {
        database.Database.dataSource?.connection.use { connection ->
            val statement = connection!!.prepareStatement("DROP TABLE IF EXISTS links")
            statement.execute()
        }
    }

    override fun save(): Links {

        if (exists()) {
            database.Database.dataSource?.connection.use { connection ->
                val statement = connection!!.prepareStatement("UPDATE links SET domain = ?, moderator_id = ?, action = ?, reason = ?, duration = ?, duration_raw = ?, blocked_at = ?, times_appeared = ?, under_revision = ? WHERE guild_id = ? AND domain = ?")
                statement.setString(1, domain)
                statement.setString(2, moderatorId)
                statement.setString(3, action.name)
                statement.setString(4, reason)
                statement.setLong(5, duration)
                statement.setString(6, durationRaw)
                statement.setLong(7, blockedAt)
                statement.setInt(8, timesAppeared)
                statement.setBoolean(9, underRevision)
                statement.setString(10, guildId)
                statement.setString(11, domain)
                statement.execute()
            }
        } else {
            database.Database.dataSource?.connection.use { connection ->
                val statement =
                    connection!!.prepareStatement("INSERT INTO links (guild_id, domain, moderator_id, action, reason, duration, duration_raw, blocked_at, times_appeared, under_revision) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")
                statement.setString(1, guildId)
                statement.setString(2, domain)
                statement.setString(3, moderatorId)
                statement.setString(4, action.name)
                statement.setString(5, reason)
                statement.setLong(6, duration)
                statement.setString(7, durationRaw)
                statement.setLong(8, blockedAt)
                statement.setInt(9, timesAppeared)
                statement.setBoolean(10, underRevision)
                statement.execute()
            }
        }

        isSaved = true
        return this
    }

    override fun delete(): Links {

        if (isDeleted)
            return this

        if(!exists())
            return this

        database.Database.dataSource?.connection.use { connection ->
            val statement = connection!!.prepareStatement("DELETE FROM links WHERE guild_id = ? AND domain = ?")
            statement.setString(1, guildId)
            statement.setString(2, domain)
            statement.execute()
        }
        isSaved = false
        isDeleted = true
        return this
    }

    override fun exists(): Boolean {
        database.Database.dataSource?.connection.use { connection ->
            val statement = connection!!.prepareStatement("SELECT * FROM links WHERE guild_id = ? AND domain = ?")
            statement.setString(1, guildId)
            statement.setString(2, domain)
            val result = statement.executeQuery()
            return result.next()
        }
    }

    companion object {
        fun createTable() {

            database.Database.dataSource?.connection.use { connection ->
                val statement = connection!!.prepareStatement(
                    """
            CREATE TABLE IF NOT EXISTS links (
                guild_id TEXT NOT NULL,
                domain TEXT NOT NULL,
                moderator_id TEXT NOT NULL,
                action TEXT NOT NULL,
                reason TEXT NOT NULL,
                duration BIGINT NOT NULL,
                duration_raw TEXT NOT NULL,
                blocked_at BIGINT NOT NULL,
                times_appeared INT NOT NULL,
                under_revision BOOLEAN NOT NULL
            );""".trimIndent()
                )

                statement.execute()
            }
        }

        fun get(domain: String, guildId: String): Links? {
            database.Database.dataSource?.connection.use { connection ->
                val statement = connection!!.prepareStatement("SELECT * FROM links WHERE domain = ? AND guild_id = ?")
                statement.setString(1, domain)
                statement.setString(2, guildId)
                val result = statement.executeQuery()
                if (result.next()) {
                    return Links(
                        result.getString("guild_id"),
                        result.getString("domain"),
                        result.getString("moderator_id"),
                        Actions.valueOf(result.getString("action")),
                        result.getString("reason"),
                        result.getLong("duration"),
                        result.getString("duration_raw"),
                        result.getLong("blocked_at"),
                        result.getInt("times_appeared"),
                        result.getBoolean("under_revision")
                    )
                }
            }
            return null
        }

        @Suppress("unused")
        fun getAll(): List<Links> {
            database.Database.dataSource?.connection.use { connection ->
                val statement = connection!!.prepareStatement("SELECT * FROM links")
                val result = statement.executeQuery()

                val links = mutableListOf<Links>()
                while (result.next()) {
                    links.add(
                        Links(
                            result.getString("guild_id"),
                            result.getString("domain"),
                            result.getString("moderator_id"),
                            Actions.valueOf(result.getString("action")),
                            result.getString("reason"),
                            result.getLong("duration"),
                            result.getString("duration_raw"),
                            result.getLong("blocked_at"),
                            result.getInt("times_appeared"),
                            result.getBoolean("under_revision")
                        )
                    )
                }
                return links
            }
        }

        @Suppress("unused")
        fun getAll(guildId: String): List<Links> {
            database.Database.dataSource?.connection.use { connection ->
                val statement = connection!!.prepareStatement("SELECT * FROM links WHERE guild_id = ?")
                statement.setString(1, guildId)
                val result = statement.executeQuery()

                val links = mutableListOf<Links>()
                while (result.next()) {
                    links.add(
                        Links(
                            result.getString("guild_id"),
                            result.getString("domain"),
                            result.getString("moderator_id"),
                            Actions.valueOf(result.getString("action")),
                            result.getString("reason"),
                            result.getLong("duration"),
                            result.getString("duration_raw"),
                            result.getLong("blocked_at"),
                            result.getInt("times_appeared"),
                            result.getBoolean("under_revision")
                        )
                    )
                }
                return links
            }
        }
    }
}