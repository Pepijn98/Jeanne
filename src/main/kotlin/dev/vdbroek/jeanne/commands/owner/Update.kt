package dev.vdbroek.jeanne.commands.owner

import dev.vdbroek.jeanne.Guild
import dev.vdbroek.jeanne.User
import dev.vdbroek.jeanne.commands.Command
import dev.vdbroek.jeanne.core.Utils
import dev.vdbroek.jeanne.managers.DatabaseManager
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.setValue

class Update : Command(
    name = "update",
    category = Category.OWNER,
    subCommands = listOf("user", "guild"),
    description = "Update a use or guild in the database",
    isDeveloperOnly = true,
    isHidden = true,
    botPermissions = listOf(Permission.MESSAGE_READ, Permission.MESSAGE_WRITE)
) {

    override suspend fun execute(args: List<String>, e: MessageReceivedEvent) {
        Utils.catchAll("Exception occured in update command", e.channel) {
            when (args.size) {
                0 -> return e.reply("Missing sub command, available sub commands are **user** and **guild**")
                1 -> return e.reply("Missing guild or user id")
                2 -> return e.reply("Missing option, available options are\n**user:** blacklisted, donator and currency\n**guild:** blacklisted")
                3 -> return e.reply("Missing new value to update")
            }

            val subCommand = args[0]
            val id = args[1]
            val option = args[2]
            val value = args[3]

            when (subCommand) {
                "user" -> {
                    val user = Utils.findUser(id, e) ?: return e.reply("Could not update this user")
                    val dbUser = DatabaseManager.users.findOne(User::id eq user.id)
                    if (dbUser == null)
                        DatabaseManager.users.insertOne(User(user.id))

                    when (option) {
                        "blacklisted" -> {
                            val newValue = value == "true"
                            DatabaseManager.users.updateOne(User::id eq user.id, setValue(User::blacklisted, newValue))
                            e.reply("Successfully updated blacklisted to **$newValue** for user **${user.name}**")
                        }
                        "donator" -> {
                            val newValue = value == "true"
                            DatabaseManager.users.updateOne(User::id eq user.id, setValue(User::donator, newValue))
                            e.reply("Successfully updated donator to **$newValue** for user **${user.name}**")
                        }
                        "currency" -> e.reply("Comming soon")
                        else -> e.reply("Not a valid argument, option can only be blacklisted, donator or currency")
                    }
                }
                "guild" -> {
                    val discordGuild = e.jda.getGuildById(id)
                    val guild = DatabaseManager.guilds.findOne(Guild::id eq id)
                    if (guild == null)
                        DatabaseManager.guilds.insertOne(Guild(id))

                    when (option) {
                        "blacklisted" -> {
                            val newValue = value == "true"
                            DatabaseManager.guilds.updateOne(Guild::id eq id, setValue(Guild::blacklisted, newValue))
                            e.reply("Successfully updated blacklisted to **$newValue** for guild **${discordGuild?.name ?: id}**")
                        }
                        else -> e.reply("Not a valid argument, option can only be blacklisted")
                    }
                }
                else -> e.reply("Not a valid sub command, available sub commands are **user** and **guild**")
            }
        }
    }
}