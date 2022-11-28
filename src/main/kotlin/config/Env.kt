package config

import io.github.cdimascio.dotenv.Dotenv
import kotlin.system.exitProcess

object Env {
    var TOKEN: String? = null
    var PREFIX: String? = null
    var API_TOKEN: String? = null
    var API_URL: String? = null

    var POSTGRES_USER: String? = null
    var POSTGRES_PASSWORD: String? = null
    var POSTGRES_HOST: String? = null
    var POSTGRES_DB: String? = null
    var POSTGRES_SSL: Boolean? = null
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
        POSTGRES_USER = getEnv("POSTGRES_USER", dotenv)
        POSTGRES_PASSWORD = getEnv("POSTGRES_PASSWORD", dotenv)
        POSTGRES_HOST = getEnv("POSTGRES_HOST", dotenv)
        POSTGRES_DB = getEnv("POSTGRES_DB", dotenv)
        POSTGRES_SSL = getEnv("POSTGRES_SSL", dotenv)?.toBoolean()
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