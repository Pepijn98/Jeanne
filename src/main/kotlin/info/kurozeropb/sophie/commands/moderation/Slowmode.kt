package info.kurozeropb.sophie.commands.moderation

import info.kurozeropb.sophie.commands.Command
import info.kurozeropb.sophie.core.Utils
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import java.lang.NumberFormatException

class Slowmode : Command(
        name = "slowmode",
        aliases = listOf("slow"),
        category = Category.MODERATION,
        allowPrivate = false,
        description = "Set slowmode for the current channel",
        usage = "<seconds: number>",
        userPermissions = listOf(Permission.MANAGE_CHANNEL),
        botPermissions = listOf(Permission.MESSAGE_WRITE, Permission.MANAGE_CHANNEL)
) {

    override suspend fun execute(args: List<String>, e: MessageReceivedEvent) {
        Utils.catchAll("Exception occured in slowmode command", e.channel) {
            if (args.isEmpty())
                return e.reply("First argument is required, check help for more info")

            val slowmode = try {
                args.joinToString(" ").toInt()
            } catch (exception: NumberFormatException) {
                return e.reply("Argument needs to be a number")
            }

            if (slowmode in 0..120) {
                val channelManager = e.textChannel.manager
                channelManager.setSlowmode(slowmode).queue()
                if (slowmode == 0)
                    e.reply("Slowmode has been disabled for this channel")
                else
                    e.reply("Slowmode has been set to **$slowmode** seconds for this channel")
            } else
                e.reply("Slowmode per user must be between 0 and 120 seconds")
        }
    }
}
