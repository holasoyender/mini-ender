package config.manager

class GuildConfigValidator(
    config: Map<String, Any>
) {

    private val config: Map<String, Any>

    init {
        this.config = config
    }

    fun validate(): Pair<Boolean, String> {
        //TODO: Literalmente todo
        return Pair(false, "no me da la gana")
    }
}