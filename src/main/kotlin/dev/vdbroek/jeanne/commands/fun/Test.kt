package dev.vdbroek.jeanne.commands.`fun`

import dev.vdbroek.jeanne.commands.Command
import dev.vdbroek.jeanne.core.HttpException
import dev.vdbroek.jeanne.core.Utils
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

class Test : Command(
    name = "test",
    category = Category.FUN,
    description = "Testing command",
    allowPrivate = false,
    isHidden = true,
    isDeveloperOnly = true
) {

    override suspend fun execute(args: List<String>, e: MessageReceivedEvent) {
        Utils.catchAll("Exception occured in test command", e.channel) {
            throw HttpException(404, "Not Found")
        }
    }
}