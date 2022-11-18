package managers

import commands.message.bot.*
import commands.message.info.*
import commands.message.ocio.*
import commands.message.moderation.*
import commands.message.dev.*
import commands.message.config.*
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
        manager.registerCommand(Error())
        manager.registerCommand(Ping())
        manager.registerCommand(Uptime())
        manager.registerCommand(Help())
        manager.registerCommand(Say())
        manager.registerCommand(Warnings())
        manager.registerCommand(Shards())

        //Info
        manager.registerCommand(Info())
        manager.registerCommand(Server())
        manager.registerCommand(JVM())
        manager.registerCommand(Avatar())
        manager.registerCommand(Bot())
        manager.registerCommand(Emojis())

        //Ocio
        manager.registerCommand(Perro())
        manager.registerCommand(Gato())
        manager.registerCommand(Tirar())
        manager.registerCommand(Panda())
        manager.registerCommand(Koala())
        manager.registerCommand(Duck())
        manager.registerCommand(Bird())

        //Moderación
        manager.registerCommand(Infrs())
        manager.registerCommand(Warn())
        manager.registerCommand(Clear())
        manager.registerCommand(Kick())
        manager.registerCommand(Infraction())
        manager.registerCommand(Ban())
        manager.registerCommand(Tempban())
        manager.registerCommand(Unban())
        manager.registerCommand(Delinfr())
        manager.registerCommand(Delinfrs())
        manager.registerCommand(Mute())
        manager.registerCommand(Tempmute())
        manager.registerCommand(Unmute())
        manager.registerCommand(Lock())
        manager.registerCommand(Unlock())
        manager.registerCommand(Slow())
        manager.registerCommand(Modinf())

        //Dev
        manager.registerCommand(Eval())
        manager.registerCommand(Config())

        //Config
        manager.registerCommand(Prefix())

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