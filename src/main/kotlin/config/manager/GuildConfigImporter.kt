package config.manager

import interfaces.CommandResponse
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message.Attachment
import org.json.JSONObject
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.error.YAMLException
import plugins.twitch.TwitchManager

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
            val (isValid, error) = GuildConfigValidator(config, guild).validate()
            if(!isValid) return CommandResponse.error("El archivo no es válido: `\n```$error\nPuedes consultar la documentación en https://miniender.kenabot.xyz``")

            /*
            * A partir de este punto sabemos que la configuración es válida
            * y que no va a causar ningún error al importarla, excepto los mapeos de arrays
            */
            val guildConfig = database.schema.Guild.get(guild.id)
            if(guildConfig == null) {

                database.schema.Guild(
                    id = guild.id,
                    prefix = config["prefix"] as String,

                    welcomeRoleId = (config["welcomes"] as Map<*, *>)["role_id"] as String,
                    welcomeChannelId = (config["welcomes"] as Map<*, *>)["channel_id"] as String,
                    welcomeMessage = (config["welcomes"] as Map<*, *>)["message"] as String,

                    muteRoleId = (config["roles"] as Map<*, *>)["mute_role_id"] as String,

                    moderationSilent = (config["moderation"] as Map<*, *>)["silent"] as Boolean,
                    moderationChannelId = (config["moderation"] as Map<*, *>)["moderation_channel_id"] as String,
                    permissions = (config["permissions"] as Map<Long, Int>).map { it.key.toString() to it.value }.toMap(),

                    logsChannelId = (config["logs"] as Map<*, *>)["channel_id"] as String,
                    moderationLogsChannelId = (config["logs"] as Map<*, *>)["moderation_channel_id"] as String,

                    antiLinksEnabled = (config["anti_links"] as Map<*, *>)["enabled"] as Boolean,
                    antiLinksAllowedLinks = try {
                        ((config["anti_links"] as Map<*, *>)["allowed_links"] as ArrayList<String>).toTypedArray()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        arrayOf()
                    },
                    antiLinksChannelId = (config["anti_links"] as Map<*, *>)["channel_id"] as String,
                    antiLinksIgnoredRoles = try {
                        ((config["anti_links"] as Map<*, *>)["ignore_roles"] as ArrayList<String>).toTypedArray()
                    } catch (e: Exception) {
                        arrayOf()
                    },
                    antiLinksIgnoredChannels = try {
                        ((config["anti_links"] as Map<*, *>)["ignore_channels"] as ArrayList<String>).toTypedArray()
                    } catch (e: Exception) {
                        arrayOf()
                    },
                    antiPhishingEnabled = (config["anti_links"] as Map<*, *>)["anti_phishing"] as Boolean,

                    twitchChannel = (config["twitch"] as Map<*, *>)["channel"] as String,
                    twitchAnnounceChannelId = (config["twitch"] as Map<*, *>)["announce_channel_id"] as String,
                    twitchAnnounceMessage = (config["twitch"] as Map<*, *>)["message"] as String,
                    twitchLiveChannelId = (config["twitch"] as Map<*, *>)["live_channel_id"] as String,
                    twitchOpenLiveMessage = (config["twitch"] as Map<*, *>)["live_open_message"] as String,
                    twitchCloseLiveMessage = (config["twitch"] as Map<*, *>)["live_close_message"] as String,

                    youtubeChannel = (config["youtube"] as Map<*, *>)["channel"] as String,
                    youtubeAnnounceChannelId = (config["youtube"] as Map<*, *>)["announce_channel_id"] as String,
                    youtubeAnnounceMessage = (config["youtube"] as Map<*, *>)["message"] as String,

                    sanctionMessage = (config["messages"] as Map<*, *>)["sanction"] as String,
                    antiLinksNewLinkMessage = (config["messages"] as Map<*, *>)["anti_links_new_link"] as String,
                    antiLinksUnderRevisionMessage = (config["messages"] as Map<*, *>)["anti_links_under_revision"] as String,
                    antiLinksSanctionMessage = (config["messages"] as Map<*, *>)["anti_links_sanction"] as String,

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

                    suggestChannel = (config["suggestions"] as Map<*, *>)["channel_id"] as String,
                    suggestCreateThread = (config["suggestions"] as Map<*, *>)["create_thread"] as Boolean,

                    raw = content
                ).save()

            } else {

                guildConfig.id = guild.id
                guildConfig.prefix = config["prefix"] as String

                guildConfig.welcomeRoleId = (config["welcomes"] as Map<*, *>)["role_id"] as String
                guildConfig.welcomeChannelId = (config["welcomes"] as Map<*, *>)["channel_id"] as String
                guildConfig.welcomeMessage = (config["welcomes"] as Map<*, *>)["message"] as String

                guildConfig.muteRoleId = (config["roles"] as Map<*, *>)["mute_role_id"] as String

                guildConfig.moderationSilent = (config["moderation"] as Map<*, *>)["silent"] as Boolean
                guildConfig.moderationChannelId = (config["moderation"] as Map<*, *>)["moderation_channel_id"] as String
                guildConfig.permissions = (config["permissions"] as Map<Long, Int>).map { it.key.toString() to it.value }.toMap()

                guildConfig.logsChannelId = (config["logs"] as Map<*, *>)["channel_id"] as String
                guildConfig.moderationLogsChannelId = (config["logs"] as Map<*, *>)["moderation_channel_id"] as String

                guildConfig.antiLinksEnabled = (config["anti_links"] as Map<*, *>)["enabled"] as Boolean
                guildConfig.antiLinksAllowedLinks = try {
                    ((config["anti_links"] as Map<*, *>)["allowed_links"] as ArrayList<String>).toTypedArray()
                } catch (e: Exception) {
                    e.printStackTrace()
                    arrayOf()
                }
                guildConfig.antiLinksChannelId = (config["anti_links"] as Map<*, *>)["channel_id"] as String
                guildConfig.antiLinksIgnoredRoles = try {
                    ((config["anti_links"] as Map<*, *>)["ignore_roles"] as ArrayList<String>).toTypedArray()
                } catch (e: Exception) {
                    arrayOf()
                }
                guildConfig.antiLinksIgnoredChannels = try {
                    ((config["anti_links"] as Map<*, *>)["ignore_channels"] as ArrayList<String>).toTypedArray()
                } catch (e: Exception) {
                    arrayOf()
                }
                guildConfig.antiPhishingEnabled = (config["anti_links"] as Map<*, *>)["anti_phishing"] as Boolean

                guildConfig.twitchChannel = (config["twitch"] as Map<*, *>)["channel"] as String
                guildConfig.twitchAnnounceChannelId = (config["twitch"] as Map<*, *>)["announce_channel_id"] as String
                guildConfig.twitchAnnounceMessage = (config["twitch"] as Map<*, *>)["message"] as String
                guildConfig.twitchLiveChannelId = (config["twitch"] as Map<*, *>)["live_channel_id"] as String
                guildConfig.twitchOpenLiveMessage = (config["twitch"] as Map<*, *>)["live_open_message"] as String
                guildConfig.twitchCloseLiveMessage = (config["twitch"] as Map<*, *>)["live_close_message"] as String

                guildConfig.youtubeChannel = (config["youtube"] as Map<*, *>)["channel"] as String
                guildConfig.youtubeAnnounceChannelId = (config["youtube"] as Map<*, *>)["announce_channel_id"] as String
                guildConfig.youtubeAnnounceMessage = (config["youtube"] as Map<*, *>)["message"] as String

                guildConfig.sanctionMessage = (config["messages"] as Map<*, *>)["sanction"] as String
                guildConfig.antiLinksNewLinkMessage = (config["messages"] as Map<*, *>)["anti_links_new_link"] as String
                guildConfig.antiLinksUnderRevisionMessage = (config["messages"] as Map<*, *>)["anti_links_under_revision"] as String
                guildConfig.antiLinksSanctionMessage = (config["messages"] as Map<*, *>)["anti_links_sanction"] as String

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

                guildConfig.suggestChannel = (config["suggestions"] as Map<*, *>)["channel_id"] as String
                guildConfig.suggestCreateThread = (config["suggestions"] as Map<*, *>)["create_thread"] as Boolean

                guildConfig.raw = content

                guildConfig.save()
            }

            if (guildConfig?.twitchChannel?.isNotEmpty() == true)
                TwitchManager.doChecks()
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