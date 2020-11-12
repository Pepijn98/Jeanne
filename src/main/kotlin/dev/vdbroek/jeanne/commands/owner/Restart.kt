package dev.vdbroek.jeanne.commands.owner

import dev.vdbroek.jeanne.Jeanne
import dev.vdbroek.jeanne.commands.Command
import dev.vdbroek.jeanne.core.Utils
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

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