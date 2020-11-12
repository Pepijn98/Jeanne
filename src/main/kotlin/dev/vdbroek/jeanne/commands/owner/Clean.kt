package dev.vdbroek.jeanne.commands.owner

import dev.vdbroek.jeanne.User
import dev.vdbroek.jeanne.commands.Command
import dev.vdbroek.jeanne.core.Utils
import dev.vdbroek.jeanne.managers.DatabaseManager
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.litote.kmongo.eq
import org.litote.kmongo.find
import kotlin.system.measureTimeMillis

class Clean : Command(
    name = "clean",
    category = Category.OWNER,
    description = "Clean database from deleted users",
    isDeveloperOnly = true,
    isHidden = true,
    botPermissions = listOf(Permission.MESSAGE_WRITE)
) {

    override suspend fun execute(args: List<String>, e: MessageReceivedEvent) {
        Utils.catchAll("Exception occured in clean command", e.channel) {
            e.reply("[Task started]: Cleaning database... ")
            val millis = measureTimeMillis {
                val users = DatabaseManager.users.find("{}")
                // val sorted = users.sortedBy { (_, _, points) -> points }.asReversed()
                // val subList = sorted.subList(0, 50)

                // for (user in subList) {
                for (user in users) {
                    val foundUser = e.jda.getUserById(user.id)
                    if (foundUser == null) {
                        DatabaseManager.users.deleteOne(User::id eq user.id)
                    }
                }
            }
            e.reply("[Task ended]: Database cleaned! (${millis}ms)")
        }
    }
}