package database

import config.Env
import org.slf4j.LoggerFactory
import redis.clients.jedis.JedisPool
import kotlin.system.exitProcess

object Redis {

    var connection: redis.clients.jedis.Jedis? = null
    private val logger = LoggerFactory.getLogger(Redis::class.java)

    init {

        try {

            val host = Env.REDIS_HOST!!.split(":")[0]
            val port = try { Env.REDIS_HOST!!.split(":")[1].toInt() } catch (e: Exception) { 6379 }

            val conn = if(Env.REDIS_USER == null || Env.REDIS_USER!!.isBlank())
                JedisPool(host,port)
            else
                JedisPool(
                    host,
                    port,
                    Env.REDIS_USER,
                    Env.REDIS_PASSWORD
                )

            if (conn.resource.ping() == "PONG") {
                logger.info("Conectado con éxito a la base de datos")
                connection = conn.resource
            } else {
                logger.error("Error al conectar con la base de datos")
                exitProcess(1)
            }

        } catch (e: Exception) {
            logger.error("Error al conectar con la base de datos", e)
            exitProcess(1)
        }
    }

    fun load() = Unit
}