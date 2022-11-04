package commands.message.bot

import commandManager
import config.Env.PREFIX
import database.schema.Guild
import interfaces.Command
import interfaces.CommandResponse
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.selections.SelectOption
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu
import net.dv8tion.jda.internal.entities.emoji.CustomEmojiImpl
import java.awt.Color
import java.time.Instant


class Help: Command {
    override fun execute(event: MessageReceivedEvent, args: List<String>): CommandResponse {

        /*
        * Por alguna razón se me ha olvidado quitar el primer argumento de la lista de args, por lo que el primer argumento
        * siempre va a ser el comando ejecutado, me da pereza arreglarlo ahora, asi que lo dejo asi
        *
        * Ahora que lo pienso escribir esto me ha llevado más tiempo que arreglarlo
        * sabes que, da igual
        *  - holasoyender 29/09/2022
        * */

        val config = Guild.get(event.guild.id) ?: Guild(event.guild.id, PREFIX ?: "-", arrayOf(), "", false)

        if(args.size > 1) {
            val _input = args[1]
            val input = _input.lowercase()

            val command = commandManager?.getCommands()?.find { it.name == input || it.aliases.contains(input) }
                ?: return CommandResponse.error("No se ha encontrado el comando $_input")

            val embed = EmbedBuilder()
                .setAuthor("Comando: " + command.name, null, event.jda.selfUser.avatarUrl)
                .setFooter("> " + event.author.asTag, event.author.avatarUrl ?: "")
                .setThumbnail("https://cdn.discordapp.com/attachments/934142973418016838/1025062210013249556/emoji.png")
                .setColor(Color.decode("#2f3136"))
                .setTimestamp(Instant.now())
                .setDescription(
                    """

    Para listar todos los comandos puedes usar `${config.prefix}help`
    
    **Nombre del comando:** `${command.name}`
    **Descripción:** ${command.description}
    **Forma de uso:** `${config.prefix}${command.name} ${command.usage}`
    **Aliases:** ${command.aliases.joinToString(", ")}
    **Permisos:** ```${ if(command.permissions.isEmpty()) "Ninguno!" else command.permissions.joinToString(", ") { it.getName() } }```
        """.trimIndent()
                )

            event.channel.sendMessageEmbeds(embed.build()).setActionRow(
                Button.primary("cmd::help:${event.author.id}", "Todos los comandos")
            ).queue()
            return CommandResponse.success()

        }

        val embed: EmbedBuilder = EmbedBuilder()
            .setAuthor("Lista de comandos de ${event.jda.selfUser.name}", null, event.jda.selfUser.avatarUrl)
            .setFooter("> " + event.author.asTag, event.author.avatarUrl ?: "")
            .setThumbnail("https://cdn.discordapp.com/attachments/934142973418016838/1025062210013249556/emoji.png")
            .setColor(Color.decode("#2f3136"))
            .setTimestamp(Instant.now())
            .setDescription(
                """**Hola** :wave:, soy `${event.jda.selfUser.name}`, un bot de ayuda para el servidor de **KenaBot**!
                Para obtener información de un comando en específico usa `${config.prefix}help <comando>`
                """
            )

        val categories = commandManager?.getCommands()?.groupBy { it.category }

        event.message.replyEmbeds(embed.build()).addActionRow(
                StringSelectMenu.create("cmd::help:${event.author.id}")
                    .setPlaceholder("Selecciona una categoría")
                    .setMaxValues(1)
                    .setMinValues(0)
                    .addOptions(
                        categories?.map { SelectOption.of(it.key, "help:${it.key}").withDescription("Comandos de la categoría ${it.key}").withEmoji(
                            Emoji.fromCustom(CustomEmojiImpl("rigth", 940316141782458418, false))) } ?: listOf()
                    ).addOption("Comandos simples", "help:simple", "Lista de comandos simples", Emoji.fromCustom(CustomEmojiImpl("slash", 941024012270710874, false)))
                    .build()
        ).queue()

        return CommandResponse.success()
    }

    override val name: String
        get() = "help"
    override val description: String
        get() = "Muestra la lista de comandos"
    override val aliases: List<String>
        get() = listOf("h")
    override val usage: String
        get() = ""
    override val category: String
        get() = "Bot"
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
    override val botPermissions: List<Permission>
        get() = listOf()
}