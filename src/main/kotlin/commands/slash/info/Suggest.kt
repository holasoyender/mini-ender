package commands.slash.info

import config.DefaultConfig
import database.schema.Guild
import interfaces.CommandResponse
import interfaces.SlashCommand
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.dv8tion.jda.api.interactions.modals.Modal
import utils.Emojis

class Suggest: SlashCommand {
    override fun execute(event: SlashCommandInteractionEvent): CommandResponse {

        val config = Guild.get(event.guild!!.id) ?: DefaultConfig.get(event.guild!!.id)

        if (config.suggestChannel.isBlank())
            return CommandResponse.error("${Emojis.f(Emojis.error)}  No hay un canal de sugerencias configurado en este servidor!")

        if(!database.Redis.usingRedis)
            return CommandResponse.error("${Emojis.f(Emojis.error)}  Este bot no admite sugerencias, si crees que esto es un error, contacta con un administrador del bot")

        event.replyModal(
            Modal.create("cmd::suggest:${event.user.id}", "Crear sugerencia")
                .addActionRow(
                    TextInput.create("body", "Sugerencia", TextInputStyle.PARAGRAPH)
                        .setPlaceholder("Escribe tu sugerencia aquí...")
                        .setRequired(true)
                        .setMinLength(10)
                        .setMaxLength(1000)
                        .build()
                ).build()
        ).queue()

        return CommandResponse.success()
    }

    override val name: String
        get() = "sugerir"
    override val description: String
        get() = "Hacer una sugerencia para el servidor."
    override val category: String
        get() = "Información"
    override val enabled: Boolean
        get() = true
    override val ownerOnly: Boolean
        get() = false
    override val guildOnly: Boolean
        get() = true
    override val permissions: List<Permission>
        get() = listOf()
    override val permissionLevel: Int
        get() = 1
    override val botPermissions: List<Permission>
        get() = listOf(Permission.CREATE_PUBLIC_THREADS, Permission.CREATE_PRIVATE_THREADS, Permission.MANAGE_THREADS)
    override val metadata: SlashCommandData?
        get() = null
}