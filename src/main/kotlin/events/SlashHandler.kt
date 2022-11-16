package events

import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import slashCommandManager

class SlashHandler: ListenerAdapter() {

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        val user: User = event.user
        if (user.isBot) return
        slashCommandManager!!.run(event)
    }

}