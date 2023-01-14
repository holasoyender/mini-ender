package commands.message.bot

import database.schema.Guild
import interfaces.Command
import interfaces.CommandResponse
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import utils.Emojis
import java.awt.Color

class Shards: Command {
    override fun execute(event: MessageReceivedEvent, args: List<String>, config: Guild): CommandResponse {

        event.message.reply("${Emojis.loading}  Cargando información interna...").queue {

            val embed = EmbedBuilder()
                .setAuthor("Shards de ${event.jda.selfUser.name}", null, event.jda.selfUser.avatarUrl)
                .setDescription("Este servidor está en la shard `${event.jda.shardInfo.shardId + 1}/${event.jda.shardInfo.shardTotal}`")
                .setColor(Color.decode("#2f3136"))

            //order from lowest to highest
            val shards = event.jda.shardManager!!.shards.sortedBy { s -> s.shardInfo.shardId }
            for (shard in shards) {
                val emoji = when(shard.status) {
                    JDA.Status.INITIALIZED,
                    JDA.Status.CONNECTED-> Emojis.online
                    JDA.Status.DISCONNECTED,
                    JDA.Status.SHUTTING_DOWN,
                    JDA.Status.SHUTDOWN,
                    JDA.Status.FAILED_TO_LOGIN -> Emojis.offline
                    else -> Emojis.warning
                }
                embed.addField("$emoji Shard ${shard.shardInfo.shardId + 1}", """
                    ```
                    Host: shard-${shard.shardInfo.shardId + 1}.kenabot.xyz
                    Servers: ${shard.guilds.size}
                    Users: ${shard.guilds.sumOf { it.memberCount }}
                    Heartbeat: ${shard.gatewayPing}ms
                    Estado: ${shard.status.name}
                    ```
                """.trimIndent(), true)
            }

            it.editMessageEmbeds(embed.build()).setContent("").mentionRepliedUser(false).queue()

        }
        return CommandResponse.success()
    }

    override val name: String
        get() = "shards"
    override val description: String
        get() = "Muestra información sobre los shards"
    override val aliases: List<String>
        get() = listOf("shard", "shard-info", "shardinfo", "cluster")
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
    override val permissionLevel: Int
        get() = 1
    override val botPermissions: List<Permission>
        get() = listOf()
}