package commands.message.info

import interfaces.Command
import interfaces.CommandResponse
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

class JVM: Command {
    override fun execute(event: MessageReceivedEvent, args: List<String>): CommandResponse {

        val runtime = Runtime.getRuntime()
        val msg = """
            ==== Memoria ====
            Used: ${(runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024} MB
            Total: ${runtime.totalMemory() / 1024 / 1024} MB
            Max: ${runtime.maxMemory() / 1024 / 1024} MB
            Free: ${runtime.freeMemory() / 1024 / 1024} MB
            
            === Sistema ===
            OS: ${System.getProperty("os.name")} ${System.getProperty("os.version")} ${System.getProperty("os.arch")}
            Cores: ${runtime.availableProcessors()}
            
            === JVM ===
            Version: ${System.getProperty("java.vm.name")} ${System.getProperty("java.version")} ${System.getProperty("java.vendor")}
            Spec: ${System.getProperty("java.specification.name")} ${System.getProperty("java.specification.version")} ${System.getProperty("java.specification.vendor")}
            """.trimIndent()

        event.message.reply("```yml\n$msg```").queue()

        return CommandResponse.success()
    }

    override val name: String
        get() = "jvm"
    override val description: String
        get() = "Muestra información sobre la JVM"
    override val aliases: List<String>
        get() = listOf("jvm-info", "java", "memory")
    override val usage: String
        get() = ""
    override val category: String
        get() = "Info"
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
    override val botPermissions: List<Permission>
        get() = listOf()
}