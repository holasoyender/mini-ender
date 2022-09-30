package managers

import config.Env.PREFIX
import interfaces.Command
import interfaces.SimpleCommand
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import utils.Constants.OWNER_IDS
import utils.Emojis
import utils.Emojis.f

class CommandManager {

    private val commands: MutableList<Command> = ArrayList()
    private val simpleCommands: MutableList<SimpleCommand> = ArrayList()

    fun getCommands(): List<Command> = commands
    fun getSimpleCommands(): List<SimpleCommand> = simpleCommands

    init {
        CommandInitializer(this)
    }

    /*void*/
    fun registerCommand(command: Command) {
        val name = command.name
        val aliases = command.aliases

        if (commands.stream().anyMatch { it.name == name || it.aliases.contains(name) }) {
            throw IllegalArgumentException("Un comando con el nombre $name ya existe")
        }
        if (commands.stream().anyMatch { it.aliases.containsAll(aliases) }) {
            throw IllegalArgumentException("Un comando con los alias $aliases ya existe")
        }

        commands.add(command)
    }

    /*void*/
    fun registerSimpleCommand(command: SimpleCommand) {
        val name = command.name
        val aliases = command.aliases

        if (simpleCommands.stream().anyMatch { it.name == name || it.aliases.contains(name) }) {
            throw IllegalArgumentException("Un comando simple con el nombre $name ya existe")
        }
        if (aliases.isNotEmpty())
            if (simpleCommands.stream().anyMatch { it.aliases.containsAll(aliases) }) {
                throw IllegalArgumentException("Un comando simple con los alias $aliases ya existe")
            }

        if (commands.stream().anyMatch { it.name == name || it.aliases.contains(name) }) {
            throw IllegalArgumentException("Un comando con el nombre $name ya existe (Simple)")
        }
        if (aliases.isNotEmpty())
            if (commands.stream().anyMatch { it.aliases.containsAll(aliases) }) {
                throw IllegalArgumentException("Un comando con los alias $aliases ya existe (Simple)")
            }

        simpleCommands.add(command)
    }

    /*void*/
    fun unregisterCommand(command: Command) {
        if (commands.contains(command)) {
            commands.remove(command)
        } else {
            throw IllegalArgumentException("El comando $command no existe")
        }
    }

    fun run(event: MessageReceivedEvent) {

        val content = event.message.contentRaw
        val prefix = PREFIX ?: "-"
        val args = content.slice(prefix.length until content.length).split(" ")
        val invoker = args[0]

        val command = commands.firstOrNull { it.name == invoker.lowercase() || it.aliases.contains(invoker.lowercase()) }
        if (command != null) {

            /*initial checks*/
            if (!command.enabled) {
                event.message.reply("${f(Emojis.error)}  El comando ${command.name} está deshabilitado").queue()
                return
            }
            if (command.guildOnly && event.isFromGuild) {
                event.message.reply("${f(Emojis.error)}  El comando ${command.name} solo puede ser usado en un servidor")
                    .queue()
                return
            }
            if (command.ownerOnly && !OWNER_IDS.contains(event.author.id)) {
                event.message.reply("${f(Emojis.error)}  El comando ${command.name} solo puede ser usado por el equipo de desarrollo")
                    .queue()
                return
            }

            /*permissions checks*/
            if (command.permissions.isNotEmpty()) {
                if (event.isFromGuild) {
                    val member = event.member
                    if (member != null) {
                        val missingPermissions = command.permissions.filter { !member.hasPermission(it) }
                        if (missingPermissions.isNotEmpty()) {
                            event.message.reply(
                                "${f(Emojis.error)}  No tienes los permisos necesarios para usar el comando ${command.name}\nNecesitas los siguientes permisos: `${
                                    missingPermissions.joinToString(
                                        ", "
                                    )
                                }`"
                            ).queue()
                            return
                        }
                    }
                } else {
                    event.message.reply("${f(Emojis.error)}  El comando ${command.name} solo puede ser usado en un servidor")
                        .queue()
                    return
                }
            }
            if (command.botPermissions.isNotEmpty()) {
                if (event.isFromGuild) {
                    val selfMember = event.guild.selfMember
                    val missingPermissions = command.botPermissions.filter { !selfMember.hasPermission(it) }
                    if (missingPermissions.isNotEmpty()) {
                        event.message.reply(
                            "${f(Emojis.error)}  No tengo los permisos necesarios para usar el comando ${command.name}\nNecesito los siguientes permisos: `${
                                missingPermissions.joinToString(
                                    ", "
                                )
                            }`"
                        ).queue()
                        return
                    }
                } else {
                    event.message.reply("${f(Emojis.error)}  El comando ${command.name} solo puede ser usado en un servidor")
                        .queue()
                    return
                }
            }

            val worker = command.run { execute(event, args) }

            if (worker.exitStatus != 0) {
                event.message.reply( "${f(Emojis.error)}  El comando ${command.name} ha fallado con el siguiente error: \n`${worker.error ?: "Desconocido"}`")
                    .queue()
            }

        } else {

            val simpleCommand = simpleCommands.firstOrNull { it.name == invoker || it.aliases.contains(invoker) }
            if (simpleCommand != null) {

                if(simpleCommand.reply) {
                    if(simpleCommand.components.isNotEmpty()) {
                        event.message.reply(simpleCommand.response).addComponents(simpleCommand.components).queue()
                        return
                    }
                    event.message.reply(simpleCommand.response).queue()
                } else {
                    if(simpleCommand.components.isNotEmpty()) {
                        event.channel.sendMessage(simpleCommand.response).addComponents(simpleCommand.components).queue()
                        return
                    }
                    event.channel.sendMessage(simpleCommand.response).queue()
                }

            }

        }

    }

}