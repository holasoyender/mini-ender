package commands.message.bot

import database.schema.Warnings
import enums.Severity
import interfaces.Command
import interfaces.CommandResponse
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.utils.TimeFormat
import utils.Emojis
import utils.Emojis.f

class Warnings: Command {
    override fun execute(event: MessageReceivedEvent, args: List<String>): CommandResponse {

        val warnings = Warnings.getAll(event.guild.id)

        if (warnings.isEmpty()) {
            event.message.reply("${f(Emojis.error)}  No hay ningún aviso activo en este servidor").queue()
            return CommandResponse.success()
        }

        val action = args.getOrNull(1)
        if(action == null) {

            val messages = formatMessages(warnings.filter { !it.resolved }.filter { !it.ignored })

            if (messages.isEmpty()) {
                event.message.reply("${f(Emojis.error)}  No hay ningún aviso activo en este servidor").queue()
                return CommandResponse.success()
            }

            messages.chunked(10).forEach { chunk ->
                if(chunk == messages.chunked(10).first())
                {
                    event.message.reply("```md\n# Lista de avisos activos de este servidor\n# Forma de uso de este comando: warnings <acción/filtro> [ID aviso]\n\nAcciones posibles: resolve, delete, ignore, info\nFiltros posibles: all, critical, very high, high, medium, low, very low, ignored, resolved\n\n${chunk.joinToString("\n")}```").queue()
                }
                else
                {
                    event.channel.sendMessage("```md\n${chunk.joinToString("\n")}```").queue()
                }
            }
        } else {

            val filterInput = args.subList(1, args.size).joinToString(" ")

            val severities = mutableListOf(
                Severity.CRITICAL,
                Severity.VERY_HIGH,
                Severity.HIGH,
                Severity.MEDIUM,
                Severity.LOW,
                Severity.VERY_LOW,
            ).map { it.name.lowercase().replace(Regex("_"), " ") }

            if(filterInput.lowercase() == "ignored") {

                val ignoredWarnings = warnings.filter { it.ignored }

                if(ignoredWarnings.isEmpty()) {
                    event.message.reply("${f(Emojis.error)}  No hay ningún aviso ignorado en este servidor").queue()
                    return CommandResponse.success()
                }

                val messages = formatMessages(ignoredWarnings)

                messages.chunked(10).forEach { chunk ->
                    if(chunk == messages.chunked(10).first())
                    {
                        event.message.reply("```md\n# Lista de avisos ignorados de este servidor\n\n${chunk.joinToString("\n")}```").queue()
                    }
                    else
                    {
                        event.channel.sendMessage("```md\n${chunk.joinToString("\n")}```").queue()
                    }
                }
                return CommandResponse.success()
            }

            if(filterInput.lowercase() == "resolved") {

                val resolvedWarnings = warnings.filter { it.resolved }

                if(resolvedWarnings.isEmpty()) {
                    event.message.reply("${f(Emojis.error)}  No hay ningún aviso resuelto en este servidor").queue()
                    return CommandResponse.success()
                }

                val messages = formatMessages(resolvedWarnings)

                messages.chunked(10).forEach { chunk ->
                    if(chunk == messages.chunked(10).first())
                    {
                        event.message.reply("```md\n# Lista de avisos resueltos de este servidor\n\n${chunk.joinToString("\n")}```").queue()
                    }
                    else
                    {
                        event.channel.sendMessage("```md\n${chunk.joinToString("\n")}```").queue()
                    }
                }
                return CommandResponse.success()
            }

            if (severities.contains(filterInput.lowercase())) {
                val severity = Severity.valueOf(filterInput.uppercase().replace(Regex(" "), "_"))
                val filteredWarnings = warnings.filter { it.severity == severity }

                if (filteredWarnings.isEmpty()) {
                    return CommandResponse.error("No hay ningún aviso activo de este servidor con la severidad $filterInput")
                }

                val messages = formatMessages(filteredWarnings)
                messages.chunked(10).forEach { chunk ->
                    if (chunk == messages.chunked(10).first()) {
                        event.message.reply(
                            "```md\n# Lista de avisos activos de este servidor con el filtro ${severity.name}\n\n${
                                chunk.joinToString(
                                    "\n"
                                )
                            }```"
                        ).queue()
                    } else {
                        event.channel.sendMessage("```md\n${chunk.joinToString("\n")}```").queue()
                    }
                }
            } else {

                val warningId =
                    args.getOrNull(2) ?: return CommandResponse.error("Esta acción requiere un ID de aviso válido")

                val warning = warnings.find { it.id == warningId }
                    ?: return CommandResponse.error(" No se ha encontrado ningún aviso con la ID $warningId")

                when (action.lowercase()) {
                    "resolve",
                    "resuelve",
                    "resuelto",
                    "resuelta",
                    "solucionada",
                    "solucionar",
                    "resolver" -> {
                        warning.resolved = true
                        warning.save()
                        event.message.reply("${Emojis.success}  El aviso con la ID ${warning.id} ha sido resuelto")
                            .queue()
                    }

                    "delete",
                    "borrar",
                    "del",
                    "remove",
                    "quitar" -> {
                        warning.delete()
                        event.message.reply("${Emojis.success}  El aviso con la ID ${warning.id} ha sido eliminado")
                            .queue()
                    }

                    "ignore" -> {
                        warning.ignored = true
                        warning.save()
                        event.message.reply("${Emojis.success}  El aviso con la ID ${warning.id} ha sido ignorado")
                            .queue()
                    }

                    "info" -> {
                        val embed = EmbedBuilder()
                            .setAuthor("Información del aviso con la ID ${warning.id}", null, event.guild.iconUrl ?: event.jda.selfUser.effectiveAvatarUrl)
                            .setDescription("```${warning.message}```")
                            .addField("ID", "`${warning.id}`", true)
                            .addField("Visto por primera vez", "${TimeFormat.DEFAULT.format(warning.firstSeen)} (${TimeFormat.RELATIVE.format(warning.firstSeen)})", true)
                            .addField("Visto por última vez", "${TimeFormat.DEFAULT.format(warning.lastSeen)} (${TimeFormat.RELATIVE.format(warning.lastSeen)})", true)
                            .addField("Severidad", warning.severity.name, true)
                            .addField("Ignorado", if(warning.ignored) "Si" else "No", true)
                            .addField("Resuelto", if(warning.ignored) "Si" else "No", true)
                            .setColor(0x2f3136)

                        event.message.replyEmbeds(embed.build()).queue()
                    }

                    else -> {
                        event.message.reply("${f(Emojis.error)}  La acción `$action` no es válida").queue()
                    }
                }
            }
        }


        return CommandResponse.success()
    }

    private fun formatMessages(warnings: List<Warnings>): List<String> {

        val messages = mutableListOf<String>()
        for (warning in warnings) {

            val emoji = when(warning.severity) {
                Severity.VERY_LOW -> "🟢"
                Severity.LOW -> "🟢"
                Severity.MEDIUM -> "🟡"
                Severity.HIGH -> "🔴"
                Severity.VERY_HIGH -> "🔴"
                Severity.CRITICAL -> "🔴⚠️"
                Severity.UNKNOWN -> "❔"
                Severity.NONE -> "❔"
            }

            val content = if (warning.message.length > 150) warning.message.substring(0, 150) + "..." else warning.message
            messages.add("$emoji  [${warning.id}] $content  -  Visto ${warning.repeats} veces")
        }
        return messages
    }

    override val name: String
        get() = "warnings"
    override val description: String
        get() = "Muestra el tiempo que lleva encendido el bot"
    override val aliases: List<String>
        get() = listOf("avisos", "warning")
    override val usage: String
        get() = "<acción/filtro> [ID Warning]"
    override val category: String
        get() = "Bot"
    override val enabled: Boolean
        get() = true
    override val ownerOnly: Boolean
        get() = false
    override val guildOnly: Boolean
        get() = true
    override val global: Boolean
        get() = false
    override val permissions: List<Permission>
        get() = listOf(Permission.MANAGE_SERVER)
    override val permissionLevel: Int
        get() = 3
    override val botPermissions: List<Permission>
        get() = listOf()
}