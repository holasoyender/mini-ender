package commands.message.dev

import database.schema.Guild
import interfaces.Command
import interfaces.CommandResponse
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import javax.script.ScriptEngineManager
import javax.script.ScriptException

class Eval: Command {
    override fun execute(event: MessageReceivedEvent, args: List<String>, config: Guild): CommandResponse {

        val toEval: String = args.subList(1, args.size).joinToString(" ")

        if (toEval.isEmpty())
            return CommandResponse.error("Debes de especificar un código a evaluar")


        val script = ScriptEngineManager().getEngineByName("nashorn")

        script.eval("var imports = new JavaImporter(java.io, java.lang, java.util, java.net);")
        script.put("client", event.jda)
        script.put("shard", event.jda.shardManager)
        script.put("channel", event.channel)
        script.put("message", event.message)
        if(event.isFromGuild) {
            script.put("guild", event.guild)
            script.put("member", event.member)
            script.put("config", config)
        }
        if(database.Redis.connection != null)
            script.put("redis", database.Redis)

        try {
            val res = script.eval("with (imports) { $toEval }")
            val result = res?.toString() ?: "void"
            event.message.reply("```kt\n${result.replace(event.jda.token.toRegex(), "T0K3N")}```").queue()
        } catch (ex: ScriptException) {
            event.message.reply("```kt\n${ex.message!!.replace(event.jda.token.toRegex(), "T0K3N")}```").queue()
        }
        return CommandResponse.success()
    }

    override val name: String
        get() = "eval"
    override val description: String
        get() = "Ejecuta código Kotlin"
    override val aliases: List<String>
        get() = listOf()
    override val usage: String
        get() = "<código>"
    override val category: String
        get() = "Dev"
    override val enabled: Boolean
        get() = true
    override val ownerOnly: Boolean
        get() = true
    override val guildOnly: Boolean
        get() = false
    override val global: Boolean
        get() = false
    override val permissions: List<Permission>
        get() = listOf()
    override val permissionLevel: Int
        get() = 10
    override val botPermissions: List<Permission>
        get() = listOf()
}