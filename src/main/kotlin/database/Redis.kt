package database

import config.Env
import org.slf4j.LoggerFactory
import redis.clients.jedis.JedisPool

object Redis {

    var connection: redis.clients.jedis.Jedis? = null
    var usingRedis = false
    private val logger = LoggerFactory.getLogger(Redis::class.java)

    init {

        try {

            val host = Env.REDIS_HOST?.split(":")?.getOrNull(0) ?: "localhost"
            val port = try { Env.REDIS_HOST?.split(":")?.getOrNull(1)?.toInt() ?: 6379 } catch (e: Exception) { 6379 }

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
                logger.info("Conectado con éxito a la base de datos de cache")
                connection = conn.resource
                usingRedis = true
            } else {
                logger.error("Error al conectar con la base de datos de Redis, no se utilizará un servidor de cache.")
            }

        } catch (e: Exception) {
            logger.error("Error al conectar con la base de datos de Redis, no se utilizará un servidor de cache.")
        }
    }

    fun load() = Unit
}