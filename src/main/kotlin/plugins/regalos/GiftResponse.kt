package plugins.regalos

import net.dv8tion.jda.api.EmbedBuilder

class GiftResponse(
    message: String,
    embed: EmbedBuilder?,
    attachmentUrl: String?,
    ephemeral: Boolean
) {
    val message: String
    val embed: EmbedBuilder?
    val attachmentUrl: String?
    val ephemeral: Boolean

    init {
        this.message = message
        this.embed = embed
        this.attachmentUrl = attachmentUrl
        this.ephemeral = ephemeral
    }
}