package commands.message.ocio

import database.schema.Regalo
import interfaces.Command
import interfaces.CommandResponse
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message.MentionType
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import plugins.regalos.GiftManager
import utils.Constants.OWNER_IDS
import java.awt.Color

class Regalo: Command {
    override fun execute(event: MessageReceivedEvent, args: List<String>): CommandResponse {
        if(!listOf("1050890422869827644", "1051176776098906112").contains(event.channel.id)) return CommandResponse.error("Este comando no está disponible en este canal.")

        val user = try {
            event.message.mentions.users.firstOrNull() ?: args.getOrNull(1)
                ?.let { event.jda.retrieveUserById(it).complete() }
        } catch (e: Exception) {
            null
        }

        if(user != null && (event.member?.hasPermission(Permission.MESSAGE_MANAGE) == true || OWNER_IDS.contains(event.author.id))) {

            val info = Regalo.get(user.id) ?: return CommandResponse.error("No se ha encontrado información sobre el usuario con ID ${user.id}")

            val gifts = info.gifts.map { it["name"] as String }
            val grouped = gifts.groupingBy { it }.eachCount()

            val message = """```nim
Historial de regalos de ${user.asTag}:

> ${grouped.map { "${it.key} -  ${it.value} veces" }.joinToString("\n> ")}

Total: ${gifts.size} regalos```
            """.trimIndent()

            event.message.reply(message).queue()
            return CommandResponse.success()
        }


        val response = GiftManager.run(event.author, event.member, event.channel.id == "1050890422869827644")

        if(response.embed == null) {
            event.message.reply(response.message).queue()
        } else {
            val embed = response.embed
            embed.setColor(Color.decode("#2f3136"))
                .setThumbnail("https://twemoji.maxcdn.com/v/latest/72x72/1f381.png")
            event.message.replyEmbeds(embed.build()).setAllowedMentions(
                listOf(MentionType.USER)
            ).queue()
        }
        return CommandResponse.success()
    }

    override val name: String
        get() = "regalo"
    override val description: String
        get() = "Mira a ver que te ha tocado en el regalo de hoy!"
    override val aliases: List<String>
        get() = listOf("regalos", "regalito", "regalitos", "gift", "gifts")
    override val usage: String
        get() = ""
    override val category: String
        get() = "Ocio"
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
    override val permissionLevel: Int
        get() = 1
    override val botPermissions: List<Permission>
        get() = listOf()
}