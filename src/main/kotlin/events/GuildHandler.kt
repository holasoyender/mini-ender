package events

import config.DefaultConfig
import database.schema.Guild
import logger.EventLogger
import net.dv8tion.jda.api.events.guild.GuildBanEvent
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.LoggerFactory

class GuildHandler: ListenerAdapter() {

    private val logger = LoggerFactory.getLogger(GuildHandler::class.java)

    override fun onGuildJoin(event: GuildJoinEvent) {
        logger.info("Se me ha añadido al servidor ${event.guild.name} (${event.guild.id})")
        super.onGuildJoin(event)
    }

    override fun onGuildLeave(event: GuildLeaveEvent) {
        logger.info("Se me ha expulsado del servidor ${event.guild.name} (${event.guild.id})")
        super.onGuildLeave(event)
    }

    override fun onGuildBan(event: GuildBanEvent) {
        EventLogger(event.guild, Guild.get(event.guild.id) ?: DefaultConfig.get()).log(event)
        super.onGuildBan(event)
    }

    override fun onGuildUnban(event: GuildUnbanEvent) {
        EventLogger(event.guild, Guild.get(event.guild.id) ?: DefaultConfig.get()).log(event)
        super.onGuildUnban(event)
    }

    override fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
        EventLogger(event.guild, Guild.get(event.guild.id) ?: DefaultConfig.get()).log(event)
        super.onGuildMemberJoin(event)
    }

    override fun onGuildMemberRemove(event: GuildMemberRemoveEvent) {
        EventLogger(event.guild, Guild.get(event.guild.id) ?: DefaultConfig.get()).log(event)
        super.onGuildMemberRemove(event)
    }
}