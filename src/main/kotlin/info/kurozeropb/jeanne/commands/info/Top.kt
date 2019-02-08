package info.kurozeropb.jeanne.commands.info

import info.kurozeropb.jeanne.commands.Command
import info.kurozeropb.jeanne.core.Utils
import info.kurozeropb.jeanne.managers.DatabaseManager
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import org.litote.kmongo.find
import java.lang.Exception

class Top : Command(
        name = "top",
        category = Category.INFO,
        description = "Shows the top users according to level and points",
        example = "top 10",
        usage = "<limit: number>",
        botPermissions = listOf(Permission.MESSAGE_WRITE)
) {

    override suspend fun execute(args: List<String>, e: MessageReceivedEvent) {
        Utils.catchAll("Exception occured in top command", e.channel) {
            val limit = try {
                args[0].toInt()
            } catch (e: Exception) {
                10
            }

            if (limit < 1 || limit > 20)
                return e.reply("The limit can only be between 1 and 20")

            val users = DatabaseManager.users.find("{}")
            val sorted = users.sortedBy { (_, _, points) -> points }.asReversed()
            val subList = sorted.subList(0, limit)

            val message = StringBuilder()
            message.append("```md\n")
            for ((i, user) in subList.withIndex()) {
                val discordUser = Utils.findUser(user.id, e)
                message.append("# ${discordUser?.asTag ?: "Unkown User (id: ${user.id})"}\n")
                message.append("Level:  ${user.level}\n")
                message.append("Points: ${user.points}\n")
                if (i < limit - 1) {
                    message.append("\n")
                } else {
                    message.append("```")
                }
            }

            e.reply(message.toString())
        }
    }
}
