package handlers

import database.schema.Infraction
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.internal.entities.emoji.CustomEmojiImpl
import java.time.Instant

object InfractionButtons {

    fun prevPage(event: ButtonInteractionEvent) {
        val page = event.componentId.split("::")[1].split(":")[3].toInt()
        val userId = event.componentId.split("::")[1].split(":")[4]

        val infractions = Infraction.getAllByUserId(event.guild!!.id, userId)
        if (infractions.isEmpty())
            return event.reply("El usuario no tiene ninguna infracción").setEphemeral(true).queue()

        val chunks = infractions.chunked(10)

        val shouldGoTo = page - 1
        if (shouldGoTo < 0) return event.reply("No puedes ir a una página anterior").setEphemeral(true).queue()

        val message = chunks.getOrNull(shouldGoTo)?.map {
            val date =
                Instant.ofEpochMilli(it.date).toString().split("T")[0] + " " + Instant.ofEpochMilli(it.date).toString()
                    .split("T")[1].split(".")[0]
            "  ${it.type}  | ${it.id} | ${it.userName} (${it.userId}) | ${it.moderatorId} | $date | ${if (it.duration.toInt() == 0) "N/A" else it.duration} | ${if (it.ended) "No" else "Si"} | ${it.reason}"
        }

        if (message.isNullOrEmpty())
            return event.reply("No puedes ir a una página anterior").setEphemeral(true).queue()

        val msg = event.editMessage(
            "```  Tipo  |  ID  |  Usuario  |  Moderador  |  Fecha  |  Duración  |  Activa  |  Motivo\n---------------------------------------------------------------------------------\n${
                message.joinToString("\n")
            }```"
        )

        msg.setActionRow(
            Button.primary(
                "cmd::infrs:${event.user.id}:prev:${shouldGoTo}:${userId}",
                Emoji.fromCustom(CustomEmojiImpl("left", 940316141983764494, false))
            ).withDisabled(shouldGoTo == 0),
            Button.primary(
                "cmd::infrs:${event.user.id}:next:${shouldGoTo}:${userId}",
                Emoji.fromCustom(CustomEmojiImpl("rigth", 940316141782458418, false))
            ).withDisabled(shouldGoTo == chunks.size - 1),
            Button.primary(
                "cmd::infrs:${event.user.id}:reload:${shouldGoTo}:${userId}",
                Emoji.fromCustom(CustomEmojiImpl("loop", 952242523521294456, false))
            ),
            Button.secondary(
                "cmd::infrs:page",
                "Página ${shouldGoTo+1}/${chunks.size}"
            ).asDisabled()
        ).queue()
    }

    fun nextPage(event: ButtonInteractionEvent) {
        val page = event.componentId.split("::")[1].split(":")[3].toInt()
        val userId = event.componentId.split("::")[1].split(":")[4]

        val infractions = Infraction.getAllByUserId(event.guild!!.id, userId)
        if (infractions.isEmpty())
            return event.reply("El usuario no tiene ninguna infracción").setEphemeral(true).queue()

        val chunks = infractions.chunked(10)

        val shouldGoTo = page + 1
        if (shouldGoTo > chunks.size - 1)
            return event.reply("No puedes ir a una página siguiente").setEphemeral(true).queue()

        val message = chunks.getOrNull(shouldGoTo)?.map {
            val date =
                Instant.ofEpochMilli(it.date).toString().split("T")[0] + " " + Instant.ofEpochMilli(it.date).toString()
                    .split("T")[1].split(".")[0]
            "  ${it.type}  | ${it.id} | ${it.userName} (${it.userId}) | ${it.moderatorId} | $date | ${if (it.duration.toInt() == 0) "N/A" else it.duration} | ${if (it.ended) "No" else "Si"} | ${it.reason}"
        }

        if (message.isNullOrEmpty())
            return event.reply("No puedes ir a una página siguiente").setEphemeral(true).queue()

        val msg = event.editMessage(
            "```  Tipo  |  ID  |  Usuario  |  Moderador  |  Fecha  |  Duración  |  Activa  |  Motivo\n---------------------------------------------------------------------------------\n${
                message.joinToString("\n")
            }```"
        )

        msg.setActionRow(
            Button.primary(
                "cmd::infrs:${event.user.id}:prev:${shouldGoTo}:${userId}",
                Emoji.fromCustom(CustomEmojiImpl("left", 940316141983764494, false))
            ).withDisabled(shouldGoTo == 0),
            Button.primary(
                "cmd::infrs:${event.user.id}:next:${shouldGoTo}:${userId}",
                Emoji.fromCustom(CustomEmojiImpl("rigth", 940316141782458418, false))
            ).withDisabled(shouldGoTo == chunks.size - 1),
            Button.primary(
                "cmd::infrs:${event.user.id}:reload:${shouldGoTo}:${userId}",
                Emoji.fromCustom(CustomEmojiImpl("loop", 952242523521294456, false))
            ),
            Button.secondary(
                "cmd::infrs:page",
                "Página ${shouldGoTo+1}/${chunks.size}"
            ).asDisabled()
        ).queue()
    }

    fun reload(event: ButtonInteractionEvent) {
        val userId = event.componentId.split("::")[1].split(":")[4]

        val infractions = Infraction.getAllByUserId(event.guild!!.id, userId)
        if (infractions.isEmpty())
            return event.reply("El usuario no tiene ninguna infracción").setEphemeral(true).queue()

        val chunks = infractions.chunked(10)

        val message = chunks.firstOrNull()?.map {
            val date =
                Instant.ofEpochMilli(it.date).toString().split("T")[0] + " " + Instant.ofEpochMilli(it.date).toString()
                    .split("T")[1].split(".")[0]
            "  ${it.type}  | ${it.id} | ${it.userName} (${it.userId}) | ${it.moderatorId} | $date | ${if (it.duration.toInt() == 0) "N/A" else it.duration} | ${if (it.ended) "No" else "Si"} | ${it.reason}"
        }

        if (message.isNullOrEmpty())
            return event.reply("No puedes ir a una página siguiente").setEphemeral(true).queue()

        val msg = event.editMessage(
            "```  Tipo  |  ID  |  Usuario  |  Moderador  |  Fecha  |  Duración  |  Activa  |  Motivo\n---------------------------------------------------------------------------------\n${
                message.joinToString("\n")
            }```"
        )

        msg.setActionRow(
            Button.primary(
                "cmd::infrs:${event.user.id}:prev:0:${userId}",
                Emoji.fromCustom(CustomEmojiImpl("left", 940316141983764494, false))
            ).withDisabled(true),
            Button.primary(
                "cmd::infrs:${event.user.id}:next:0:${userId}",
                Emoji.fromCustom(CustomEmojiImpl("rigth", 940316141782458418, false))
            ).withDisabled(chunks.size == 1),
            Button.primary(
                "cmd::infrs:${event.user.id}:reload:0:${userId}",
                Emoji.fromCustom(CustomEmojiImpl("loop", 952242523521294456, false))
            ),
            Button.secondary(
                "cmd::infrs:page",
                "Página 1/${chunks.size}"
            ).asDisabled()
        ).setEmbeds()
            .queue()
    }


}