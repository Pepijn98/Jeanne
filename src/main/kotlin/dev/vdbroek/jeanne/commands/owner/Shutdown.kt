package dev.vdbroek.jeanne.commands.owner

import dev.vdbroek.jeanne.ExitStatus
import dev.vdbroek.jeanne.Jeanne
import dev.vdbroek.jeanne.commands.Command
import dev.vdbroek.jeanne.core.Utils
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import kotlin.system.exitProcess

class Shutdown : Command(
    name = "shutdown",
    aliases = listOf("stop"),
    category = Category.OWNER,
    description = "Shutdown the bot",
    isDeveloperOnly = true,
    isHidden = true,
    botPermissions = listOf(Permission.MESSAGE_WRITE)
) {

    override suspend fun execute(args: List<String>, e: MessageReceivedEvent) {
        Utils.catchAll("Exception occured in shutdown command", e.channel) {
            if (args.isNotEmpty()) {
                Jeanne.shardManager.shutdown(args[0].toInt())
                Thread.sleep(1000)
                exitProcess(ExitStatus.SHUTDOWN.code)
            } else {
                Jeanne.shardManager.shutdown()
                Thread.sleep(1000)
                exitProcess(ExitStatus.SHUTDOWN.code)
            }

        }
    }
}