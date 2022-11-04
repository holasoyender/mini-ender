package commands.message.info

import interfaces.Command
import interfaces.CommandResponse
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.utils.TimeFormat
import utils.Emojis
import java.awt.Color
import java.time.Instant

class Info: Command {
    override fun execute(event: MessageReceivedEvent, args: List<String>): CommandResponse {


        /*
        * Port de https://github.com/holasoyender/Libra/blob/main/src/main/java/libra/Commands/Info/Info.java para kotlin
        */

        val user = try {
            event.message.mentions.users.firstOrNull() ?: args.getOrNull(1)?.let { event.jda.retrieveUserById(it).complete() } ?: event.author
        } catch (e: Exception) {
            return CommandResponse.error("No se ha podido encontrar al usuario con ID ${args[1]}")
        }

        val member = try {
            event.guild.retrieveMember(user).complete()
        } catch (e: Exception) {
            null
        }

        val avatar = (user.avatarUrl ?: user.defaultAvatarUrl) + "?size=512"
        val smallBadges = mutableListOf<String>()

        if (user.isBot) smallBadges.add(Emojis.BOT)
        if (member?.isOwner == true) smallBadges.add(Emojis.OWNER)

        val embed = EmbedBuilder()
            .setColor(Color.decode("#2f3136"))
            .addField("${user.asTag} ${smallBadges.joinToString(" ")}", "```yml\nID: ${user.id}```", false)
            .addField("Badges", badges(user), true)
            .addField("Avatar", "[Link]($avatar)", true)
            .addField("Rol más alto", member?.roles?.firstOrNull()?.asMention ?: "Ninguno", true)
            .setFooter("> " + event.author.asTag, event.author.avatarUrl ?: "")
            .setTimestamp(Instant.now())
            .setThumbnail(avatar)

        if (member == null) {
            embed.addField("Todos los roles", "`El usuario no está en el servidor`", false)
        } else {

            val roles = member.roles.joinToString("\n") { it.name }
            embed.addField("Todos los roles", "```$roles```", false)
                .addField(
                    "${Emojis.invite}  Unido al servidor",
                    TimeFormat.DEFAULT.format(member.timeJoined) + " (${TimeFormat.RELATIVE.format(member.timeJoined)})",
                    true
                )
        }

        embed.addField(
            "${Emojis.time}  Cuenta creada",
            TimeFormat.DEFAULT.format(user.timeCreated) + " (${TimeFormat.RELATIVE.format(user.timeCreated)})",
            true
        )

        if(user.retrieveProfile().complete().bannerUrl != null)
            embed.setImage(user.retrieveProfile().complete().bannerUrl + "?size=1024")

        event.message.replyEmbeds(embed.build()).queue()

        return CommandResponse.success()
    }

    private fun badges(user: User): String {

        val badges = mutableListOf<String>()

        if(user.flags.isEmpty()) return "Ninguna"

        for (badge in user.flags) {

            when (badge) {
                User.UserFlag.STAFF -> badges.add(Emojis.STAFF)
                User.UserFlag.PARTNER -> badges.add(Emojis.PARTNER)
                User.UserFlag.HYPESQUAD -> badges.add(Emojis.HYPESQUAD)
                User.UserFlag.BUG_HUNTER_LEVEL_1 -> badges.add(Emojis.BUG_HUNTER_LEVEL_1)
                User.UserFlag.HYPESQUAD_BRAVERY -> badges.add(Emojis.HYPESQUAD_BRAVERY)
                User.UserFlag.HYPESQUAD_BRILLIANCE -> badges.add(Emojis.HYPESQUAD_BRILLIANCE)
                User.UserFlag.HYPESQUAD_BALANCE -> badges.add(Emojis.HYPESQUAD_BALANCE)
                User.UserFlag.EARLY_SUPPORTER -> badges.add(Emojis.EARLY_SUPPORTER)
                User.UserFlag.BUG_HUNTER_LEVEL_2 -> badges.add(Emojis.BUG_HUNTER_LEVEL_2)
                User.UserFlag.VERIFIED_DEVELOPER -> badges.add(Emojis.VERIFIED_DEVELOPER)
                User.UserFlag.CERTIFIED_MODERATOR -> badges.add(Emojis.CERTIFIED_MODERATOR)
                else -> {}
            }
        }

        return badges.joinToString(" ")
    }

    override val name: String
        get() = "info"
    override val description: String
        get() = "Muestra información sobre un usuario"
    override val aliases: List<String>
        get() = listOf("userinfo", "user")
    override val usage: String
        get() = "[usuario]"
    override val category: String
        get() = "Info"
    override val enabled: Boolean
        get() = true
    override val ownerOnly: Boolean
        get() = false
    override val guildOnly: Boolean
        get() = false
    override val global: Boolean
        get() = false
    override val permissions: List<Permission>
        get() = listOf()
    override val botPermissions: List<Permission>
        get() = listOf()
}