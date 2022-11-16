package config

import database.schema.Guild

object DefaultConfig {

    fun get(): Guild {
        return Guild("", Env.PREFIX ?: "-", arrayOf(), "", false, false, "")
    }

    fun get(guildID: String): Guild {
        return Guild(guildID, Env.PREFIX ?: "-", arrayOf(), "", false, false,"")
    }
}