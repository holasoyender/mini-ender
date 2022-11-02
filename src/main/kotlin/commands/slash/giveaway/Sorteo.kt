package commands.slash.giveaway

import interfaces.CommandResponse
import interfaces.SlashCommand
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.*
import plugins.giveaway.GiveawayManager
import utils.Emojis
import utils.Time

class Sorteo: SlashCommand {
    override fun execute(event: SlashCommandInteractionEvent): CommandResponse {

        when (event.subcommandName) {
            "crear" -> {
                val channel = event.getOption("canal")?.asChannel ?: return CommandResponse.error("Debes especificar un canal valido")
                val time = event.getOption("tiempo")?.asString ?: return CommandResponse.error("Debes especificar un tiempo valido (Ej: 1d, 12m, 30d)")
                val winners = event.getOption("ganadores")?.asLong ?: return CommandResponse.error("Debes especificar un numero de ganadores valido")
                val prize = event.getOption("premio")?.asString ?: return CommandResponse.error("Debes especificar un premio para el sorteo")
                val host = event.getOption("host")?.asUser ?: event.user

                val formattedTime = Time.ms(time)
                if(formattedTime < 1 || formattedTime > 604800000) return CommandResponse.error("El tiempo debe ser mayor a 1ms y menor a 1 semana")

                if(channel !is TextChannel) return CommandResponse.error("Debes especificar un canal de texto valido")
                if(winners < 1 || winners > 10) return CommandResponse.error("El numero de ganadores debe estar entre 1 y 10")

                event.reply("${Emojis.success} Sorteo creado en ${(channel as TextChannel).asMention}").queue {
                    GiveawayManager.createGiveaway(event.guild!!, channel, formattedTime, winners, prize, host, it)
                }

            }
            "acabar" -> {

            }
            "repetir" -> {

            }
            "info" -> {

            }
            else -> {
                return CommandResponse.error("No se ha especificado una acción valida")
            }
        }
        return CommandResponse.success()
    }

    override val name: String
        get() = "sorteo"
    override val description: String
        get() = "Crear, acabar o reiniciar un sorteo"
    override val category: String
        get() = "Sorteos"
    override val enabled: Boolean
        get() = true
    override val ownerOnly: Boolean
        get() = false
    override val guildOnly: Boolean
        get() = true
    override val permissions: List<Permission>
        get() = listOf(Permission.MESSAGE_MANAGE)
    override val botPermissions: List<Permission>
        get() = listOf(Permission.MESSAGE_MANAGE, Permission.MANAGE_SERVER)
    override val metadata: SlashCommandData
        get() = Commands.slash(this.name, this.description)
            .addSubcommands(
                SubcommandData("crear", "Crear un sorteo")
                    .addOptions(
                        OptionData(OptionType.CHANNEL, "canal", "Canal donde se realizará el sorteo", true),
                        OptionData(OptionType.STRING, "tiempo", "Tiempo del sorteo en segundos", true),
                        OptionData(OptionType.INTEGER, "ganadores", "Número de ganadores", true),
                        OptionData(OptionType.STRING, "premio", "El premio a sortear", true),
                        OptionData(OptionType.USER, "host", "El host del sorteo", false)
                    ),
            )
            .addSubcommands(
                SubcommandData("acabar", "Acabar un sorteo")
                    .addOptions(
                        OptionData(OptionType.INTEGER, "mensaje", "La ID del mensaje del sorteo a acabar", true)
                    ),
            )
            .addSubcommands(
                SubcommandData("repetir", "Repetir un sorteo")
                    .addOptions(
                        OptionData(OptionType.INTEGER, "mensaje", "La ID del mensaje del sorteo a repetir", true)
                    ),
            )
            .addSubcommands(
                SubcommandData("info", "Información de un sorteo")
                    .addOptions(
                        OptionData(OptionType.INTEGER, "mensaje", "La ID del mensaje del sorteo a dar información", true)
                    ),
            )
}