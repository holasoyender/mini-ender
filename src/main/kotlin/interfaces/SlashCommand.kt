package interfaces

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

interface SlashCommand {

    fun execute(event: SlashCommandInteractionEvent): CommandResponse

    val name: String

    val description: String
    val category: String
    val enabled: Boolean
    val ownerOnly: Boolean
    val guildOnly: Boolean
    val permissions: List<Permission>
    val botPermissions: List<Permission>
    val metadata: SlashCommandData?
}