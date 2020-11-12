package dev.vdbroek.jeanne.commands.info

import dev.vdbroek.jeanne.commands.Command
import dev.vdbroek.jeanne.core.Utils
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.util.function.Consumer

class Ping : Command(
    name = "ping",
    category = Category.INFO,
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
