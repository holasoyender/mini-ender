package config

import io.github.cdimascio.dotenv.Dotenv
import kotlin.system.exitProcess

object Env {
    var TOKEN: String? = null
    var PREFIX: String? = null
    var API_TOKEN: String? = null
    var API_URL: String? = null

    var DATABASE_USER: String? = null
    var DATABASE_PASSWORD: String? = null
    var DATABASE_HOST: String? = null
    var DATABASE_SSL: Boolean? = null

    var REDIS_USER: String? = null
    var REDIS_PASSWORD: String? = null
    var REDIS_HOST: String? = null

    var OAUTH2_CLIENT_ID: String? = null
    var OAUTH2_CLIENT_SECRET: String? = null

    var ERROR_CHANNEL_ID: String? = null

    var TWITCH_CLIENT_ID: String? = null
    var TWITCH_CLIENT_SECRET: String? = null

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
        API_TOKEN = getEnv("API_TOKEN", dotenv)
        API_URL = getEnv("API_URL", dotenv)
        DATABASE_USER = getEnv("DATABASE_USER", dotenv)
        DATABASE_PASSWORD = getEnv("DATABASE_PASSWORD", dotenv)
        DATABASE_HOST = getEnv("DATABASE_HOST", dotenv)
        DATABASE_SSL = getEnv("DATABASE_SSL", dotenv)?.toBoolean()
        REDIS_USER = getEnv("REDIS_USER", dotenv)
        REDIS_PASSWORD = getEnv("REDIS_PASSWORD", dotenv)
        REDIS_HOST = getEnv("REDIS_HOST", dotenv)
        ERROR_CHANNEL_ID = getEnv("ERROR_CHANNEL_ID", dotenv)
        OAUTH2_CLIENT_ID = getEnv("OAUTH2_CLIENT_ID", dotenv)
        OAUTH2_CLIENT_SECRET = getEnv("OAUTH2_CLIENT_SECRET", dotenv)
        TWITCH_CLIENT_ID = getEnv("TWITCH_CLIENT_ID", dotenv)
        TWITCH_CLIENT_SECRET = getEnv("TWITCH_CLIENT_SECRET", dotenv)

        System.setProperty("SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_DISCORD_CLIENT_ID", OAUTH2_CLIENT_ID ?: "Invalid")
        System.setProperty("SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_DISCORD_CLIENT_SECRET", OAUTH2_CLIENT_SECRET ?: "Invalid")

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