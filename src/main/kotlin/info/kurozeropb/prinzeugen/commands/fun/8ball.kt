package info.kurozeropb.prinzeugen.commands.`fun`

import info.kurozeropb.prinzeugen.commands.Command
import info.kurozeropb.prinzeugen.utils.Utils
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import java.util.*

class `8ball` : Command(
        name = "8ball",
        category = "fun",
        description = "Ask the magic 8 ball",
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
        Utils.catchAll("Exception occured in ping command", e.channel) {
            if (args.isEmpty()) {
                e.reply("Insufficient argument count")
                return
            }

            e.reply(answers.random(rand))
        }
    }
}
