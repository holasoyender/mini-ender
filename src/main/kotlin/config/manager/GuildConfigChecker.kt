package config.manager

class GuildConfigChecker(
    config: Map<String, Any>
) {

    private val config: Map<String, Any>

    init {
        this.config = config
    }

    fun verify(): Pair<Boolean, String> {
        return Pair(false, "error jasjdjsajd")

    }

}