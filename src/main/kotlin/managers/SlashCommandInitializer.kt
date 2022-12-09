package managers

import commands.slash.giveaway.*
import commands.slash.config.*
import commands.slash.ocio.*
class SlashCommandInitializer(manager: SlashCommandManager) {

    init {

        manager.registerCommand(Sorteo())
        manager.registerCommand(Importar())
        manager.registerCommand(Exportar())
        manager.registerCommand(Reiniciar())
        manager.registerCommand(Regalo())

    }
}