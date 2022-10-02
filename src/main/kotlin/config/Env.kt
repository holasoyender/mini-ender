package config

import io.github.cdimascio.dotenv.Dotenv
import kotlin.system.exitProcess

object Env {
    var TOKEN: String? = null
    var PREFIX: String? = null

    var POSTGRES_USER: String? = null
    var POSTGRES_PASSWORD: String? = null
    var POSTGRES_HOST: String? = null
    var POSTGRES_DB: String? = null
    var POSTGRES_SSL: Boolean? = null

    init {
        val dotenv: Dotenv
        try {
            dotenv = Dotenv.configure().ignoreIfMissing().ignoreIfMalformed().load()
        } catch (e: Exception) {
            println("No he encontrado el archivo .env")
            exitProcess(1)
        }

        TOKEN = getEnv("TOKEN", dotenv)
        PREFIX = getEnv("PREFIX", dotenv)
        POSTGRES_USER = getEnv("POSTGRES_USER", dotenv)
        POSTGRES_PASSWORD = getEnv("POSTGRES_PASSWORD", dotenv)
        POSTGRES_HOST = getEnv("POSTGRES_HOST", dotenv)
        POSTGRES_DB = getEnv("POSTGRES_DB", dotenv)
        POSTGRES_SSL = getEnv("POSTGRES_SSL", dotenv)?.toBoolean()

    }

    private fun getEnv(key: String, dotenv: Dotenv): String? {
        if(System.getenv(key) != null) {
            return System.getenv(key)
        } else if(dotenv.get(key) != null) {
            return dotenv.get(key)
        }
        return null
    }
}