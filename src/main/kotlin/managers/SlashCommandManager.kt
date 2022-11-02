package managers

import interfaces.SlashCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import utils.Constants.OWNER_IDS
import utils.Emojis
import utils.Emojis.f

class SlashCommandManager {

    private val commands: MutableList<SlashCommand> = ArrayList()

    fun getCommands(): List<SlashCommand> = commands

    init {
        SlashCommandInitializer(this)
    }

    /*void*/
    fun registerCommand(command: SlashCommand) {
        val name = command.name

        if (commands.stream().anyMatch { it.name == name }) {
            throw IllegalArgumentException("Un comando de slash con el nombre $name ya existe")
        }

        commands.add(command)
    }

    @Suppress("unused") /*void*/
    fun unregisterCommand(command: SlashCommand) {
        if (commands.contains(command)) {
            commands.remove(command)
        } else {
            throw IllegalArgumentException("El comando de slash $command no existe")
        }
    }

    fun run(event: SlashCommandInteractionEvent) {

        val invoker = event.name

        val command = commands.firstOrNull { it.name == invoker.lowercase() }
        if (command != null) {

            /*initial checks*/
            if (!command.enabled) {
                event.reply("${f(Emojis.error)}  El comando `${command.name}` está deshabilitado").setEphemeral(true)
                    .queue()
                return
            }
            if (command.guildOnly && !event.isFromGuild) {
                event.reply("${f(Emojis.error)}  El comando `${command.name}` solo puede ser usado en un servidor")
                    .setEphemeral(true).queue()
                return
            }
            if (command.ownerOnly && !OWNER_IDS.contains(event.user.id)) {
                event.reply("${f(Emojis.error)}  El comando `${command.name}` solo puede ser usado por el equipo de desarrollo")
                    .setEphemeral(true).queue()
                return
            }

            /*permissions checks*/
            if (command.permissions.isNotEmpty()) {
                if (event.isFromGuild) {
                    val member = event.member
                    if (member != null) {
                        val missingPermissions = command.permissions.filter { !member.hasPermission(it) }
                        if (missingPermissions.isNotEmpty()) {
                            event.reply(
                                "${f(Emojis.error)}  No tienes los permisos necesarios para usar el comando ${command.name}\nNecesitas los siguientes permisos: `${
                                    missingPermissions.joinToString(
                                        ", "
                                    )
                                }`"
                            ).setEphemeral(true).queue()
                            return
                        }
                    }
                } else {
                    event.reply("${f(Emojis.error)}  El comando ${command.name} solo puede ser usado en un servidor")
                        .setEphemeral(true)
                        .queue()
                    return
                }
            }
            if (command.botPermissions.isNotEmpty()) {
                if (event.isFromGuild) {
                    val selfMember = event.guild!!.selfMember
                    val missingPermissions = command.botPermissions.filter { !selfMember.hasPermission(it) }
                    if (missingPermissions.isNotEmpty()) {
                        event.reply(
                            "${f(Emojis.error)}  No tengo los permisos necesarios para usar el comando ${command.name}\nNecesito los siguientes permisos: `${
                                missingPermissions.joinToString(
                                    ", "
                                )
                            }`"
                        ).setEphemeral(true).queue()
                        return
                    }
                } else {
                    event.reply("${f(Emojis.error)}  El comando ${command.name} solo puede ser usado en un servidor")
                        .setEphemeral(true)
                        .queue()
                    return
                }
            }

            val worker = command.run { execute(event) }

            if (worker.exitStatus != 0) {
                event.reply("${f(Emojis.error)}  El comando ${command.name} ha fallado con el siguiente error: \n`${worker.error ?: "Desconocido"}`")
                    .setEphemeral(true)
                    .queue()
            }

        } else {
            event.reply("${f(Emojis.error)}  El comando `$invoker` no existe").setEphemeral(true).queue()
            return
        }
    }
}