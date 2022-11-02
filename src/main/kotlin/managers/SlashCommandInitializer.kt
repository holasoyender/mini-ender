package managers

import commands.slash.giveaway.*
class SlashCommandInitializer(manager: SlashCommandManager) {

    init {

        manager.registerCommand(Sorteo())

    }
}