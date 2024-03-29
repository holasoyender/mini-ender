package database.schema

import interfaces.Schema
import org.json.JSONArray

class Sorteo(
    guildId: String,
    channelId: String,
    messageId: String,
    hostId: String,

    endAfter: Long,
    startedAt: Long,

    prize: String,
    winnerCount: Int,

    ended: Boolean,
    winnerIds: Array<String>,

    clickers: Array<String>,
    style: String
): Schema {

    var guildId: String
    var channelId: String
    var messageId: String
    var hostId: String

    var endAfter: Long
    var startedAt: Long

    var prize: String
    var winnerCount: Int

    var ended: Boolean
    var winnerIds: Array<String>

    var clickers: Array<String>
    var style: String

    private var isSaved = false
    private var isDeleted = false


    init {
        this.guildId = guildId
        this.channelId = channelId
        this.messageId = messageId
        this.hostId = hostId
        this.endAfter = endAfter
        this.startedAt = startedAt
        this.prize = prize
        this.winnerCount = winnerCount
        this.ended = ended
        this.winnerIds = winnerIds
        this.clickers = clickers
        this.style = style

        if (exists()) {
            isSaved = true
        }
    }

    override val tableName: String
        get() = "sorteos"

    override fun dropTable() {
        database.Database.dataSource?.connection.use { connection ->
            val statement = connection!!.prepareStatement("DROP TABLE IF EXISTS sorteos")
            statement.execute()
        }
    }

    override fun save(): Sorteo {

        if (exists()) {
            database.Database.dataSource?.connection.use { connection ->
                val statement = connection!!.prepareStatement("UPDATE sorteos SET guild_id = ?, channel_id = ?, message_id = ?, host_id = ?, end_after = ?, started_at = ?, prize = ?, winner_count = ?, ended = ?, winner_ids = ?, clickers = ?, style = ? WHERE message_id = ?")
                statement.setString(1, guildId)
                statement.setString(2, channelId)
                statement.setString(3, messageId)
                statement.setString(4, hostId)
                statement.setLong(5, endAfter)
                statement.setLong(6, startedAt)
                statement.setString(7, prize)
                statement.setInt(8, winnerCount)
                statement.setBoolean(9, ended)
                statement.setString(10, JSONArray().let {
                    winnerIds.forEach { id ->
                        it.put(id)
                    }
                    it
                }.toString())
                //statement.setArray(10, connection.createArrayOf("text", winnerIds))
                statement.setString(11, JSONArray().let {
                    clickers.forEach { id ->
                        it.put(id)
                    }
                    it
                }.toString())
                //statement.setArray(11, connection.createArrayOf("text", clickers))
                statement.setString(12, style)
                statement.setString(13, messageId)
                statement.execute()
            }
        } else {
            database.Database.dataSource?.connection.use { connection ->
                val statement =
                    connection!!.prepareStatement("INSERT INTO sorteos (guild_id, channel_id, message_id, host_id, end_after, started_at, prize, winner_count, ended, winner_ids, clickers, style) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")
                statement.setString(1, guildId)
                statement.setString(2, channelId)
                statement.setString(3, messageId)
                statement.setString(4, hostId)
                statement.setLong(5, endAfter)
                statement.setLong(6, startedAt)
                statement.setString(7, prize)
                statement.setInt(8, winnerCount)
                statement.setBoolean(9, ended)
                statement.setString(10, JSONArray().let {
                    winnerIds.forEach { id ->
                        it.put(id)
                    }
                    it
                }.toString())
                //statement.setArray(10, connection.createArrayOf("text", winnerIds))
                statement.setString(11, JSONArray().let {
                    clickers.forEach { id ->
                        it.put(id)
                    }
                    it
                }.toString())
                //statement.setArray(11, connection.createArrayOf("text", clickers))
                statement.setString(12, style)
                statement.execute()
            }
        }

        isSaved = true
        return this
    }

    override fun delete(): Sorteo {

        if (isDeleted)
            return this

        if(!exists())
            return this

        database.Database.dataSource?.connection.use { connection ->
            val statement = connection!!.prepareStatement("DELETE FROM sorteos WHERE message_id = ?")
            statement.setString(1, messageId)
            statement.execute()
        }
        isSaved = false
        isDeleted = true
        return this
    }

    override fun exists(): Boolean {
        database.Database.dataSource?.connection.use { connection ->
            val statement = connection!!.prepareStatement("SELECT * FROM sorteos WHERE message_id = ?")
            statement.setString(1, messageId)
            val result = statement.executeQuery()
            return result.next()
        }
    }

    companion object {
        fun createTable() {

            database.Database.dataSource?.connection.use { connection ->
                val statement = connection!!.prepareStatement(
                    """
            CREATE TABLE IF NOT EXISTS sorteos ( 
                guild_id TEXT NOT NULL,
                channel_id TEXT NOT NULL,
                message_id TEXT NOT NULL,
                host_id TEXT NOT NULL,
                end_after BIGINT NOT NULL,
                started_at BIGINT NOT NULL,
                prize TEXT NOT NULL,
                winner_count INT NOT NULL,
                ended BOOLEAN NOT NULL,
                winner_ids TEXT NOT NULL,
                clickers TEXT NOT NULL,
                style TEXT NOT NULL
            );"""
                )

                statement.execute()
            }
        }

        fun get(messageId: String): Sorteo? {
            database.Database.dataSource?.connection.use { connection ->
                val statement = connection!!.prepareStatement("SELECT * FROM sorteos WHERE message_id = ?")
                statement.setString(1, messageId)
                val result = statement.executeQuery()
                if (result.next()) {
                    return Sorteo(
                        result.getString("guild_id"),
                        result.getString("channel_id"),
                        result.getString("message_id"),
                        result.getString("host_id"),
                        result.getLong("end_after"),
                        result.getLong("started_at"),
                        result.getString("prize"),
                        result.getInt("winner_count"),
                        result.getBoolean("ended"),
                        JSONArray(result.getString("winner_ids")).map { it as String }.toTypedArray(),
                        //result.getArray("winner_ids").array as Array<String>,
                        JSONArray(result.getString("clickers")).map { it as String }.toTypedArray(),
                        //result.getArray("clickers").array as Array<String>,
                        result.getString("style")
                    )
                }
            }
            return null
        }

        @Suppress("unused")
        fun getAll(): List<Sorteo> {
            database.Database.dataSource?.connection.use { connection ->
                val statement = connection!!.prepareStatement("SELECT * FROM sorteos")
                val result = statement.executeQuery()

                val sorteos = mutableListOf<Sorteo>()
                while (result.next()) {
                    sorteos.add(
                        Sorteo(
                            result.getString("guild_id"),
                            result.getString("channel_id"),
                            result.getString("message_id"),
                            result.getString("host_id"),
                            result.getLong("end_after"),
                            result.getLong("started_at"),
                            result.getString("prize"),
                            result.getInt("winner_count"),
                            result.getBoolean("ended"),
                            JSONArray(result.getString("winner_ids")).map { it as String }.toTypedArray(),
                            //result.getArray("winner_ids").array as Array<String>,
                            JSONArray(result.getString("clickers")).map { it as String }.toTypedArray(),
                            //result.getArray("clickers").array as Array<String>,
                            result.getString("style")
                        )
                    )
                }
                return sorteos
            }
        }
    }
}