package managers

import commands.slash.giveaway.*
import commands.slash.config.*
class SlashCommandInitializer(manager: SlashCommandManager) {

    init {

        manager.registerCommand(Sorteo())
        manager.registerCommand(Importar())

    }
}