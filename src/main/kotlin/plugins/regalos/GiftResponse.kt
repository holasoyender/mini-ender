package plugins.regalos

import net.dv8tion.jda.api.EmbedBuilder

class GiftResponse(
    message: String,
    embed: EmbedBuilder?,
    ephemeral: Boolean
) {
    val message: String
    val embed: EmbedBuilder?
    val ephemeral: Boolean

    init {
        this.message = message
        this.embed = embed
        this.ephemeral = ephemeral
    }
}