package info.kurozeropb.jeanne.commands.owner

import info.kurozeropb.jeanne.Jeanne
import info.kurozeropb.jeanne.commands.Command
import info.kurozeropb.jeanne.core.Utils
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.MessageReceivedEvent

class Restart : Command(
        name = "restart",
        category = Category.OWNER,
        description = "Restarts the bot",
        isDeveloperOnly = true,
        isHidden = true,
        botPermissions = listOf(Permission.MESSAGE_WRITE)
) {

    override suspend fun execute(args: List<String>, e: MessageReceivedEvent) {
        Utils.catchAll("Exception occured in restart command", e.channel) {
            if (args.isNotEmpty())
                Jeanne.shardManager.restart(args[0].toInt())
            else
                Jeanne.shardManager.restart()
        }
    }
}