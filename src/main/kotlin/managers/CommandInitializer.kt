package managers

import commands.bot.*
import interfaces.SimpleCommand

class CommandInitializer(manager: CommandManager) {

    init {

        /*
        * Comandos normales
        * */

        manager.registerCommand(Ping())


        /*
        * Comandos simples de una sola respuesta
        * */
        manager.registerSimpleCommand(SimpleCommand("hola", listOf("hi", "hello"), "Hola!"))

    }
}