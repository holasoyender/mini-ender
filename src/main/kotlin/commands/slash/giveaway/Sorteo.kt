package commands.slash.giveaway

import database.schema.Sorteo
import interfaces.CommandResponse
import interfaces.SlashCommand
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.*
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.utils.TimeFormat
import net.dv8tion.jda.internal.entities.emoji.CustomEmojiImpl
import plugins.giveaway.GiveawayManager
import utils.Emojis
import utils.Emojis.f
import utils.Time
import java.awt.Color

class Sorteo: SlashCommand {
    override fun execute(event: SlashCommandInteractionEvent): CommandResponse {

        when (event.subcommandName) {
            "crear" -> {
                val channel = event.getOption("canal")?.asChannel ?: return CommandResponse.error("Debes especificar un canal valido")
                val time = event.getOption("tiempo")?.asString ?: return CommandResponse.error("Debes especificar un tiempo valido (Ej: 1d, 12m, 30d)")
                val winners = event.getOption("ganadores")?.asLong ?: return CommandResponse.error("Debes especificar un numero de ganadores valido")
                val prize = event.getOption("premio")?.asString ?: return CommandResponse.error("Debes especificar un premio para el sorteo")
                val host = event.getOption("host")?.asUser ?: event.user
                var style = event.getOption("estilo")?.asString ?: "normal"

                if(event.guild?.name?.lowercase() == "ibai")
                    style = "ibai"

                val formattedTime = Time.ms(time)
                if(formattedTime < 1 || formattedTime > 604800000) return CommandResponse.error("El tiempo debe ser mayor a 1ms y menor a 1 semana")

                if(channel !is TextChannel) return CommandResponse.error("Debes especificar un canal de texto valido")
                if(winners < 1 || winners > 10) return CommandResponse.error("El numero de ganadores debe estar entre 1 y 10")

                event.reply("${Emojis.success} Sorteo creado en ${(channel as TextChannel).asMention}").queue {
                    GiveawayManager.createGiveaway(event.guild!!, channel, formattedTime, winners, prize, host, style, it)
                }

            }
            "acabar" -> {
                val messageId = event.getOption("mensaje")?.asString ?: return CommandResponse.error("Debes especificar un id de un mensaje valida")

                val giveaway = Sorteo.get(messageId) ?: return CommandResponse.error("No se ha encontrado un sorteo con esa id de mensaje")

                if(giveaway.ended) return CommandResponse.error("Este sorteo ya ha terminado")
                val channel = event.guild!!.getTextChannelById(giveaway.channelId) ?: return CommandResponse.error("No se ha encontrado el canal del sorteo")

                channel.retrieveWebhooks().queue { webhooks ->
                    val webhook = webhooks.firstOrNull { w -> w.name == "Sorteo" }

                    if(webhook == null) {
                        event.reply("${f(Emojis.error)} No se ha encontrado el webhook del sorteo").queue()
                    } else {
                        event.reply("${Emojis.loading} Finalizando el sorteo...").queue { msg ->
                            GiveawayManager.endGiveaway(webhook, giveaway, msg, event.guild!!)
                        }
                    }
                }

            }
            "repetir" -> {
                val messageId = event.getOption("mensaje")?.asString ?: return CommandResponse.error("Debes especificar un id de un mensaje valida")

                val giveaway = Sorteo.get(messageId) ?: return CommandResponse.error("No se ha encontrado un sorteo con esa id de mensaje")

                if(!giveaway.ended) return CommandResponse.error("Este sorteo no ha terminado")
                val channel = event.guild!!.getTextChannelById(giveaway.channelId) ?: return CommandResponse.error("No se ha encontrado el canal del sorteo")

                channel.retrieveWebhooks().queue { webhooks ->
                    val webhook = webhooks.firstOrNull { w -> w.name == "Sorteo" }

                    if(webhook == null) {
                        event.reply("${f(Emojis.error)} No se ha encontrado el webhook del sorteo").queue()
                    } else {
                        event.reply("${Emojis.loading} Repitiendo el sorteo...").queue { msg ->
                            GiveawayManager.redoGiveaway(webhook, giveaway, msg, event.guild!!)
                        }
                    }
                }
            }
            "info" -> {
                val messageId = event.getOption("mensaje")?.asString ?: return CommandResponse.error("Debes especificar un id de un mensaje valida")

                val giveaway = Sorteo.get(messageId) ?: return CommandResponse.error("No se ha encontrado un sorteo con esa id de mensaje")

                event.replyEmbeds(
                    EmbedBuilder()
                        .setTitle("Información del sorteo")
                        .setColor(Color.decode("#2f3136"))
                        .addField("Canal", "<#${giveaway.channelId}>", true)
                        .addField("Tiempo", "${TimeFormat.DEFAULT.format(giveaway.endAfter + giveaway.startedAt)}  (${ TimeFormat.RELATIVE.format(giveaway.endAfter + giveaway.startedAt)})", true)
                        .addField("Ganadores", "`${giveaway.winnerCount}` ganador${if(giveaway.winnerCount > 1) "es" else ""}", true)
                        .addField("Premio", "```${giveaway.prize}```", false)
                        .addField("Host", "<@${giveaway.hostId}>", true)
                        .addField("Mensaje", "[Click aquí](https://discord.com/channels/${event.guild!!.id}/${giveaway.channelId}/${giveaway.messageId})", true)
                        .addField("Estado", if(giveaway.ended) "Terminado" else "En curso", true)
                        .addField("Ganadores", if(giveaway.ended) giveaway.winnerIds.joinToString(", ") { "<@${it}>" } else "Ninguno", true)
                        .addField("Participantes totales", "`${giveaway.clickers.size}` participantes", true)
                        .setThumbnail(event.jda.selfUser.avatarUrl ?: event.jda.selfUser.defaultAvatarUrl)
                        .build()
                ).setEphemeral(false).addActionRow(
                    Button.danger("cmd::giveaway:end:${giveaway.messageId}", "Acabar el sorteo").withDisabled(giveaway.ended),
                    Button.success("cmd::giveaway:redo:${giveaway.messageId}", "Repetir el sorteo").withDisabled(!giveaway.ended),
                    Button.link("https://discord.com/channels/${event.guild!!.id}/${giveaway.channelId}/${giveaway.messageId}", "Ir al mensaje"),
                    Button.primary("cmd::giveaway:reload:${giveaway.messageId}", Emoji.fromCustom(CustomEmojiImpl("loop", 952242523521294456, false)))
                ).queue()
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
        get() = listOf(Permission.MESSAGE_MANAGE, Permission.MANAGE_SERVER)
    override val permissionLevel: Int
        get() = 2
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
                        OptionData(OptionType.USER, "host", "El host del sorteo", true),
                        OptionData(OptionType.STRING, "estilo", "Estilo del embed del sorteo", false)
                            .addChoice("Normal", "normal")
                            .addChoice("Mínima información", "minimum")
                            .addChoice("Ibai", "ibai")

                    ),
            )
            .addSubcommands(
                SubcommandData("acabar", "Acabar un sorteo")
                    .addOptions(
                        OptionData(OptionType.STRING, "mensaje", "La ID del mensaje del sorteo a acabar", true)
                    ),
            )
            .addSubcommands(
                SubcommandData("repetir", "Repetir un sorteo")
                    .addOptions(
                        OptionData(OptionType.STRING, "mensaje", "La ID del mensaje del sorteo a repetir", true)
                    ),
            )
            .addSubcommands(
                SubcommandData("info", "Información de un sorteo")
                    .addOptions(
                        OptionData(OptionType.STRING, "mensaje", "La ID del mensaje del sorteo a dar información", true)
                    ),
            )
}