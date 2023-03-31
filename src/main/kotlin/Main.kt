
import api.ApiLauncher
import config.Env
import events.*
import managers.CommandManager
import managers.SlashCommandManager
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
//import net.dv8tion.jda.api.utils.ChunkingFilter
import net.dv8tion.jda.api.utils.Compression
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag
import net.dv8tion.jda.api.utils.messages.MessageRequest
import okhttp3.OkHttpClient
import java.util.logging.Logger
import javax.security.auth.login.LoginException

var commandManager: CommandManager? = null
var slashCommandManager: SlashCommandManager? = null
var jda:JDA? = null
fun main(args: Array<String>) {

    val builder = DefaultShardManagerBuilder.createDefault(null)
    Logger.getLogger(OkHttpClient::class.java.name).level = java.util.logging.Level.FINE
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
        CacheFlag.STICKER,
        CacheFlag.SCHEDULED_EVENTS
    )

    builder.setBulkDeleteSplittingEnabled(true)
    builder.setCompression(Compression.ZLIB)
    builder.setActivity(Activity.watching("kenabot.xyz"))

    builder.setToken(Env.TOKEN ?: throw LoginException("No token provided"))

    builder.setEventPassthrough(true)

    builder.addEventListeners(
        MessageHandler(),
        ShardHandler(),
        InteractionHandler(),
        ModalHandler(),
        SlashHandler(),
        GuildHandler()
    )

    builder.setMemberCachePolicy(MemberCachePolicy.NONE)
    //builder.setChunkingFilter(ChunkingFilter.NONE)

    builder.disableIntents(
        GatewayIntent.GUILD_MESSAGE_TYPING,
        GatewayIntent.GUILD_PRESENCES,
        GatewayIntent.DIRECT_MESSAGE_REACTIONS,
        GatewayIntent.DIRECT_MESSAGE_TYPING,
        GatewayIntent.GUILD_INVITES
    )
    builder.enableIntents(
        GatewayIntent.DIRECT_MESSAGES,
        GatewayIntent.GUILD_MESSAGES,
        GatewayIntent.MESSAGE_CONTENT,
        GatewayIntent.GUILD_MEMBERS,
        GatewayIntent.GUILD_MESSAGE_REACTIONS,
        GatewayIntent.GUILD_EMOJIS_AND_STICKERS,
    )
    builder.setLargeThreshold(50)

    builder.setShardsTotal(-1)

    try {
        ApiLauncher.load(args)
        val shardingManager = builder.build()

        shardingManager.getShardById(0)?.let {
            jda = it
        }

        services.ServiceManager.load(shardingManager)

        commandManager = CommandManager()
        slashCommandManager = SlashCommandManager()
    } catch (e: LoginException) {
        e.printStackTrace()
    }
}