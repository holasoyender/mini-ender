package plugins.suggest

import database.schema.Sugerencia
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.internal.entities.emoji.CustomEmojiImpl
import utils.Emojis
import utils.Emojis.f
import java.awt.Color
import kotlin.math.round

object SuggestionButton {

    fun accept(event: ButtonInteractionEvent) {

        if(!database.Redis.usingRedis)
            return failure(event, "No se ha podido conectar con la base de datos")

        val suggestion = Sugerencia.get(event.messageId) ?: return failure(event, "Esta sugerencia ya ha acabado!")

        if(suggestion.acceptVotes.contains(event.user.id))
            return event.reply("${f(Emojis.error)}  Ya has votado en esta sugerencia").setEphemeral(true).queue()


        val acceptVotes = suggestion.acceptVotes.toMutableList()
        acceptVotes.add(event.user.id)
        suggestion.acceptVotes = acceptVotes

        if(suggestion.denyVotes.contains(event.user.id)) {
            val denyVotes = suggestion.denyVotes.toMutableList()
            denyVotes.remove(event.user.id)
            suggestion.denyVotes = denyVotes
        }

        suggestion.save()

        val all = suggestion.acceptVotes.size + suggestion.denyVotes.size
        val y = round((suggestion.acceptVotes.size.toDouble() / all.toDouble()) * 10).toInt()
        val n = round((suggestion.denyVotes.size.toDouble() / all.toDouble()) * 10).toInt()

        val string = when {
            y == 10 -> "${Emojis.GREEN_L}${Emojis.GREEN_M.repeat(10)}${Emojis.GREEN_R}"
            n == 10 -> "${Emojis.RED_L}${Emojis.RED_M.repeat(10)}${Emojis.RED_R}"
            else -> "${Emojis.GREEN_L}${Emojis.GREEN_M.repeat(y)}${Emojis.RED_M.repeat(n)}${Emojis.RED_R}"
        }

        val description = event.message.embeds[0].description
        val newDescription = description?.replace(Regex("\\[.*]"), "[` $string `]")

        val embed = EmbedBuilder(event.message.embeds[0])
            .setDescription(newDescription)
            .build()

        event.message.editMessageEmbeds(embed).setActionRow(
            Button.success(
                "suggestion::accept:$y-$n",
                Emoji.fromCustom(CustomEmojiImpl("tick", 952250501779570738, false))
            ),
            Button.danger(
                "suggestion::deny:$y-$n",
                Emoji.fromCustom(CustomEmojiImpl("x_", 1083494146116960378, false))
            ),
            Button.secondary(
                "suggestion::end:$y-$n",
                "Acabar"
            )
        ).queue()

        event.reply("${Emojis.success}  Has votado correctamente en esta sugerencia").setEphemeral(true).queue()
    }

    fun deny(event: ButtonInteractionEvent) {

        if(!database.Redis.usingRedis)
            return failure(event, "No se ha podido conectar con la base de datos")

        val suggestion = Sugerencia.get(event.messageId) ?: return failure(event, "Esta sugerencia ya ha acabado!")

        if(suggestion.denyVotes.contains(event.user.id))
            return event.reply("${f(Emojis.error)}  Ya has votado en esta sugerencia").setEphemeral(true).queue()

        val denyVotes = suggestion.denyVotes.toMutableList()
        denyVotes.add(event.user.id)
        suggestion.denyVotes = denyVotes

        if(suggestion.acceptVotes.contains(event.user.id)) {
            val acceptVotes = suggestion.acceptVotes.toMutableList()
            acceptVotes.remove(event.user.id)
            suggestion.acceptVotes = acceptVotes
        }

        suggestion.save()

        val all = suggestion.acceptVotes.size + suggestion.denyVotes.size
        val y = round((suggestion.acceptVotes.size.toDouble() / all.toDouble()) * 10).toInt()
        val n = round((suggestion.denyVotes.size.toDouble() / all.toDouble()) * 10).toInt()

        val string = when {
            y == 10 -> "${Emojis.GREEN_L}${Emojis.GREEN_M.repeat(10)}${Emojis.GREEN_R}"
            n == 10 -> "${Emojis.RED_L}${Emojis.RED_M.repeat(10)}${Emojis.RED_R}"
            else -> "${Emojis.GREEN_L}${Emojis.GREEN_M.repeat(y)}${Emojis.RED_M.repeat(n)}${Emojis.RED_R}"
        }

        val description = event.message.embeds[0].description
        val newDescription = description?.replace(Regex("\\[.*]"), "[` $string `]")

        val embed = EmbedBuilder(event.message.embeds[0])
            .setDescription(newDescription)
            .build()

        event.message.editMessageEmbeds(embed).setActionRow(
            Button.success(
                "suggestion::accept:$y-$n",
                Emoji.fromCustom(CustomEmojiImpl("tick", 952250501779570738, false))
            ),
            Button.danger(
                "suggestion::deny:$y-$n",
                Emoji.fromCustom(CustomEmojiImpl("x_", 1083494146116960378, false))
            ),
            Button.secondary(
                "suggestion::end:$y-$n",
                "Acabar"
            )
        ).queue()

        event.reply("${Emojis.success}  Has votado correctamente en esta sugerencia").setEphemeral(true).queue()
    }

    fun end(event: ButtonInteractionEvent) {
        try {
            val votes = event.componentId.split("::")[1].split(":")[1].split("-")
            val y = votes[0].toInt()
            val n = votes[1].toInt()

            val embed = EmbedBuilder(event.message.embeds[0])

            when {
                y > n-> embed.setColor(Color.decode("#57F287"))
                    .setTitle("${Emojis.success}  Sugerencia aceptada")
                n > y -> embed.setColor(Color.decode("#ED4245"))
                    .setTitle("${f(Emojis.error)}  Sugerencia rechazada")
                else -> embed.setColor(Color.decode("#5865F2"))
                    .setTitle("${Emojis.warning}  Sugerencia empatada!")
            }

            event.message.editMessageEmbeds(embed.build()).setActionRow(
                Button.success("suggestion::accept", Emoji.fromCustom(CustomEmojiImpl("tick", 952250501779570738, false))).asDisabled(),
                Button.danger("suggestion::deny", Emoji.fromCustom(CustomEmojiImpl("x_", 1083494146116960378, false))).asDisabled()
            ).queue({}, {})
        } catch (_: Exception) {}
    }

    private fun failure(event: ButtonInteractionEvent, failureMessage: String) {

        event.message.editMessageComponents(
            ActionRow.of(
                Button.success("suggestion::accept", Emoji.fromCustom(CustomEmojiImpl("tick", 952250501779570738, false))).asDisabled(),
                Button.danger("suggestion::deny", Emoji.fromCustom(CustomEmojiImpl("x_", 1083494146116960378, false))).asDisabled()
            )
        ).queue()

        end(event)

        event.reply("${f(Emojis.error)}  Ha ocurrido un error al intentar votar en esta sugerencia: `$failureMessage`").setEphemeral(true).queue()
    }
}