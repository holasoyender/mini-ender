package database.schema

import interfaces.Schema

class Error(
    id: String,
    userid: String,
    error: String,
    date: String,
    acknowledged: Boolean,
    solved: Boolean,
): Schema {

    var id: String
    var userid: String
    var error: String
    var date: String
    var acknowledged: Boolean
    var solved: Boolean

    private var isSaved = false
    private var isDeleted = false


    init {
        this.id = id
        this.userid = userid
        this.error = error
        this.date = date
        this.acknowledged = acknowledged
        this.solved = solved

        if (exists()) {
            isSaved = true
        }
    }

    override val tableName: String
        get() = "errors"

    override fun dropTable() {
        database.Postgres.dataSource?.connection.use { connection ->
            val statement = connection!!.prepareStatement("DROP TABLE IF EXISTS errors")
            statement.execute()
        }
    }

    override fun save(): Error {

        if (exists()) {
            database.Postgres.dataSource?.connection.use { connection ->
                val statement = connection!!.prepareStatement("UPDATE errors SET userid = ?, error = ?, date = ?, acknowledged = ?, solved = ? WHERE id = ?")
                statement.setString(1, userid)
                statement.setString(2, error)
                statement.setString(3, date)
                statement.setBoolean(4, acknowledged)
                statement.setBoolean(5, solved)
                statement.setString(6, id)

                statement.execute()
            }
        } else {
            database.Postgres.dataSource?.connection.use { connection ->
                val statement =
                    connection!!.prepareStatement("INSERT INTO errors (id, userid, error, date, acknowledged, solved) VALUES (?, ?, ?, ?, ?, ?)")
                statement.setString(1, id)
                statement.setString(2, userid)
                statement.setString(3, error)
                statement.setString(4, date)
                statement.setBoolean(5, acknowledged)
                statement.setBoolean(6, solved)

                statement.execute()
            }
        }

        isSaved = true
        return this
    }

    override fun delete(): Error {

        if (isDeleted)
            return this

        if(!exists())
            return this

        database.Postgres.dataSource?.connection.use { connection ->
            val statement = connection!!.prepareStatement("DELETE FROM errors WHERE id = ?")
            statement.setString(1, id)
            statement.execute()
        }
        isSaved = false
        isDeleted = true
        return this
    }

    override fun exists(): Boolean {
        database.Postgres.dataSource?.connection.use { connection ->
            val statement = connection!!.prepareStatement("SELECT * FROM errors WHERE id = ?")
            statement.setString(1, id)
            val result = statement.executeQuery()
            return result.next()
        }
    }

    companion object {
        fun createTable() {

            database.Postgres.dataSource?.connection.use { connection ->
                val statement = connection!!.prepareStatement(
                    """
            CREATE TABLE IF NOT EXISTS errors (
                id TEXT PRIMARY KEY,
                userid TEXT NOT NULL,
                error TEXT NOT NULL,
                date TEXT NOT NULL,
                acknowledged BOOLEAN NOT NULL,
                solved BOOLEAN NOT NULL
            );"""
                )

                statement.execute()
            }
        }

        fun get(id: String): Error? {
            database.Postgres.dataSource?.connection.use { connection ->
                val statement = connection!!.prepareStatement("SELECT * FROM errors WHERE id = ?")
                statement.setString(1, id)
                val result = statement.executeQuery()
                if (result.next()) {
                    return Error(
                        result.getString("id"),
                        result.getString("userid"),
                        result.getString("error"),
                        result.getString("date"),
                        result.getBoolean("acknowledged"),
                        result.getBoolean("solved"),
                    )
                }
            }
            return null
        }

        fun getAll(): List<Error> {
            database.Postgres.dataSource?.connection.use {connection ->
                val statement = connection!!.prepareStatement("SELECT * FROM errors")
                val result = statement.executeQuery()

                val errors = mutableListOf<Error>()
                while (result.next()) {
                    errors.add(
                        Error(
                            result.getString("id"),
                            result.getString("userid"),
                            result.getString("error"),
                            result.getString("date"),
                            result.getBoolean("acknowledged"),
                            result.getBoolean("solved"),
                        )
                    )
                }
                return errors
            }
        }
    }
}