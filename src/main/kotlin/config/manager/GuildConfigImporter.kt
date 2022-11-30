package config.manager

import interfaces.CommandResponse
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message.Attachment
import org.json.JSONObject
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.error.YAMLException

object GuildConfigImporter {

    @Suppress("UNCHECKED_CAST")
    fun import(file: Attachment, guild: Guild): CommandResponse {

        if (file.fileExtension != "yaml" && file.fileExtension != "yml") return CommandResponse.error("El archivo adjuntado no es un archivo YAML")
        if (file.fileName != "${guild.id}.yaml" && file.fileName != "${guild.id}.yml") return CommandResponse.error("El archivo adjuntado no es el archivo de configuración de este servidor")
        if (file.size > 1000000) return CommandResponse.error("El archivo es demasiado grande, el tamaño máximo es de 1MB")

        try {
            val content = file.proxy.download().get().readAllBytes().toString(Charsets.UTF_8)

            if(content.length < 100) return CommandResponse.error("El archivo está vacío o no es válido")

            val config = Yaml().load(content) as Map<String, Any>
            val (isValid, error) = GuildConfigValidator(config).validate()
            if(!isValid) return CommandResponse.error("El archivo no es válido: `\n```$error\nPuedes consultar la documentación en https://miniender.kenabot.xyz``")

            val (isOk, validationError) = GuildConfigChecker(config).verify()
            if(!isOk) return CommandResponse.error("El archivo no es válido: `\n```$validationError\nPuedes consultar la documentación en https://miniender.kenabot.xyz``")

            val guildConfig = database.schema.Guild.get(guild.id)
            if(guildConfig == null) {

                database.schema.Guild(
                    id = guild.id,
                    prefix = config["prefix"] as String,

                    welcomeRoleId = (config["welcomes"] as Map<*, *>)["role_id"] as String,
                    welcomeChannelId = (config["welcomes"] as Map<*, *>)["channel_id"] as String,
                    welcomeMessage = (config["welcomes"] as Map<*, *>)["message"] as String,

                    muteRoleId = (config["roles"] as Map<*, *>)["mute_role_id"] as String,

                    logsChannelId = (config["logs"] as Map<*, *>)["channel_id"] as String,

                    antiLinksEnabled = (config["anti_links"] as Map<*, *>)["enabled"] as Boolean,
                    antiLinksChannelId = (config["anti_links"] as Map<*, *>)["channel_id"] as String,
                    antiLinksIgnoredRoles = try {
                        ((config["anti_links"] as Map<*, *>)["ignored_roles"] as Array<String>)
                    } catch (e: Exception) {
                        arrayOf()
                    },
                    antiLinksIgnoredChannels = try {
                        ((config["anti_links"] as Map<*, *>)["ignored_channels"] as Array<String>)
                    } catch (e: Exception) {
                        arrayOf()
                    },
                    antiPhishingEnabled = (config["anti_links"] as Map<*, *>)["anti_phishing"] as Boolean,

                    twitchChannel = (config["twitch"] as Map<*, *>)["channel"] as String,
                    twitchAnnounceChannelId = (config["twitch"] as Map<*, *>)["announce_channel_id"] as String,
                    twitchAnnounceMessage = (config["twitch"] as Map<*, *>)["message"] as String,
                    twitchLiveChannelId = (config["twitch"] as Map<*, *>)["live_channel_id"] as String,
                    twitchLiveMessage = (config["twitch"] as Map<*, *>)["live_message"] as String,

                    customCommands = try {
                        (config["custom_commands"] as Map<*, *>).map {
                            val values = it.value as Map<*, *>
                            JSONObject()
                                .put("name", it.key)
                                .put("response", values["response"])
                                .put("description", values["description"])
                                .put("aliases", values["aliases"] as List<String>)
                        }.toTypedArray()
                    } catch (e: Exception) {
                        arrayOf()
                    },

                    raw = content
                ).save()

            } else {

                guildConfig.id = guild.id
                guildConfig.prefix = config["prefix"] as String

                guildConfig.welcomeRoleId = (config["welcomes"] as Map<*, *>)["role_id"] as String
                guildConfig.welcomeChannelId = (config["welcomes"] as Map<*, *>)["channel_id"] as String
                guildConfig.welcomeMessage = (config["welcomes"] as Map<*, *>)["message"] as String

                guildConfig.muteRoleId = (config["roles"] as Map<*, *>)["mute_role_id"] as String

                guildConfig.logsChannelId = (config["logs"] as Map<*, *>)["channel_id"] as String

                guildConfig.antiLinksEnabled = (config["anti_links"] as Map<*, *>)["enabled"] as Boolean
                guildConfig.antiLinksChannelId = (config["anti_links"] as Map<*, *>)["channel_id"] as String
                guildConfig.antiLinksIgnoredRoles = try {
                    ((config["anti_links"] as Map<*, *>)["ignored_roles"] as Array<String>)
                } catch (e: Exception) {
                    arrayOf()
                }
                guildConfig.antiLinksIgnoredChannels = try {
                    ((config["anti_links"] as Map<*, *>)["ignored_channels"] as Array<String>)
                } catch (e: Exception) {
                    arrayOf()
                }
                guildConfig.antiPhishingEnabled = (config["anti_links"] as Map<*, *>)["anti_phishing"] as Boolean

                guildConfig.twitchChannel = (config["twitch"] as Map<*, *>)["channel"] as String
                guildConfig.twitchAnnounceChannelId = (config["twitch"] as Map<*, *>)["announce_channel_id"] as String
                guildConfig.twitchAnnounceMessage = (config["twitch"] as Map<*, *>)["message"] as String
                guildConfig.twitchLiveChannelId = (config["twitch"] as Map<*, *>)["live_channel_id"] as String
                guildConfig.twitchLiveMessage = (config["twitch"] as Map<*, *>)["live_message"] as String

                guildConfig.customCommands = try {
                    (config["custom_commands"] as Map<*, *>).map {
                        val values = it.value as Map<*, *>
                        JSONObject()
                            .put("name", it.key)
                            .put("response", values["response"])
                            .put("description", values["description"])
                            .put("aliases", values["aliases"] as List<String>)
                    }.toTypedArray()
                } catch (e: Exception) {
                    arrayOf()
                }

                guildConfig.raw = content

                guildConfig.save()
            }
            return CommandResponse.success()

        } catch (e: Exception) {
            return if (e is YAMLException)
                CommandResponse.error("El archivo de configuración no es valido: `\n```${e.message ?: "Error desconocido"}\nPuedes consultar la documentación en https://miniender.kenabot.xyz``")
            else {
                e.printStackTrace()
                CommandResponse.error("Ha ocurrido un error inesperado: `\n```${e.message ?: "Error desconocido"}``")
            }
        }
    }
}