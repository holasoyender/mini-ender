package plugins.modchannel

import net.dv8tion.jda.api.entities.Message.Attachment

class ParsedMessage(
    users: List<String>,
    attachments: List<Attachment>,
    reason: String,
    duration: String
) {

    val users: List<String>
    val attachments: List<Attachment>

    val reason: String
    val duration: String

    init {
        this.users = users
        this.attachments = attachments
        this.reason = reason
        this.duration = duration
    }
}