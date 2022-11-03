
import config.Env
import events.*
import managers.CommandManager
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.api.utils.ChunkingFilter
import net.dv8tion.jda.api.utils.Compression
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag
import net.dv8tion.jda.api.utils.messages.MessageRequest
import javax.security.auth.login.LoginException

var commandManager: CommandManager? = null
var jda:JDA? = null
fun main() {

    val builder = DefaultShardManagerBuilder.createDefault(null)

    MessageRequest.setDefaultMentionRepliedUser(false)
    builder.disableCache(
        CacheFlag.EMOJI,
        CacheFlag.ACTIVITY,
        CacheFlag.MEMBER_OVERRIDES,
        CacheFlag.CLIENT_STATUS,
        CacheFlag.ONLINE_STATUS,
        CacheFlag.FORUM_TAGS,
        CacheFlag.VOICE_STATE,
        CacheFlag.STICKER,
        CacheFlag.ROLE_TAGS,
    )

    builder.setBulkDeleteSplittingEnabled(true)
    builder.setCompression(Compression.NONE)
    builder.setActivity(Activity.watching("kenabot.xyz"))

    builder.setToken(Env.TOKEN!!)

    builder.setEventPassthrough(true)

    builder.addEventListeners(
        MessageHandler(),
        ShardHandler(),
        InteractionHandler(),
        ModalHandler(),
        SlashHandler()
    )

    builder.setMemberCachePolicy(MemberCachePolicy.NONE)
    builder.setChunkingFilter(ChunkingFilter.NONE)

    builder.disableIntents(
        GatewayIntent.GUILD_MESSAGE_TYPING,
        GatewayIntent.GUILD_PRESENCES,
        GatewayIntent.DIRECT_MESSAGE_REACTIONS,
        GatewayIntent.DIRECT_MESSAGE_TYPING,
        GatewayIntent.GUILD_MESSAGE_REACTIONS,
        GatewayIntent.GUILD_INVITES
    )
    builder.enableIntents(
        GatewayIntent.DIRECT_MESSAGES,
        GatewayIntent.GUILD_MESSAGES,
        GatewayIntent.MESSAGE_CONTENT,
        GatewayIntent.GUILD_MEMBERS
    )
    builder.setLargeThreshold(50)

    builder.setShardsTotal(-1)

    try {
        val shardingManager = builder.build()

        shardingManager.getShardById(0)?.let {
            jda = it
        }

        services.ServiceManager.load(shardingManager)

        commandManager = CommandManager()
    } catch (e: LoginException) {
        e.printStackTrace()
    }
}