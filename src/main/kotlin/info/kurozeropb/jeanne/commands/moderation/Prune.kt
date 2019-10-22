package info.kurozeropb.jeanne.commands.moderation

import info.kurozeropb.jeanne.commands.Command
import info.kurozeropb.jeanne.core.Utils
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.lang.NumberFormatException
import java.time.OffsetDateTime

class Prune : Command(
        name = "prune",
        aliases = listOf("purge", "clear"),
        category = Category.MODERATION,
        allowPrivate = false,
        description = "Prunes the given number of messages, defaults to 50 messages",
        usage = "[messages_to_delete: number]",
        cooldown = 2,
        userPermissions = listOf(Permission.MESSAGE_MANAGE),
        botPermissions = listOf(Permission.MESSAGE_WRITE, Permission.MESSAGE_MANAGE)
) {

    override suspend fun execute(args: List<String>, e: MessageReceivedEvent) {
        Utils.catchAll("Exception occured in prune command", e.channel) {
            val history = e.textChannel.history
            var messagesToDelete = 50
            if (args.isNotEmpty())
                messagesToDelete = try {
                    args.joinToString("").toInt()
                } catch (exception: NumberFormatException) {
                    return e.reply("Argument needs to be a number")
                }

            if (messagesToDelete <= 1)
                return e.reply("You need to delete 1 or more messages to use this command.")
            else if (messagesToDelete > 100)
                return e.reply("You cannot delete more than 100 messages at a time.")

            val time = OffsetDateTime.now().minusWeeks(2)
            history.retrievePast(messagesToDelete).queue {
                val messages = it.filter { msg -> msg.timeCreated.isAfter(time) }
                when {
                    messages.size >= 2 -> e.message.textChannel.deleteMessages(messages).queue()
                    messages.size == 1 -> messages.first().delete().queue()
                    else -> e.reply("No messages were found (that are younger than 2 weeks).")
                }
            }
        }
    }
}
