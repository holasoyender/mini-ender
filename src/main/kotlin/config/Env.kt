package config

import io.github.cdimascio.dotenv.Dotenv
import kotlin.system.exitProcess

object Env {
    var TOKEN: String? = null
    var PREFIX: String? = null

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