package info.kurozeropb.prinzeugen.commands.general

import info.kurozeropb.prinzeugen.Prinz
import info.kurozeropb.prinzeugen.commands.Command
import info.kurozeropb.prinzeugen.utils.Utils
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.MessageReceivedEvent

class Restart : Command(
        name = "restart",
        category = "general",
        description = "Restarts the bot",
        isDeveloperOnly = true,
        isHidden = true,
        botPermissions = listOf(Permission.MESSAGE_WRITE)
) {

    override suspend fun execute(args: List<String>, e: MessageReceivedEvent) {
        Utils.catchAll("Exception occured in restart command", e.channel) {
            if (args.isNotEmpty())
                Prinz.shardManager.restart(args[0].toInt())
            else
                Prinz.shardManager.restart()
        }
    }
}