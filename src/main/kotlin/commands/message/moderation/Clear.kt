package commands.message.moderation

import interfaces.Command
import interfaces.CommandResponse
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import utils.Emojis
import utils.Emojis.f
import java.time.OffsetDateTime

class Clear: Command {
    override fun execute(event: MessageReceivedEvent, args: List<String>): CommandResponse {

        val amountOrUserId = args.getOrNull(1)?.toLongOrNull()
            ?: return CommandResponse.error("Debes de especificar una cantidad válida")

        val maxMessages = 500

        val maxAge = OffsetDateTime.now().minusWeeks(2).minusHours(1)

        if (amountOrUserId < 1000000000000000) {

            if (amountOrUserId < 1 || amountOrUserId > maxMessages)
                return CommandResponse.error("Debes de especificar una cantidad entre 1 y $maxMessages mensajes")

            event.message.reply("${Emojis.loading}  Recopilando mensajes...").queue { msg ->
                try {
                    event.channel.iterableHistory
                        .takeAsync(amountOrUserId.toInt()).thenAccept { messages ->
                            val filteredMessages =
                                messages.filter { it.timeCreated.isAfter(maxAge) && it.author.idLong != event.jda.selfUser.idLong }
                            if (filteredMessages.isEmpty()) {
                                msg.editMessage("${f(Emojis.error)}  No se han encontrado mensajes para borrar, asegurate de que no sean mas antiguos de 2 semanas")
                                    .mentionRepliedUser(false)
                                    .queue()
                                return@thenAccept
                            }

                            event.channel.purgeMessages(filteredMessages)
                            msg.editMessage("${Emojis.success}  Se han eliminado **${filteredMessages.size}** mensajes")
                                .mentionRepliedUser(false)
                                .queue()
                        }
                } catch (e: Exception) {
                    msg.editMessage("${f(Emojis.error)}  No se han podido eliminar los mensajes: `${e.message}`")
                        .mentionRepliedUser(false).queue()
                }
            }

        } else {

            val amount = args.getOrNull(2)?.toLongOrNull()
                ?: return CommandResponse.error("Debes de especificar una cantidad válida de mensajes del usuario")

            if (amount < 1 || amount > maxMessages)
                return CommandResponse.error("Debes de especificar una cantidad entre 1 y $maxMessages mensajes")

            event.message.reply("${Emojis.loading}  Recopilando mensajes...").queue { msg ->
                try {
                    event.channel.iterableHistory.takeAsync(amount.toInt()).thenAccept { messages ->
                        val filteredMessages =
                            messages.filter { it.timeCreated.isAfter(maxAge) && it.author.idLong == amountOrUserId }
                        if (filteredMessages.isEmpty()) {
                            msg.editMessage("${f(Emojis.error)}  No he encontrado ningún mensaje de ese usuario en los últimos $amount mensajes")
                                .mentionRepliedUser(false)
                                .queue()
                            return@thenAccept
                        }
                        event.channel.purgeMessages(filteredMessages)
                        msg.editMessage("${Emojis.success}  Se han eliminado **${filteredMessages.size}** mensajes del usuario con ID `${amountOrUserId}`")
                            .mentionRepliedUser(false)
                            .queue()
                    }
                } catch (e: Exception) {
                    msg.editMessage("${f(Emojis.error)}  No se han podido eliminar los mensajes del usuario: `${e.message}`")
                        .mentionRepliedUser(false).queue()
                }
            }
        }

        return CommandResponse.success()
    }

    override val name: String
        get() = "clear"
    override val description: String
        get() = "Limpiar todos los mensajes del canal o de un usuario"
    override val aliases: List<String>
        get() = listOf("limpiar", "cls")
    override val usage: String
        get() = "[usuario]"
    override val category: String
        get() = "Moderación"
    override val enabled: Boolean
        get() = true
    override val ownerOnly: Boolean
        get() = false
    override val guildOnly: Boolean
        get() = true
    override val global: Boolean
        get() = false
    override val permissions: List<Permission>
        get() = listOf(Permission.MESSAGE_MANAGE)
    override val botPermissions: List<Permission>
        get() = listOf(Permission.MESSAGE_MANAGE, Permission.MANAGE_CHANNEL)
}