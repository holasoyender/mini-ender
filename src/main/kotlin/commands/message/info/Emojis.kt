package commands.message.info

import emoji.Emoji
import emoji.EmojiManager
import interfaces.Command
import interfaces.CommandResponse
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.utils.TimeFormat
import java.awt.Color

class Emojis: Command {
    override fun execute(event: MessageReceivedEvent, args: List<String>): CommandResponse {

        val emoji = event.message.mentions.customEmojis.firstOrNull() ?: args.getOrNull(1)
            ?.let {
                try {
                    event.guild.getEmojiById(it)
                } catch (e: Exception) {
                    null
                }
            } ?: args.getOrNull(1)

        if (emoji == null) {

            event.guild.retrieveEmojis().queue({ guildEmojis ->

                if (guildEmojis.isEmpty()) {
                    event.message.reply("Este servidor no tiene emojis personalizados!").queue()
                    return@queue
                }

                val staticEmojis = guildEmojis.filter { it.isAnimated.not() }
                val animatedEmojis = guildEmojis.filter { it.isAnimated }
                val unavailableEmojis = guildEmojis.filter { it.isAvailable.not() }

                val embed = EmbedBuilder()
                    .setAuthor("Lista de emojis del servidor", null, event.guild.iconUrl)
                    .setDescription("Este servidor tiene ${guildEmojis.size} emojis, para obtener más información sobre un emoji en concreto usa `emoji <emoji>`")
                    .setColor(Color.decode("#2f3136"))
                if (staticEmojis.isNotEmpty()) {
                    var emojiList = staticEmojis.joinToString(" ") { it.asMention }
                    if (emojiList.length > 1024) emojiList = emojiList.substring(0, 1020) + "..."
                    embed.addField("Emojis estáticos", emojiList, false)
                }
                if (animatedEmojis.isNotEmpty())
                    embed.addField("Emojis animados", animatedEmojis.joinToString(" ") { it.asMention }, true)
                if (unavailableEmojis.isNotEmpty())
                    embed.addField("Emojis no disponibles", unavailableEmojis.joinToString(" ") { it.asMention }, true)

                event.message.replyEmbeds(embed.build()).queue()
            }, {
                event.message.reply("No he podido obtener la lista de emojis").queue()
            })
        } else {

            val customEmoji = event.message.mentions.customEmojis.firstOrNull() ?: args.getOrNull(1)
                ?.let {
                    try {
                        event.guild.getEmojiById(it)
                    } catch (e: Exception) {
                        null
                    }
                }

            if (customEmoji != null) {
                val embed = EmbedBuilder()
                    .setColor(Color.decode("#2f3136"))
                    .setAuthor("Información del emoji \"${customEmoji.name}\"", null, event.guild.iconUrl)
                    .addField("ID", customEmoji.id, true)
                    .addField(
                        "Creado el",
                        "${TimeFormat.DEFAULT.format(customEmoji.timeCreated)} (${TimeFormat.RELATIVE.format(customEmoji.timeCreated)})",
                        true
                    )
                    .addField("Animado", if (customEmoji.isAnimated) "Si" else "No", true)
                    .addField("URL de la imagen", "[Imagen del emoji](${customEmoji.imageUrl})", true)
                    .addField("Nombre", customEmoji.name, true)
                    .setDescription("```${customEmoji.asMention}```")
                    .setImage(customEmoji.imageUrl)

                event.message.replyEmbeds(embed.build()).queue()
                return CommandResponse.success()
            } else {
                val tEmoji = EmojiManager.getByUnicode(emoji.toString())
                    ?: return CommandResponse.error("No he podido encontrar ese emoji")

                val embed = EmbedBuilder()
                    .setColor(Color.decode("#2f3136"))
                    .setAuthor("Información del emoji \"${tEmoji.aliases.first()}\"", null, event.guild.iconUrl)
                    .addField("ID", tEmoji.unicode, true)
                    .addField("Nombre", tEmoji.aliases.first(), true)
                    .addField("Categoría", tEmoji.category.displayName, true)
                    .setImage("https://twemoji.maxcdn.com/v/latest/72x72/${getCodePoints(tEmoji)}.png")
                    .setDescription("```${tEmoji.unicode}```")

                event.message.replyEmbeds(embed.build()).queue()

            }

        }
        return CommandResponse.success()
    }

    private fun getCodePoints(emoji: Emoji): String {
        val points = mutableListOf<Int>()
        var char: Int
        var previous = 0
        var i = 0

        while (i < emoji.unicode.length) {
            char = emoji.unicode.codePointAt(i++)
            if (previous != 0) {
                points.add(0x10000 + (previous - 0xD800) * 0x400 + (char - 0xDC00))
                previous = 0
            } else if (char in 0xD800..0xDBFF) {
                previous = char
            } else {
                points.add(char)
            }
            i += Character.charCount(char)
        }
        return points.joinToString("-") { it.toString(16) }
    }

    override val name: String
        get() = "emojis"
    override val description: String
        get() = "Muestra todos los emojis del servidor o información sobre un emoji en concreto"
    override val aliases: List<String>
        get() = listOf("emoji", "emojilist", "emojilista", "emojiinfo")
    override val usage: String
        get() = "[emoji]"
    override val category: String
        get() = "Información"
    override val enabled: Boolean
        get() = true
    override val ownerOnly: Boolean
        get() = false
    override val guildOnly: Boolean
        get() = true
    override val global: Boolean
        get() = false
    override val permissions: List<Permission>
        get() = listOf()
    override val permissionLevel: Int
        get() = 1
    override val botPermissions: List<Permission>
        get() = listOf()
}