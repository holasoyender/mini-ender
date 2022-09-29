package interfaces

import net.dv8tion.jda.api.interactions.components.ActionRow

class SimpleCommand(name: String, aliases: List<String>, response: String, reply: Boolean, components: List<ActionRow>) {
    var name: String = ""
    var aliases: List<String> = listOf()
    var response: String = ""
    var reply: Boolean = true
    var components: List<ActionRow> = listOf()

    init {
        this.name = name
        this.aliases = aliases
        this.response = response
        this.reply = reply
        this.components = components
    }
}