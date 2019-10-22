package info.kurozeropb.jeanne.commands.`fun`

import info.kurozeropb.jeanne.commands.Command
import info.kurozeropb.jeanne.core.Utils
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.util.*

class EightBall : Command(
        name = "8ball",
        category = Category.FUN,
        description = "Ask the magic 8 ball",
        usage = "<question: string>",
        botPermissions = listOf(Permission.MESSAGE_WRITE)
) {

    private val rand = Random()
    private val answers = listOf(
            "Yes!",
            "No way!",
            "Obviously",
            "Try again next time",
            "Probably not",
            "Probably",
            "Think harder and try again!",
            "Idk u tell me..",
            "Keep on dreaming!",
            "Without a doubt",
            "My sources say no",
            "Very doubtful",
            "It is decidedly so..",
            "My reply is no",
            "Better not tell you now..",
            "Don't count on it..",
            "Cannot predict now.."
    )

    override suspend fun execute(args: List<String>, e: MessageReceivedEvent) {
        Utils.catchAll("Exception occured in 8ball command", e.channel) {
            if (args.isEmpty())
                return e.reply("Insufficient argument count")

            e.reply(answers.random(rand))
        }
    }
}
