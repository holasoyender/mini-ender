package config.manager

class GuildConfigValidator(
    config: Map<String, Any>
) {

    private val config: Map<String, Any>

    init {
        this.config = config
    }

    fun validate(): Pair<Boolean, String> {

        val (prefix, prefixError) = prefix()
        if (!prefix) return Pair(false, prefixError)

        val (welcomes, welcomesError) = welcomes()
        if (!welcomes) return Pair(false, welcomesError)

        val (roles, rolesError) = roles()
        if (!roles) return Pair(false, rolesError)

        val (logs, logsError) = logs()
        if (!logs) return Pair(false, logsError)

        val (antiLinks, antiLinksError) = antiLinks()
        if (!antiLinks) return Pair(false, antiLinksError)

        val (twitch, twitchError) = twitch()
        if (!twitch) return Pair(false, twitchError)

        val (customCommands, customCommandsError) = customCommands()
        if (!customCommands) return Pair(false, customCommandsError)

        return Pair(true, "")
    }

    private fun prefix(): Pair<Boolean, String> {
        val prefix = config["prefix"] ?: return Pair(false, "El prefijo no está definido")
        if(prefix !is String) return Pair(false, "El prefijo no es un String")
        if(prefix.length > 5) return Pair(false, "El prefijo no puede tener más de 5 caracteres")
        if(prefix.length < 1) return Pair(false, "El prefijo no puede tener menos de 1 carácter")
        return Pair(true, "")
    }

    private fun welcomes(): Pair<Boolean, String> {
        val welcomes = config["welcomes"] ?: return Pair(false, "El modulo de bienvenidas no está definido")
        if(welcomes !is Map<*, *>) return Pair(false, "El modulo de bienvenidas no está bien definido")
        if(welcomes["role_id"] !is String) return Pair(false, "El rol de bienvenida no está bien definido")
        if(!isIdOrEmpty(welcomes["role_id"] as String)) return Pair(false, "El rol de bienvenida no es un ID válido")
        if(welcomes["channel_id"] !is String) return Pair(false, "El canal de bienvenida no está bien definido")
        if(!isIdOrEmpty(welcomes["channel_id"] as String)) return Pair(false, "El canal de bienvenida no es un ID válido")
        if(welcomes["message"] !is String) return Pair(false, "El mensaje de bienvenida no está bien definido")
        return Pair(true, "")
    }

    private fun roles(): Pair<Boolean, String> {
        val roles = config["roles"] ?: return Pair(false, "El modulo de roles no está definido")
        if(roles !is Map<*, *>) return Pair(false, "El modulo de roles no está bien definido")
        if(roles["mute_role_id"] !is String) return Pair(false, "El rol de mute no está bien definido")
        if(!isIdOrEmpty(roles["mute_role_id"] as String)) return Pair(false, "El rol de mute no es un ID válido")
        return Pair(true, "")
    }

    private fun logs(): Pair<Boolean, String> {
        val logs = config["logs"] ?: return Pair(false, "El modulo de logs no está definido")
        if(logs !is Map<*, *>) return Pair(false, "El modulo de logs no está bien definido")
        if(logs["channel_id"] !is String) return Pair(false, "El canal de logs no está bien definido")
        if(!isIdOrEmpty(logs["channel_id"] as String)) return Pair(false, "El canal de logs no es un ID válido")
        return Pair(true, "")
    }

    private fun antiLinks(): Pair<Boolean, String> {
        val antiLinks = config["anti_links"] ?: return Pair(false, "El modulo de anti-links no está definido")
        if(antiLinks !is Map<*, *>) return Pair(false, "El modulo de anti-links no está bien definido")
        if(antiLinks["enabled"] !is Boolean) return Pair(false, "El estado de anti-links no está bien definido")
        if(antiLinks["channel_id"] !is String) return Pair(false, "El canal de anti-links no está bien definido")
        if(!isIdOrEmpty(antiLinks["channel_id"] as String)) return Pair(false, "El canal de anti-links no es un ID válido")

        if(antiLinks["ignore_roles"] !is List<*>) return Pair(false, "La lista de roles ignorados no está bien definida")
        val ignoreRoles = antiLinks["ignore_roles"] as List<*>
        for(role in ignoreRoles) {
            if(role !is String) return Pair(false, "La lista de roles ignorados no está bien definida (ID en la posición ${ignoreRoles.indexOf(role)} no es válida)")
            if(!isId(role)) return Pair(false, "La lista de roles ignorados no está bien definida (ID en la posición ${ignoreRoles.indexOf(role)} no es válida)")
        }

        if(antiLinks["ignore_channels"] !is List<*>) return Pair(false, "La lista de canales ignorados no está bien definida")
        val ignoreChannels = antiLinks["ignore_channels"] as List<*>
        for(channel in ignoreChannels) {
            if(channel !is String) return Pair(false, "La lista de canales ignorados no está bien definida (ID en la posición ${ignoreChannels.indexOf(channel)} no es válida)")
            if(!isId(channel)) return Pair(false, "La lista de canales ignorados no está bien definida (ID en la posición ${ignoreChannels.indexOf(channel)} no es válida)")
        }

        if(antiLinks["anti_phishing"] !is Boolean) return Pair(false, "El estado de anti-phishing no está bien definido")

        return Pair(true, "")
    }

    fun twitch(): Pair<Boolean, String> {
        val twitch = config["twitch"] ?: return Pair(false, "El modulo de twitch no está definido")
        if(twitch !is Map<*, *>) return Pair(false, "El modulo de twitch no está bien definido")
        if(twitch["channel"] !is String) return Pair(false, "El canal de twitch del streamer no está bien definido")
        if(twitch["announce_channel_id"] !is String) return Pair(false, "El canal de anuncios de twitch no está bien definido")
        if(!isIdOrEmpty(twitch["announce_channel_id"] as String)) return Pair(false, "El canal de anuncios de twitch no es un ID válido")
        if(twitch["message"] !is String) return Pair(false, "El mensaje de anuncios de twitch no está bien definido")
        if(twitch["live_channel_id"] !is String) return Pair(false, "El canal de live de twitch no está bien definido")
        if(!isIdOrEmpty(twitch["live_channel_id"] as String)) return Pair(false, "El canal de live de twitch no es un ID válido")
        if(twitch["live_message"] !is String) return Pair(false, "El mensaje de live de twitch no está bien definido")

        return Pair(true, "")
    }

    private fun customCommands(): Pair<Boolean, String> {
        val customCommands = config["custom_commands"] ?: return Pair(true, "")
        if (customCommands !is Map<*, *>) return Pair(false, "El modulo de comandos personalizados no está bien definido")
        if (customCommands.isEmpty()) return Pair(true, "")

        for (command in customCommands) {
            if (command.key !is String) return Pair(false, "El comando personalizado \"${command.key}\" no está bien definido")
            if (command.value !is Map<*, *>) return Pair(false, "El comando personalizado \"${command.key}\" no está bien definido")
            val commandValue = command.value as Map<*, *>
            if (commandValue["response"] !is String) return Pair(false, "El comando personalizado \"${command.key}\" no está bien definido")
            if (commandValue["description"] !is String) return Pair(false, "El comando personalizado \"${command.key}\" no está bien definido")
            if (commandValue["aliases"] !is List<*>) return Pair(false, "El comando personalizado \"${command.key}\" no está bien definido")
            val aliases = commandValue["aliases"] as List<*>
            for (alias in aliases) {
                if (alias !is String) return Pair(false, "El comando personalizado \"${command.key}\" no está bien definido")
            }
        }

        return Pair(true, "")
    }

    private fun isId(input: String): Boolean {
        return input.matches(Regex("[0-9]{18}")) || input.matches(Regex("[0-9]{17}")) || input.matches(Regex("[0-9]{19}"))
    }

    private fun isIdOrEmpty(input: String): Boolean {
        return input.isEmpty() || isId(input)
    }
}