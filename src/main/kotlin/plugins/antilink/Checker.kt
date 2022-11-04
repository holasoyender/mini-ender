package plugins.antilink

import java.net.URL

class Checker(message: String) {

    val link: String
    private val rawMessage: String
    private val linkRegex: Regex = Regex("(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]")

    init {
        rawMessage = message
        link = if(linkRegex.containsMatchIn(message)) {
            linkRegex.find(message)!!.value
        } else {
            ""
        }
    }

    val isLink:Boolean
        get() {
            return try {
                URL(link)
                true
            } catch (e: Exception) {
                false
            }
        }

    val isDiscordInvite:Boolean
        get() {
            return rawMessage.contains("discord.gg") || rawMessage.contains("discord.com/invite")
        }

    val domain: String
        get() {
            return try {
                URL(link).host
            } catch (e: Exception) {
                ""
            }
        }

}