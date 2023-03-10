package plugins.suggest

import database.schema.Guild
import database.schema.Sugerencia
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.internal.entities.emoji.CustomEmojiImpl
import utils.Emojis
import java.awt.Color
import java.time.Instant

object SuggestionManager {

    fun createSuggestion(suggestion: String, config: Guild, author: User, channel: GuildMessageChannel): Boolean {

        try {
            val embed = EmbedBuilder()
                .setAuthor(author.name, null, author.effectiveAvatarUrl)
                .setTitle("¡Nueva sugerencia! ${Emojis.BETA}")
                .setTimestamp(Instant.now())
                .setDescription(
                    "**Estado de la votación**:\n\n`[` ${Emojis.GREY_L}${Emojis.GREY_M.repeat(10)}${Emojis.GREY_R} `]`\n\n```${
                        if (suggestion.length > 1024) suggestion.substring(0, 1020) + "..." else suggestion
                    }```"
                )
                .setThumbnail("https://cdn.discordapp.com/attachments/839400943517827092/1083492194062700604/emoji.png")
                .setColor(Color.decode("#2f3136"))

            channel.sendMessageEmbeds(embed.build()).setActionRow(
                Button.success(
                    "suggestion::accept:0-0",
                    Emoji.fromCustom(CustomEmojiImpl("tick", 952250501779570738, false))
                ),
                Button.danger(
                    "suggestion::deny:0-0",
                    Emoji.fromCustom(CustomEmojiImpl("x_", 1083494146116960378, false))
                ),
                Button.secondary(
                    "suggestion::end:0-0",
                    "Acabar"
                )
            ).queue({
                val sugerencia = Sugerencia(
                    id = it.id,
                    acceptVotes = listOf(),
                    denyVotes = listOf(),
                )

                sugerencia.save()

                if(config.suggestCreateThread)
                    it.createThreadChannel("Sugerencia de ${author.name}").queue({}, {})
            }, {})

            return true
        } catch (e: Exception) {
            return false
        }
    }
}