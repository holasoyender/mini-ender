package database.schema

import interfaces.Schema
import org.json.JSONObject

class Guild(
    id: String,
    prefix: String,

    welcomeRoleId: String,
    welcomeChannelId: String,
    welcomeMessage: String,

    muteRoleId: String,

    logsChannelId: String,

    antiLinksEnabled: Boolean,
    antiLinksChannelId: String,
    antiLinksIgnoredRoles: Array<String>,
    antiLinksIgnoredChannels: Array<String>,
    antiPhishingEnabled: Boolean,

    customCommands: Array<JSONObject>,

    twitchChannel: String,
    twitchAnnounceChannelId: String,
    twitchAnnounceMessage: String,
    twitchLiveChannelId: String,
    twitchLiveMessage: String,

    raw: String
): Schema {

    var id: String
    var prefix: String

    var welcomeRoleId: String
    var welcomeChannelId: String
    var welcomeMessage: String

    var muteRoleId: String

    var logsChannelId: String

    var antiLinksEnabled: Boolean
    var antiLinksChannelId: String
    var antiLinksIgnoredRoles: Array<String>
    var antiLinksIgnoredChannels: Array<String>
    var antiPhishingEnabled: Boolean

    var customCommands: Array<JSONObject>

    var twitchChannel: String
    var twitchAnnounceChannelId: String
    var twitchAnnounceMessage: String
    var twitchLiveChannelId: String
    var twitchLiveMessage: String

    var raw: String

    private var isSaved = false
    private var isDeleted = false


    init {
        this.id = id
        this.prefix = prefix

        this.welcomeRoleId = welcomeRoleId
        this.welcomeChannelId = welcomeChannelId
        this.welcomeMessage = welcomeMessage

        this.muteRoleId = muteRoleId

        this.logsChannelId = logsChannelId

        this.antiLinksEnabled = antiLinksEnabled
        this.antiLinksChannelId = antiLinksChannelId
        this.antiLinksIgnoredRoles = antiLinksIgnoredRoles
        this.antiLinksIgnoredChannels = antiLinksIgnoredChannels
        this.antiPhishingEnabled = antiPhishingEnabled

        this.customCommands = customCommands

        this.twitchChannel = twitchChannel
        this.twitchAnnounceChannelId = twitchAnnounceChannelId
        this.twitchAnnounceMessage = twitchAnnounceMessage
        this.twitchLiveChannelId = twitchLiveChannelId
        this.twitchLiveMessage = twitchLiveMessage

        this.raw = raw

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
                val statement = connection!!.prepareStatement("UPDATE guilds SET prefix = ?, welcome_role_id = ?, welcome_channel_id = ?, welcome_message = ?, mute_role_id = ?, logs_channel_id = ?, anti_links_enabled = ?, anti_links_channel_id = ?, anti_links_ignored_roles = ?, anti_links_ignored_channels = ?, anti_phishing_enabled = ?, custom_commands = ?, twitch_channel = ?, twitch_announce_channel_id = ?, twitch_announce_message = ?, twitch_live_channel_id = ?, twitch_live_message = ?, raw = ? WHERE id = ?")
                statement.setString(1, prefix)

                statement.setString(2, welcomeRoleId)
                statement.setString(3, welcomeChannelId)
                statement.setString(4, welcomeMessage)

                statement.setString(5, muteRoleId)

                statement.setString(6, logsChannelId)

                statement.setBoolean(7, antiLinksEnabled)
                statement.setString(8, antiLinksChannelId)
                statement.setArray(9, connection.createArrayOf("text", antiLinksIgnoredRoles))
                statement.setArray(10, connection.createArrayOf("text", antiLinksIgnoredChannels))
                statement.setBoolean(11, antiPhishingEnabled)

                statement.setArray(12, connection.createArrayOf("jsonb", customCommands))

                statement.setString(13, twitchChannel)
                statement.setString(14, twitchAnnounceChannelId)
                statement.setString(15, twitchAnnounceMessage)
                statement.setString(16, twitchLiveChannelId)
                statement.setString(17, twitchLiveMessage)

                statement.setString(18, raw)

                statement.setString(19, id)

                statement.execute()
            }
        } else {
            database.Postgres.dataSource?.connection.use { connection ->
                val statement =
                    connection!!.prepareStatement("INSERT INTO guilds (id, prefix, welcome_role_id, welcome_channel_id, welcome_message, mute_role_id, logs_channel_id, anti_links_enabled, anti_links_channel_id, anti_links_ignored_roles, anti_links_ignored_channels, anti_phishing_enabled, custom_commands, twitch_channel, twitch_announce_channel_id, twitch_announce_message, twitch_live_channel_id, twitch_live_message, raw) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")
                statement.setString(1, id)
                statement.setString(2, prefix)

                statement.setString(3, welcomeRoleId)
                statement.setString(4, welcomeChannelId)
                statement.setString(5, welcomeMessage)

                statement.setString(6, muteRoleId)

                statement.setString(7, logsChannelId)

                statement.setBoolean(8, antiLinksEnabled)
                statement.setString(9, antiLinksChannelId)
                statement.setArray(10, connection.createArrayOf("text", antiLinksIgnoredRoles))
                statement.setArray(11, connection.createArrayOf("text", antiLinksIgnoredChannels))
                statement.setBoolean(12, antiPhishingEnabled)

                statement.setArray(13, connection.createArrayOf("jsonb", customCommands))

                statement.setString(14, twitchChannel)
                statement.setString(15, twitchAnnounceChannelId)
                statement.setString(16, twitchAnnounceMessage)
                statement.setString(17, twitchLiveChannelId)
                statement.setString(18, twitchLiveMessage)

                statement.setString(19, raw)

                statement.execute()
            }
        }

        isSaved = true
        return this
    }

    override fun delete(): Guild {

        if (isDeleted)
            return this

        if(!exists())
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
                id TEXT PRIMARY KEY NOT NULL UNIQUE,
                prefix TEXT NOT NULL,
                welcome_role_id TEXT NOT NULL,
                welcome_channel_id TEXT NOT NULL,
                welcome_message TEXT NOT NULL,
                mute_role_id TEXT NOT NULL,
                logs_channel_id TEXT NOT NULL,
                anti_links_enabled BOOLEAN NOT NULL,
                anti_links_channel_id TEXT NOT NULL,
                anti_links_ignored_roles TEXT[] NOT NULL,
                anti_links_ignored_channels TEXT[] NOT NULL,
                anti_phishing_enabled BOOLEAN NOT NULL,
                custom_commands JSON[] NOT NULL,
                twitch_channel TEXT NOT NULL,
                twitch_announce_channel_id TEXT NOT NULL,
                twitch_announce_message TEXT NOT NULL,
                twitch_live_channel_id TEXT NOT NULL,
                twitch_live_message TEXT NOT NULL,
                raw TEXT NOT NULL
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

                        result.getString("welcome_role_id"),
                        result.getString("welcome_channel_id"),
                        result.getString("welcome_message"),

                        result.getString("mute_role_id"),

                        result.getString("logs_channel_id"),

                        result.getBoolean("anti_links_enabled"),
                        result.getString("anti_links_channel_id"),
                        result.getArray("anti_links_ignored_roles").array as Array<String>,
                        result.getArray("anti_links_ignored_channels").array as Array<String>,
                        result.getBoolean("anti_phishing_enabled"),

                        //esta linea ha causado un daño permanente en mi cerebro
                        (result.getArray("custom_commands")?.array as Array<String>?)?.map { JSONObject(it) }?.toTypedArray() ?: arrayOf(),

                        result.getString("twitch_channel"),
                        result.getString("twitch_announce_channel_id"),
                        result.getString("twitch_announce_message"),
                        result.getString("twitch_live_channel_id"),
                        result.getString("twitch_live_message"),

                        result.getString("raw")

                    )
                }
            }
            return null
        }

        @Suppress("unused")
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

                            result.getString("welcome_role_id"),
                            result.getString("welcome_channel_id"),
                            result.getString("welcome_message"),

                            result.getString("mute_role_id"),

                            result.getString("logs_channel_id"),

                            result.getBoolean("anti_links_enabled"),
                            result.getString("anti_links_channel_id"),
                            result.getArray("anti_links_ignored_roles").array as Array<String>,
                            result.getArray("anti_links_ignored_channels").array as Array<String>,
                            result.getBoolean("anti_phishing_enabled"),

                            //esta linea ha causado un daño permanente en mi cerebro
                            (result.getArray("custom_commands")?.array as Array<String>?)?.map { JSONObject(it) }?.toTypedArray() ?: arrayOf(),

                            result.getString("twitch_channel"),
                            result.getString("twitch_announce_channel_id"),
                            result.getString("twitch_announce_message"),
                            result.getString("twitch_live_channel_id"),
                            result.getString("twitch_live_message"),

                            result.getString("raw")
                        )
                    )
                }
                return guilds
            }
        }

        fun getGuildsWithTwitchSubscriptions(): List<Guild> {
            database.Postgres.dataSource?.connection.use {connection ->
                val statement = connection!!.prepareStatement("SELECT * FROM guilds WHERE twitch_channel <> ''")
                val result = statement.executeQuery()

                val guilds = mutableListOf<Guild>()
                while (result.next()) {
                    guilds.add(
                        Guild(
                            result.getString("id"),
                            result.getString("prefix"),

                            result.getString("welcome_role_id"),
                            result.getString("welcome_channel_id"),
                            result.getString("welcome_message"),

                            result.getString("mute_role_id"),

                            result.getString("logs_channel_id"),

                            result.getBoolean("anti_links_enabled"),
                            result.getString("anti_links_channel_id"),
                            result.getArray("anti_links_ignored_roles").array as Array<String>,
                            result.getArray("anti_links_ignored_channels").array as Array<String>,
                            result.getBoolean("anti_phishing_enabled"),

                            //esta linea ha causado un daño permanente en mi cerebro
                            (result.getArray("custom_commands")?.array as Array<String>?)?.map { JSONObject(it) }?.toTypedArray() ?: arrayOf(),

                            result.getString("twitch_channel"),
                            result.getString("twitch_announce_channel_id"),
                            result.getString("twitch_announce_message"),
                            result.getString("twitch_live_channel_id"),
                            result.getString("twitch_live_message"),

                            result.getString("raw")
                        )
                    )
                }
                return guilds
            }
        }
    }
}