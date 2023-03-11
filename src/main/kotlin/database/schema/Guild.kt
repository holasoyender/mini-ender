package database.schema

import com.google.gson.Gson
import database.Redis
import interfaces.Schema
import org.json.JSONObject
import java.sql.ResultSet

class Guild(
    id: String,
    prefix: String,

    welcomeRoleId: String,
    welcomeChannelId: String,
    welcomeMessage: String,

    muteRoleId: String,

    moderationSilent: Boolean,
    moderationChannelId: String,

    permissions: Map<String, Int>,

    logsChannelId: String,
    moderationLogsChannelId: String,

    antiLinksEnabled: Boolean,
    antiLinksAllowedLinks: Array<String>,
    antiLinksChannelId: String,
    antiLinksIgnoredRoles: Array<String>,
    antiLinksIgnoredChannels: Array<String>,
    antiPhishingEnabled: Boolean,

    customCommands: Array<JSONObject>,

    twitchChannel: String,
    twitchAnnounceChannelId: String,
    twitchAnnounceMessage: String,
    twitchLiveChannelId: String,
    twitchOpenLiveMessage: String,
    twitchCloseLiveMessage: String,

    sanctionMessage: String,
    antiLinksNewLinkMessage: String,
    antiLinksUnderRevisionMessage: String,
    antiLinksSanctionMessage: String,

    suggestChannel: String,
    suggestCreateThread: Boolean,

    raw: String
): Schema {

    var id: String
    var prefix: String

    var welcomeRoleId: String
    var welcomeChannelId: String
    var welcomeMessage: String

    var muteRoleId: String

    var moderationSilent: Boolean
    var moderationChannelId: String

    var permissions: Map<String, Int>

    var logsChannelId: String
    var moderationLogsChannelId: String

    var antiLinksEnabled: Boolean
    var antiLinksAllowedLinks: Array<String>
    var antiLinksChannelId: String
    var antiLinksIgnoredRoles: Array<String>
    var antiLinksIgnoredChannels: Array<String>
    var antiPhishingEnabled: Boolean

    var customCommands: Array<JSONObject>

    var twitchChannel: String
    var twitchAnnounceChannelId: String
    var twitchAnnounceMessage: String
    var twitchLiveChannelId: String
    var twitchOpenLiveMessage: String
    var twitchCloseLiveMessage: String

    var sanctionMessage: String
    var antiLinksNewLinkMessage: String
    var antiLinksUnderRevisionMessage: String
    var antiLinksSanctionMessage: String

    var suggestChannel: String
    var suggestCreateThread: Boolean

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

        this.moderationSilent = moderationSilent
        this.moderationChannelId = moderationChannelId

        this.permissions = permissions

        this.logsChannelId = logsChannelId
        this.moderationLogsChannelId = moderationLogsChannelId

        this.antiLinksEnabled = antiLinksEnabled
        this.antiLinksAllowedLinks = antiLinksAllowedLinks
        this.antiLinksChannelId = antiLinksChannelId
        this.antiLinksIgnoredRoles = antiLinksIgnoredRoles
        this.antiLinksIgnoredChannels = antiLinksIgnoredChannels
        this.antiPhishingEnabled = antiPhishingEnabled

        this.customCommands = customCommands

        this.twitchChannel = twitchChannel
        this.twitchAnnounceChannelId = twitchAnnounceChannelId
        this.twitchAnnounceMessage = twitchAnnounceMessage
        this.twitchLiveChannelId = twitchLiveChannelId
        this.twitchOpenLiveMessage = twitchOpenLiveMessage
        this.twitchCloseLiveMessage = twitchCloseLiveMessage

        this.sanctionMessage = sanctionMessage
        this.antiLinksNewLinkMessage = antiLinksNewLinkMessage
        this.antiLinksUnderRevisionMessage = antiLinksUnderRevisionMessage
        this.antiLinksSanctionMessage = antiLinksSanctionMessage

        this.suggestChannel = suggestChannel
        this.suggestCreateThread = suggestCreateThread

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
                val statement = connection!!.prepareStatement("UPDATE guilds SET prefix = ?, welcome_role_id = ?, welcome_channel_id = ?, welcome_message = ?, mute_role_id = ?, moderation_silent = ?, moderation_channel_id = ?, permissions = ?, logs_channel_id = ?, moderation_logs_channel_id = ?, anti_links_enabled = ?, anti_links_allowed_links = ?, anti_links_channel_id = ?, anti_links_ignored_roles = ?, anti_links_ignored_channels = ?, anti_phishing_enabled = ?, custom_commands = ?, twitch_channel = ?, twitch_announce_channel_id = ?, twitch_announce_message = ?, twitch_live_channel_id = ?, twitch_open_live_message = ?, twitch_close_live_message = ?, sanction_message = ?, anti_links_new_link_message = ?, anti_links_under_revision_message = ?, anti_links_sanction_message = ?, suggest_channel = ?, suggest_create_thread = ?, raw = ? WHERE id = ?")
                statement.setString(1, prefix)

                statement.setString(2, welcomeRoleId)
                statement.setString(3, welcomeChannelId)
                statement.setString(4, welcomeMessage)

                statement.setString(5, muteRoleId)

                statement.setBoolean(6, moderationSilent)
                statement.setString(7, moderationChannelId)
                statement.setString(8, JSONObject().let {
                    permissions.forEach { (key, value) ->
                        it.put(key, value)
                    }
                    it
                }.toString())

                statement.setString(9, logsChannelId)
                statement.setString(10, moderationLogsChannelId)

                statement.setBoolean(11, antiLinksEnabled)
                statement.setArray(12, connection.createArrayOf("text", antiLinksAllowedLinks))
                statement.setString(13, antiLinksChannelId)
                statement.setArray(14, connection.createArrayOf("text", antiLinksIgnoredRoles))
                statement.setArray(15, connection.createArrayOf("text", antiLinksIgnoredChannels))
                statement.setBoolean(16, antiPhishingEnabled)

                statement.setArray(17, connection.createArrayOf("json", customCommands))

                statement.setString(18, twitchChannel)
                statement.setString(19, twitchAnnounceChannelId)
                statement.setString(20, twitchAnnounceMessage)
                statement.setString(21, twitchLiveChannelId)
                statement.setString(22, twitchOpenLiveMessage)
                statement.setString(23, twitchCloseLiveMessage)

                statement.setString(24, sanctionMessage)
                statement.setString(25, antiLinksNewLinkMessage)
                statement.setString(26, antiLinksUnderRevisionMessage)
                statement.setString(27, antiLinksSanctionMessage)

                statement.setString(28, suggestChannel)
                statement.setBoolean(29, suggestCreateThread)

                statement.setString(30, raw)
                statement.setString(31, id)

                statement.execute()
            }
        } else {
            database.Postgres.dataSource?.connection.use { connection ->
                val statement =
                    connection!!.prepareStatement("INSERT INTO guilds (id, prefix, welcome_role_id, welcome_channel_id, welcome_message, mute_role_id, moderation_silent, moderation_channel_id, permissions, logs_channel_id, moderation_logs_channel_id, anti_links_enabled, anti_links_allowed_links, anti_links_channel_id, anti_links_ignored_roles, anti_links_ignored_channels, anti_phishing_enabled, custom_commands, twitch_channel, twitch_announce_channel_id, twitch_announce_message, twitch_live_channel_id, twitch_open_live_message, twitch_close_live_message, sanction_message, anti_links_new_link_message, anti_links_under_revision_message, anti_links_sanction_message, suggest_channel, suggest_create_thread, raw) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")
                statement.setString(1, id)
                statement.setString(2, prefix)

                statement.setString(3, welcomeRoleId)
                statement.setString(4, welcomeChannelId)
                statement.setString(5, welcomeMessage)

                statement.setString(6, muteRoleId)

                statement.setBoolean(7, moderationSilent)
                statement.setString(8, moderationChannelId)
                statement.setString(9, JSONObject().let {
                    permissions.forEach { (key, value) ->
                        it.put(key, value)
                    }
                    it
                }.toString())

                statement.setString(10, logsChannelId)
                statement.setString(11, moderationLogsChannelId)

                statement.setBoolean(12, antiLinksEnabled)
                statement.setArray(13, connection.createArrayOf("text", antiLinksAllowedLinks))
                statement.setString(14, antiLinksChannelId)
                statement.setArray(15, connection.createArrayOf("text", antiLinksIgnoredRoles))
                statement.setArray(16, connection.createArrayOf("text", antiLinksIgnoredChannels))
                statement.setBoolean(17, antiPhishingEnabled)

                statement.setArray(18, connection.createArrayOf("json", customCommands))

                statement.setString(19, twitchChannel)
                statement.setString(20, twitchAnnounceChannelId)
                statement.setString(21, twitchAnnounceMessage)
                statement.setString(22, twitchLiveChannelId)
                statement.setString(23, twitchOpenLiveMessage)
                statement.setString(24, twitchCloseLiveMessage)

                statement.setString(25, sanctionMessage)
                statement.setString(26, antiLinksNewLinkMessage)
                statement.setString(27, antiLinksUnderRevisionMessage)
                statement.setString(28, antiLinksSanctionMessage)

                statement.setString(29, suggestChannel)
                statement.setBoolean(30, suggestCreateThread)

                statement.setString(31, raw)

                statement.execute()
            }
        }

        isSaved = true

        if(Redis.usingRedis)
            Redis.connection!!.setex("guilds:${id}", 3600, Gson().toJson(this))

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
                moderation_silent BOOLEAN NOT NULL,
                moderation_channel_id TEXT NOT NULL,
                permissions TEXT NOT NULL,
                logs_channel_id TEXT NOT NULL,
                moderation_logs_channel_id TEXT NOT NULL,
                anti_links_enabled BOOLEAN NOT NULL,
                anti_links_allowed_links TEXT[] NOT NULL,
                anti_links_channel_id TEXT NOT NULL,
                anti_links_ignored_roles TEXT[] NOT NULL,
                anti_links_ignored_channels TEXT[] NOT NULL,
                anti_phishing_enabled BOOLEAN NOT NULL,
                custom_commands JSON[] NOT NULL,
                twitch_channel TEXT NOT NULL,
                twitch_announce_channel_id TEXT NOT NULL,
                twitch_announce_message TEXT NOT NULL,
                twitch_live_channel_id TEXT NOT NULL,
                twitch_open_live_message TEXT NOT NULL,
                twitch_close_live_message TEXT NOT NULL,
                sanction_message TEXT NOT NULL,
                anti_links_new_link_message TEXT NOT NULL,
                anti_links_under_revision_message TEXT NOT NULL,
                anti_links_sanction_message TEXT NOT NULL,
                suggest_channel TEXT NOT NULL,
                suggest_create_thread BOOLEAN NOT NULL,
                raw TEXT NOT NULL
            );"""
                )

                statement.execute()
            }
        }

        fun get(id: String, force: Boolean = false): Guild? {

            if (Redis.usingRedis)
                if (!force) {
                    val cache = Redis.connection!!.get("guilds:$id")
                    if (cache != null) {
                        return Gson().fromJson(cache, Guild::class.java)
                    }
                }

            database.Postgres.dataSource?.connection.use { connection ->
                val statement = connection!!.prepareStatement("SELECT * FROM guilds WHERE id = ?")
                statement.setString(1, id)
                val result = statement.executeQuery()
                if (result.next()) {

                    val config = formatGuild(result)

                    if (Redis.usingRedis)
                        Redis.connection!!.setex("guilds:$id", 3600, Gson().toJson(config))
                    return config
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
                    guilds.add(formatGuild(result))
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
                    guilds.add(formatGuild(result))
                }
                return guilds
            }
        }

        fun getGuildsWithTwitchSubscription(channel: String): List<Guild> {
            database.Postgres.dataSource?.connection.use {connection ->
                val statement = connection!!.prepareStatement("SELECT * FROM guilds WHERE twitch_channel = ?")
                statement.setString(1, channel)
                val result = statement.executeQuery()

                val guilds = mutableListOf<Guild>()
                while (result.next()) {
                    guilds.add(formatGuild(result))
                }
                return guilds
            }
        }

        private fun formatGuild(result: ResultSet): Guild {
            return Guild(
                result.getString("id"),
                result.getString("prefix"),

                result.getString("welcome_role_id"),
                result.getString("welcome_channel_id"),
                result.getString("welcome_message"),

                result.getString("mute_role_id"),

                result.getBoolean("moderation_silent"),
                result.getString("moderation_channel_id"),
                result.getObject("permissions").toString().let {
                    val map = mutableMapOf<String, Int>()
                    JSONObject(it).keys().forEach { key ->
                        map[key] = JSONObject(it).getInt(key)
                    }
                    map
                },

                result.getString("logs_channel_id"),
                result.getString("moderation_logs_channel_id"),

                result.getBoolean("anti_links_enabled"),
                result.getArray("anti_links_allowed_links").array as Array<String>,
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
                result.getString("twitch_open_live_message"),
                result.getString("twitch_close_live_message"),

                result.getString("sanction_message"),
                result.getString("anti_links_new_link_message"),
                result.getString("anti_links_under_revision_message"),
                result.getString("anti_links_sanction_message"),

                result.getString("suggest_channel"),
                result.getBoolean("suggest_create_thread"),

                result.getString("raw")
            )
        }
    }
}