package config.manager

import commandManager
import net.dv8tion.jda.api.entities.Guild

class GuildConfigValidator(
    config: Map<String, Any>,
    guild: Guild
) {

    private val config: Map<String, Any>
    private val guild: Guild

    init {
        this.config = config
        this.guild = guild
    }

    fun validate(): Pair<Boolean, String> {

        val (prefix, prefixError) = prefix()
        if (!prefix) return Pair(false, prefixError)

        val (welcomes, welcomesError) = welcomes()
        if (!welcomes) return Pair(false, welcomesError)

        val (roles, rolesError) = roles()
        if (!roles) return Pair(false, rolesError)

        val (moderation, moderationError) = moderation()
        if (!moderation) return Pair(false, moderationError)

        val (permissions, permissionsError) = permissions()
        if (!permissions) return Pair(false, permissionsError)

        val (logs, logsError) = logs()
        if (!logs) return Pair(false, logsError)

        val (antiLinks, antiLinksError) = antiLinks()
        if (!antiLinks) return Pair(false, antiLinksError)

        val (twitch, twitchError) = twitch()
        if (!twitch) return Pair(false, twitchError)

        val (customCommands, customCommandsError) = customCommands()
        if (!customCommands) return Pair(false, customCommandsError)

        val (messages, messagesError) = messages()
        if (!messages) return Pair(false, messagesError)

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
        if((welcomes["role_id"] as String).isNotEmpty() && !isValidRole(welcomes["role_id"] as String)) return Pair(false, "El rol de bienvenida no existe en el servidor")
        if(welcomes["channel_id"] !is String) return Pair(false, "El canal de bienvenida no está bien definido")
        if(!isIdOrEmpty(welcomes["channel_id"] as String)) return Pair(false, "El canal de bienvenida no es un ID válido")
        if((welcomes["channel_id"] as String).isNotEmpty() && !isValidChannel(welcomes["channel_id"] as String)) return Pair(false, "El canal de bienvenida no existe en el servidor")
        if(welcomes["message"] !is String) return Pair(false, "El mensaje de bienvenida no está bien definido")
        return Pair(true, "")
    }

    private fun roles(): Pair<Boolean, String> {
        val roles = config["roles"] ?: return Pair(false, "El modulo de roles no está definido")
        if(roles !is Map<*, *>) return Pair(false, "El modulo de roles no está bien definido")
        if(roles["mute_role_id"] !is String) return Pair(false, "El rol de mute no está bien definido")
        if(!isIdOrEmpty(roles["mute_role_id"] as String)) return Pair(false, "El rol de mute no es un ID válido")
        if((roles["mute_role_id"] as String).isNotEmpty() && !isValidRole(roles["mute_role_id"] as String)) return Pair(false, "El rol de mute no existe en el servidor")
        return Pair(true, "")
    }

    private fun moderation(): Pair<Boolean, String> {
        val moderation = config["moderation"] ?: return Pair(false, "El modulo de moderación no está definido")
        if(moderation !is Map<*, *>) return Pair(false, "El modulo de moderación no está bien definido")
        if(moderation["silent"] !is Boolean) return Pair(false, "El modo silencioso no está bien definido")
        if(moderation["moderation_channel_id"] !is String) return Pair(false, "El canal de moderación no está bien definido")
        if(!isIdOrEmpty(moderation["moderation_channel_id"] as String)) return Pair(false, "El canal de moderación no es un ID válido")
        if((moderation["moderation_channel_id"] as String).isNotEmpty() && !isValidChannel(moderation["moderation_channel_id"] as String)) return Pair(false, "El canal de moderación no existe en el servidor")
        return Pair(true, "")
    }

    private fun permissions(): Pair<Boolean, String> {
        val permissions = config["permissions"] ?: return Pair(false, "El modulo de permisos no está definido")
        if(permissions !is Map<*, *>) return Pair(false, "El modulo de permisos no está bien definido")
        for((key, value) in permissions) {
            if(key !is Long) return Pair(false, "El ID del rol $key no es un String")
            if(!isIdOrEmpty(key.toString())) return Pair(false, "El ID del rol $key no es un ID válido")
            if(key.toString().isNotEmpty() && !isValidRole(key.toString())) return Pair(false, "El ID del rol $key no existe en el servidor")
            if(value !is Int) return Pair(false, "El nivel del rol $key no es un Int")
            if(value < 0 || value > 5) return Pair(false, "El nivel del rol $key no es válido")
        }
        return Pair(true, "")
    }

    private fun logs(): Pair<Boolean, String> {
        val logs = config["logs"] ?: return Pair(false, "El modulo de logs no está definido")
        if(logs !is Map<*, *>) return Pair(false, "El modulo de logs no está bien definido")
        if(logs["channel_id"] !is String) return Pair(false, "El canal de logs no está bien definido")
        if(!isIdOrEmpty(logs["channel_id"] as String)) return Pair(false, "El canal de logs no es un ID válido")
        if((logs["channel_id"] as String).isNotEmpty() && !isValidChannel(logs["channel_id"] as String)) return Pair(false, "El canal de logs no existe en el servidor")
        if(logs["moderation_channel_id"] !is String) return Pair(false, "El canal de logs de moderación no está bien definido")
        if(!isIdOrEmpty(logs["moderation_channel_id"] as String)) return Pair(false, "El canal de logs de moderación no es un ID válido")
        if((logs["moderation_channel_id"] as String).isNotEmpty() && !isValidChannel(logs["moderation_channel_id"] as String)) return Pair(false, "El canal de logs de moderación no existe en el servidor")

        return Pair(true, "")
    }

    private fun antiLinks(): Pair<Boolean, String> {
        val antiLinks = config["anti_links"] ?: return Pair(false, "El modulo de anti-links no está definido")
        if(antiLinks !is Map<*, *>) return Pair(false, "El modulo de anti-links no está bien definido")
        if(antiLinks["enabled"] !is Boolean) return Pair(false, "El estado de anti-links no está bien definido")
        if(antiLinks["channel_id"] !is String) return Pair(false, "El canal de anti-links no está bien definido")
        if(!isIdOrEmpty(antiLinks["channel_id"] as String)) return Pair(false, "El canal de anti-links no es un ID válido")
        if((antiLinks["channel_id"] as String).isNotEmpty() && !isValidChannel(antiLinks["channel_id"] as String)) return Pair(false, "El canal de anti-links no existe en el servidor")

        if(antiLinks["ignore_roles"] !is List<*>) return Pair(false, "La lista de roles ignorados no está bien definida")
        val ignoreRoles = antiLinks["ignore_roles"] as List<*>
        for(role in ignoreRoles) {
            if(role !is String) return Pair(false, "La lista de roles ignorados no está bien definida (ID en la posición ${ignoreRoles.indexOf(role)} no es válida)")
            if(!isId(role)) return Pair(false, "La lista de roles ignorados no está bien definida (ID en la posición ${ignoreRoles.indexOf(role)} no es válida)")
            if(role.isNotEmpty() && !isValidRole(role)) return Pair(false, "La lista de roles ignorados no está bien definida (ID en la posición ${ignoreRoles.indexOf(role)} no existe en el servidor)")
        }

        if(antiLinks["allowed_links"] !is List<*>) return Pair(false, "La lista de links permitidos no está bien definida")
        val allowedLinks = antiLinks["allowed_links"] as List<*>
        for(link in allowedLinks) {
            if(link !is String) return Pair(false, "La lista de links permitidos no está bien definida (link en la posición ${allowedLinks.indexOf(link)} no es válido)")
            if(link.startsWith("http://") || link.startsWith("https://")) return Pair(false, "La lista de links permitidos no está bien definida (link en la posición ${allowedLinks.indexOf(link)} no es válido)\nPrueba a quitar el protocolo (http:// o https://)")
        }

        if(antiLinks["ignore_channels"] !is List<*>) return Pair(false, "La lista de canales ignorados no está bien definida")
        val ignoreChannels = antiLinks["ignore_channels"] as List<*>
        for(channel in ignoreChannels) {
            if(channel !is String) return Pair(false, "La lista de canales ignorados no está bien definida (ID en la posición ${ignoreChannels.indexOf(channel)} no es válida)")
            if(!isId(channel)) return Pair(false, "La lista de canales ignorados no está bien definida (ID en la posición ${ignoreChannels.indexOf(channel)} no es válida)")
            if(channel.isNotEmpty() && !isValidChannel(channel)) return Pair(false, "La lista de canales ignorados no está bien definida (ID en la posición ${ignoreChannels.indexOf(channel)} no existe en el servidor)")
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
        if((twitch["announce_channel_id"] as String).isNotEmpty() && !isValidChannel(twitch["announce_channel_id"] as String)) return Pair(false, "El canal de anuncios de twitch no existe en el servidor")
        if(twitch["message"] !is String) return Pair(false, "El mensaje de anuncios de twitch no está bien definido")
        if(twitch["live_channel_id"] !is String) return Pair(false, "El canal de live de twitch no está bien definido")
        if(!isIdOrEmpty(twitch["live_channel_id"] as String)) return Pair(false, "El canal de live de twitch no es un ID válido")
        if((twitch["live_channel_id"] as String).isNotEmpty() && !isValidChannel(twitch["live_channel_id"] as String)) return Pair(false, "El canal de live de twitch no existe en el servidor")
        if(twitch["live_open_message"] !is String) return Pair(false, "El mensaje de live_open de twitch no está bien definido")
        if(twitch["live_close_message"] !is String) return Pair(false, "El mensaje de live_close de twitch no está bien definido")

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
            if(isValidCommand(command.key as String)) return Pair(false, "El comando personalizado \"${command.key}\" ya existe como comando del bot")
            val aliases = commandValue["aliases"] as List<*>
            for (alias in aliases) {
                if (alias !is String) return Pair(false, "El comando personalizado \"${command.key}\" no está bien definido")
                if(isValidCommand(alias)) return Pair(false, "El comando personalizado \"${command.key}\" ya existe como comando del bot")
            }
        }

        return Pair(true, "")
    }

    private fun messages(): Pair<Boolean, String> {
        val messages = config["messages"] ?: return Pair(false, "El modulo de mensajes no está definido")
        if(messages !is Map<*, *>) return Pair(false, "El modulo de mensajes no está bien definido")

        if(messages["sanction"] !is String) return Pair(false, "El mensaje de sanciones no está bien definido")
        if(messages["anti_links_new_link"] !is String) return Pair(false, "El mensaje de nuevo link no está bien definido")
        if(messages["anti_links_under_revision"] !is String) return Pair(false, "El mensaje de links bajo revisión no está bien definido")
        if(messages["anti_links_sanction"] !is String) return Pair(false, "El mensaje de sanción por links no está bien definido")

        return Pair(true, "")
    }

    private fun isId(input: String): Boolean {
        return input.matches(Regex("[0-9]{18}")) || input.matches(Regex("[0-9]{17}")) || input.matches(Regex("[0-9]{19}"))
    }
    private fun isIdOrEmpty(input: String): Boolean {
        return input.isEmpty() || isId(input)
    }

    private fun isValidRole(roleId: String): Boolean = try { guild.getRoleById(roleId) != null } catch (e: Exception) { false }
    private fun isValidChannel(channelId: String): Boolean = try { guild.getTextChannelById(channelId) != null } catch (e: Exception) { false }
    private fun isValidCommand(command: String): Boolean = commandManager!!.getCommands().find { it.name == command || it.aliases.contains(command) } != null
}