package managers

import commands.bot.*
import commands.info.*
import interfaces.SimpleCommand
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import utils.Emojis

class CommandInitializer(manager: CommandManager) {

    init {

        /*
        * Comandos normales
        */

        //Bot
        manager.registerCommand(Ping())
        manager.registerCommand(Uptime())
        manager.registerCommand(Help())

        //Info
        manager.registerCommand(Info())
        manager.registerCommand(Server())


        /*
        * Comandos simples de una sola respuesta
        */
        manager.registerSimpleCommand(SimpleCommand("hola", listOf("hi", "hello"), "Hola!", true, listOf()))
        manager.registerSimpleCommand(SimpleCommand("github", listOf("git", "gh", "repo", "repositorio"), "${Emojis.success}  **Este bot es open source!** Y puedes ver su código fuente haciendo click en el **botón!**", true, listOf(
            ActionRow.of(
                Button.link("https://github.com/holasoyender/mini-ender", "Repositorio")
            )
        )))
        manager.registerSimpleCommand(SimpleCommand("shrug", listOf(), "¯\\_(ツ)_/¯", false, listOf()))
        manager.registerSimpleCommand(SimpleCommand("ender", listOf(), "ender que?", true, listOf()))

    }
}