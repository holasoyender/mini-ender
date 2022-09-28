package interfaces

import net.dv8tion.jda.api.interactions.components.ActionRow

class SimpleCommand(name: String, aliases: List<String>, response: String, components: List<ActionRow>) {
    var name: String = ""
    var aliases: List<String> = listOf()
    var response: String = ""
    var components: List<ActionRow> = listOf()

    init {
        this.name = name
        this.aliases = aliases
        this.response = response
        this.components = components
    }
}