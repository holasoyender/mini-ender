package managers

import commands.bot.*
import interfaces.SimpleCommand
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import utils.Emojis

class CommandInitializer(manager: CommandManager) {

    init {

        /*
        * Comandos normales
        */

        manager.registerCommand(Ping())
        manager.registerCommand(Uptime())


        /*
        * Comandos simples de una sola respuesta
        */
        manager.registerSimpleCommand(SimpleCommand("hola", listOf("hi", "hello"), "Hola!", listOf()))
        manager.registerSimpleCommand(SimpleCommand("github", listOf("git", "gh", "repo", "repositorio"), "${Emojis.success}  **Este bot es open source!** Y puedes ver su código fuente haciendo click en el **botón!**", listOf(
            ActionRow.of(
                Button.link("https://github.com/holasoyender/mini-ender", "Repositorio")
            )
        )))

    }
}