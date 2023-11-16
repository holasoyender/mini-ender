package events

import config.DefaultConfig
import database.schema.Guild
import database.schema.Infraction
import logger.EventLogger
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent
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
        val config = Guild.get(event.guild.id) ?: DefaultConfig.get()
        EventLogger(event.guild, config).log(event)

        if (config.muteRoleId.isNotEmpty()) {
            val muteRole = event.guild.getRoleById(config.muteRoleId)

            if (muteRole != null) {
                val infractions = Infraction.getAllByUserId(event.guild.id, event.user.id)
                if (infractions.isNotEmpty()) {

                    val activeInfractions = infractions.filter { !it.ended }
                    if (activeInfractions.isNotEmpty()) {

                        val latestInfraction = activeInfractions.maxByOrNull { it.date }
                        if (latestInfraction != null) {
                            if (latestInfraction.type.name == "MUTE" || latestInfraction.type.name == "TEMP_MUTE") {
                                event.guild.addRoleToMember(event.member, muteRole).queue({}, {})
                            }
                        }
                    }
                }
            }
        }

        super.onGuildMemberJoin(event)
    }

    override fun onGuildMemberRemove(event: GuildMemberRemoveEvent) {
        EventLogger(event.guild, Guild.get(event.guild.id) ?: DefaultConfig.get()).log(event)
        super.onGuildMemberRemove(event)
    }

    override fun onChannelCreate(event: ChannelCreateEvent) {
        val channel = event.channel

        if (channel is TextChannel && channel.parentCategory?.name?.lowercase() == "tickets") {
            val embed = EmbedBuilder()
                .setTitle("Ticket abierto: ${(channel as TextChannel).name}")
                .setColor(0xE64D3D)

            val logChannel = event.guild.getNewsChannelById("838900371262799883") ?: return
            logChannel.sendMessageEmbeds(embed.build()).queue()
        }
    }
}