package interfaces

class SimpleCommand(name: String, aliases: List<String>, response: String) {
    var name: String = ""
    var aliases: List<String> = listOf()
    var response: String = ""

    init {
        this.name = name
        this.aliases = aliases
        this.response = response
    }
}