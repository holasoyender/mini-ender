package database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import config.Env
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

object Postgres {

    var dataSource: HikariDataSource? = null
    private val logger = LoggerFactory.getLogger(Postgres::class.java)

    init {
        try {

            val props = HikariConfig()
            props.jdbcUrl = "jdbc:postgresql://${Env.POSTGRES_HOST}/${Env.POSTGRES_DB}"
            props.username = Env.POSTGRES_USER
            props.password = Env.POSTGRES_PASSWORD
            props.addDataSourceProperty("ssl", Env.POSTGRES_SSL.toString())
            props.addDataSourceProperty("ApplicationName", "Cliente de mini-ender")
            props.connectionTimeout = 3000
            props.maximumPoolSize = 4

            val conn = HikariDataSource(props)

            if (conn.connection.isValid(1000)) {
                logger.info("Conectado con éxito a la base de datos")
                dataSource = conn
            } else {
                logger.error("Error al conectar con la base de datos")
                exitProcess(1)
            }
        } catch (e: Exception) {
            logger.error("Error al conectar con la base de datos", e)
            exitProcess(1)
        }
    }

    fun load() {

        database.schema.Guild.createTable()
        
    }
}