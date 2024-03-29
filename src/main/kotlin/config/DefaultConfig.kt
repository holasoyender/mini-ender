package config

import database.schema.Guild

object DefaultConfig {

    fun get(): Guild {
        return Guild(
            id = "0",
            prefix = Env.PREFIX ?: "-",

            welcomeRoleId = "",
            welcomeChannelId = "",
            welcomeMessage = "",

            muteRoleId = "",

            moderationSilent = false,
            moderationChannelId= "",
            permissions = mapOf(),

            logsChannelId = "",
            moderationLogsChannelId = "",

            antiLinksEnabled = false,
            antiLinksAllowedLinks = arrayOf(),
            antiLinksChannelId = "",
            antiLinksIgnoredRoles = arrayOf(),
            antiLinksIgnoredChannels = arrayOf(),
            antiPhishingEnabled = false,

            customCommands = arrayOf(),

            twitchChannel = "",
            twitchAnnounceChannelId = "",
            twitchAnnounceMessage = "",
            twitchLiveChannelId = "",
            twitchOpenLiveMessage = "",
            twitchCloseLiveMessage = "",

            youtubeChannel = "",
            youtubeAnnounceChannelId = "",
            youtubeAnnounceMessage = "",

            sanctionMessage = "",
            antiLinksNewLinkMessage = "",
            antiLinksSanctionMessage = "",
            antiLinksUnderRevisionMessage = "",

            suggestChannel = "",
            suggestCreateThread = false,

            raw = ""
        )
    }

    fun get(guildID: String): Guild {
        val guild = get()
        guild.id = guildID
        return guild
    }
}