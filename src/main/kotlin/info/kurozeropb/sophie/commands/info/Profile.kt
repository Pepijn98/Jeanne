package info.kurozeropb.sophie.commands.info

import info.kurozeropb.sophie.Sophie
import info.kurozeropb.sophie.User
import info.kurozeropb.sophie.commands.Command
import info.kurozeropb.sophie.managers.DatabaseManager
import info.kurozeropb.sophie.utils.Utils
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import com.github.kittinunf.fuel.core.HttpException
import okhttp3.Headers
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.set

class Profile : Command(
        name = "profile",
        category = "info",
        description = "View your profile card",
        usage = "[\"large\"|\"small\"|<\"update\"] [\"bg/background\"|\"about\"] [new_value: string]>",
        aliases = listOf("me", "level", "card"),
        subCommands = listOf("update", "large", "small"),
        cooldown = 30,
        botPermissions = listOf(Permission.MESSAGE_WRITE, Permission.MESSAGE_ATTACH_FILES)
) {

    override suspend fun execute(args: List<String>, e: MessageReceivedEvent) {
        Utils.catchAll("Exception occured in profile command", e.channel) {
            var user = DatabaseManager.users.findOne(User::id eq e.author.id)

            if (args.isNotEmpty() && args[0] == "update") {
                if (args.size <= 2)
                    return e.reply("Please specify what to update and the value to update.\nFor more help about this command please visit https://sophiebot.info/settings")

                when (args[1]) {
                    "bg", "background" -> {
                        val regex = """^(http)?s?:?(//[^"']*\.(?:png|jpg|jpeg|gif|svg))$""".toRegex()

                        if (!args[2].matches(regex))
                            return e.reply("I don't think that's a valid image url, if it is please talk to my owner about it")

                        if (user == null) {
                            DatabaseManager.users.insertOne(User(e.author.id, background = args[2]))
                            return e.reply("Successfully updated your profile background")
                        }

                        DatabaseManager.users.updateOne(User::id eq e.author.id, set(User::background, args[2]))
                        e.reply("Successfully updated your profile background")
                    }
                    "about" -> {
                        val message = args.subList(2, args.size).joinToString(" ")

                        if (user == null) {
                            DatabaseManager.users.insertOne(User(e.author.id, about = message))
                            return e.reply("Successfully updated your about description")
                        }

                        DatabaseManager.users.updateOne(User::id eq e.author.id, set(User::about, message))
                        e.reply("Successfully updated your about description")
                    }
                }
                return
            }

            if (user == null) {
                DatabaseManager.users.insertOne(User(e.author.id))
                user = User(e.author.id)
            }

            val size = if (args.isNotEmpty()) args[0].toLowerCase() else "small"
            if (size != "large" && size != "small")
                return e.reply("Invalid size, only sizes 'large' and 'small' are allowed.")

            val json = """
                {
                    "username": "${e.author.name}",
                    "avatar": "${e.author.effectiveAvatarUrl}",
                    "about": "${user.about}",
                    "level": ${user.level},
                    "points": ${user.points},
                    "background": "${user.background}",
                    "size": "$size"
                }
            """.trimIndent()

            val mediaType = MediaType.parse("application/json; charset=utf-8")
            val requestBody = RequestBody.create(mediaType, json)
            val apiUrl = Sophie.config.api.url + "/profile"
            val headers = Sophie.defaultHeaders
            headers.putAll(mapOf("authorization" to Sophie.config.api.token))
            val request = Request.Builder()
                    .url(apiUrl)
                    .headers(Headers.of(headers))
                    .post(requestBody)
                    .build()

            val response = Sophie.httpClient.newCall(request).execute()

            if (response.isSuccessful) {
                val file = response.body()?.byteStream()
                if (file != null)
                    e.reply(file, "${e.author.name.toLowerCase().replace(" ", "_")}-profile-card-$size.png")
                else
                    e.reply("Failed to request profile card please try again later.")
            } else {
                val code = response.code()
                val message = response.message()
                response.close()
                throw HttpException(code, message)
            }
            response.close()
        }
    }
}
