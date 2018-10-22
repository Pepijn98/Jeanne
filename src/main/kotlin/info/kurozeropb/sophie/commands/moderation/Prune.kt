package info.kurozeropb.sophie.commands.moderation

import info.kurozeropb.sophie.commands.Command
import info.kurozeropb.sophie.utils.Utils
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import java.lang.NumberFormatException
import java.time.OffsetDateTime

class Prune : Command(
        name = "prune",
        aliases = listOf("purge", "clear"),
        category = "moderation",
        allowPrivate = false,
        description = "Prunes the given number of messages, defaults to 50 messages",
        userPermissions = listOf(Permission.MESSAGE_MANAGE),
        botPermissions = listOf(Permission.MESSAGE_WRITE, Permission.MESSAGE_MANAGE)
) {

    override suspend fun execute(args: List<String>, e: MessageReceivedEvent) {
        Utils.catchAll("Exception occured in prune command", e.channel) {
            val history = e.textChannel.history
            var messagesToDelete = 51
            if (args.isNotEmpty())
                messagesToDelete = try {
                    args.joinToString("").toInt() + 1
                } catch (exception: NumberFormatException) {
                    return e.reply("Argument needs to be a number")
                }

            if (messagesToDelete <= 1)
                return e.reply("You need to delete 1 or more messages to use this command.")

            val time = OffsetDateTime.now().minusWeeks(2)
            history.retrievePast(messagesToDelete).queue {
                val messages = it.filter { msg -> msg.creationTime.isAfter(time) }
                when {
                    messages.size >= 2 -> e.message.textChannel.deleteMessages(messages).queue()
                    messages.size == 1 -> messages.first().delete().queue()
                    else -> e.reply("No messages were found (that are younger than 2 weeks).")
                }
            }
        }
    }
}
