package info.kurozeropb.prinzeugen.commands.general

import info.kurozeropb.prinzeugen.commands.Command
import info.kurozeropb.prinzeugen.utils.Utils
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import java.util.function.Consumer

class Ping : Command(
        name = "ping",
        category = "general",
        description = "Returns an estimated ping to Discord's servers",
        botPermissions = listOf(Permission.MESSAGE_WRITE)
) {

    override suspend fun execute(args: List<String>, e: MessageReceivedEvent) {
        Utils.catchAll("Exception occured in ping command", e.channel) {
            var time = System.currentTimeMillis()
            e.reply("Pinging...", Consumer {
                time = (System.currentTimeMillis() - time) / 2
                Utils.edit(it, "**Ping:** ${time}ms")
            })
        }
    }
}
