package events

import managers.SlashCommandManager
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class SlashHandler: ListenerAdapter() {

    private val manager: SlashCommandManager = SlashCommandManager()

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        val user: User = event.user
        if (user.isBot) return
        manager.run(event)
    }

}