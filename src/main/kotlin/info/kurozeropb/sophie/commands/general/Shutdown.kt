package info.kurozeropb.sophie.commands.general

import info.kurozeropb.sophie.ExitStatus
import info.kurozeropb.sophie.Sophie
import info.kurozeropb.sophie.commands.Command
import info.kurozeropb.sophie.utils.Utils
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import kotlin.system.exitProcess

class Shutdown : Command(
        name = "shutdown",
        aliases = listOf("stop"),
        category = "general",
        description = "Shutdown the bot",
        isDeveloperOnly = true,
        isHidden = true,
        botPermissions = listOf(Permission.MESSAGE_WRITE)
) {

    override suspend fun execute(args: List<String>, e: MessageReceivedEvent) {
        Utils.catchAll("Exception occured in shutdown command", e.channel) {
            if (args.isNotEmpty()) {
                Sophie.shardManager.shutdown(args[0].toInt())
                Thread.sleep(1000)
                exitProcess(ExitStatus.SHUTDOWN.code)
            } else {
                Sophie.shardManager.shutdown()
                Thread.sleep(1000)
                exitProcess(ExitStatus.SHUTDOWN.code)
            }

        }
    }
}