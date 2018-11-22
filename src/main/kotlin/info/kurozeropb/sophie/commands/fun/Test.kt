package info.kurozeropb.sophie.commands.`fun`

import info.kurozeropb.sophie.commands.Command
import info.kurozeropb.sophie.core.HttpException
import info.kurozeropb.sophie.core.Utils
import net.dv8tion.jda.core.events.message.MessageReceivedEvent

class Test : Command(
        name = "test",
        category = Category.FUN,
        description = "Testing command",
        allowPrivate = false,
        isHidden = true,
        isDeveloperOnly = true
) {

    override suspend fun execute(args: List<String>, e: MessageReceivedEvent) {
        Utils.catchAll("Exception occured in test command", e.channel) {
            throw HttpException(404, "Not Found")
        }
    }
}