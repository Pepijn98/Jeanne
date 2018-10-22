package info.kurozeropb.sophie.commands.`fun`

import info.kurozeropb.sophie.commands.Command
import info.kurozeropb.sophie.utils.Utils
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.MessageReceivedEvent

class Aesthetic : Command(
        name = "aesthetic",
        aliases = listOf("aes"),
        category = "fun",
        description = "Convert text to aesthetic text",
        botPermissions = listOf(Permission.MESSAGE_WRITE)
) {

    override suspend fun execute(args: List<String>, e: MessageReceivedEvent) {
        Utils.catchAll("Exception occured in aesthetic command", e.channel) {
            if (args.isEmpty())
                return e.reply("Insufficient argument count")

            var message = args.joinToString(" ")
            message = message.replace(Regex("[a-zA-Z0-9!?.'\";:\\]\\[}{)(@#\$%^&*\\-_=+`~><]")) { c -> c.value[0].plus(0xFEE0).toString() }
            message = message.replace(Regex(" "), "　")
            e.reply(message)
        }
    }
}