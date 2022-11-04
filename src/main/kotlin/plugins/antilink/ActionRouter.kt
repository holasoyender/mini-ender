package plugins.antilink

import database.schema.Links
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User

object ActionRouter {

    /*
    * TODO:
    *   - Sistema de infracciones para guardar cada evento
    *   - Hace que esto funcione (xd)
    * */
    fun ban(user: User, guild: Guild, link: Links): Boolean {
        return true

    }

    fun kick(user: User, guild: Guild, link: Links): Boolean {
        return true

    }

    fun mute(user: User, guild: Guild, link: Links): Boolean {
        return true

    }

    fun warn(user: User, guild: Guild, link: Links): Boolean {
        return true

    }

    fun tempBan(user: User, guild: Guild, link: Links): Boolean {
        return true

    }

    fun tempMute(user: User, guild: Guild, link: Links): Boolean {
        return true

    }
}