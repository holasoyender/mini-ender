package handlers

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import java.awt.Color

object ErrorReporter {

    fun acknowledgeError(event: ButtonInteractionEvent) {
        val errorId = event.componentId.split("::")[1].split(":")[1]

        val error = database.schema.Error.get(errorId) ?:
        return event.reply("Parece que ese error ya no está en la base de datos!").setEphemeral(true).queue()

        val embed = EmbedBuilder()
            .setDescription("Se ha reportado un error con id `${error.id}`")
            .addField("Error", "```${error.error}```", false)
            .addField("Usuario", event.user.asTag + " (${event.user.id})", false)
            .addField("ID", "`${error.id}`", true)
            .addField("Estado", "<:idle:983837896287092796>  Identificado", true)
            .setColor(Color.decode("#2f3136"))
            .setThumbnail("https://cdn.discordapp.com/attachments/1026084700189638738/1026247509225508895/emoji..png")
            .setAuthor("Error identificado", null, event.jda.selfUser.avatarUrl)

        error.acknowledged = true
        error.save()

        event.editMessageEmbeds(embed.build()).setActionRow(
            Button.primary("error::acknowledge:${error.id}", "Marcar como identificado").asDisabled(),
            Button.success("error::solve:${error.id}", "Marcar como resuelto"),
            Button.danger("error::delete:${error.id}", "Eliminar")
        ).queue()
    }

    fun solveError(event: ButtonInteractionEvent) {

        val errorId = event.componentId.split("::")[1].split(":")[1]

        val error = database.schema.Error.get(errorId) ?:
        return event.reply("Parece que ese error ya no está en la base de datos!").setEphemeral(true).queue()

        val embed = EmbedBuilder()
            .setDescription("Se ha reportado un error con id `${error.id}`")
            .addField("Error", "```${error.error}```", false)
            .addField("Usuario", event.user.asTag + " (${event.user.id})", false)
            .addField("ID", "`${error.id}`", true)
            .addField("Estado", "<:green:940708160161845289>  Solucionado", true)
            .setColor(Color.decode("#2f3136"))
            .setThumbnail("https://cdn.discordapp.com/attachments/1026084700189638738/1026246953622847521/emoji..png")
            .setAuthor("Error solucionado", null, event.jda.selfUser.avatarUrl)

        error.acknowledged = true
        error.solved = true
        error.save()

        event.editMessageEmbeds(embed.build()).setActionRow(
            Button.primary("error::acknowledge:${error.id}", "Marcar como identificado").asDisabled(),
            Button.success("error::solve:${error.id}", "Marcar como resuelto").asDisabled(),
            Button.danger("error::delete:${error.id}", "Eliminar").asDisabled()
        ).queue()
    }

    fun deleteError(event: ButtonInteractionEvent) {

        val errorId = event.componentId.split("::")[1].split(":")[1]

        val error = database.schema.Error.get(errorId) ?:
            return event.reply("Parece que ese error ya no está en la base de datos!").setEphemeral(true).queue()

        val embed = EmbedBuilder()
            .setDescription("Se ha reportado un error con id `${error.id}`")
            .addField("Error", "```${error.error}```", false)
            .addField("Usuario", event.user.asTag + " (${event.user.id})", false)
            .addField("ID", "`${error.id}`", true)
            .addField("Estado", "<:dnd:983837895083323452>  Eliminado", true)
            .setColor(Color.decode("#2f3136"))
            .setThumbnail("https://cdn.discordapp.com/attachments/1026084700189638738/1026246375136034896/emoji.png")
            .setAuthor("Error eliminado", null, event.jda.selfUser.avatarUrl)

        error.delete()

        event.editMessageEmbeds(embed.build()).setActionRow(
            Button.primary("error::acknowledge:${error.id}", "Marcar como identificado").asDisabled(),
            Button.success("error::solve:${error.id}", "Marcar como resuelto").asDisabled(),
            Button.danger("error::delete:${error.id}", "Eliminar").asDisabled()
        ).queue()
    }

}