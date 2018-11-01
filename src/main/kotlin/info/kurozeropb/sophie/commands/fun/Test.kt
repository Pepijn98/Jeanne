package info.kurozeropb.sophie.commands.`fun`

import info.kurozeropb.sophie.commands.Command
import info.kurozeropb.sophie.utils.Utils
import net.dv8tion.jda.core.events.message.MessageReceivedEvent

class Test : Command(
        name = "test",
        category = Category.FUN,
        description = "Testing command",
        allowPrivate = false,
        isDeveloperOnly = true
) {

    override suspend fun execute(args: List<String>, e: MessageReceivedEvent) {
        Utils.catchAll("Exception occured in test command", e.channel) {
            Utils.sendGuildCountAll(7300, 7)
        }
    }
}