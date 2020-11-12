package dev.vdbroek.jeanne.commands.info

import dev.vdbroek.jeanne.Jeanne
import dev.vdbroek.jeanne.User
import dev.vdbroek.jeanne.commands.Command
import dev.vdbroek.jeanne.core.HttpException
import dev.vdbroek.jeanne.core.Utils
import dev.vdbroek.jeanne.managers.DatabaseManager
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import okhttp3.*
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.setValue
import java.io.IOException

class Profile : Command(
    name = "profile",
    category = Category.INFO,
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
                    return e.reply("Please specify what to update and the value to update.\nFor more help about this command please visit https://jeannebot.info/settings")

                when (args[1]) {
                    "bg", "background" -> {
                        val regex = """^(http)?s?:?(//[^"']*\.(?:png|jpg|jpeg|gif|svg))$""".toRegex()

                        if (!args[2].matches(regex))
                            return e.reply("I don't think that's a valid image url, if it is please talk to my owner about it")

                        if (user == null) {
                            DatabaseManager.users.insertOne(User(e.author.id, background = args[2]))
                            return e.reply("Successfully updated your profile background")
                        }

                        DatabaseManager.users.updateOne(User::id eq e.author.id, setValue(User::background, args[2]))
                        e.reply("Successfully updated your profile background")
                    }
                    "about" -> {
                        val message = args.subList(2, args.size).joinToString(" ")

                        if (user == null) {
                            DatabaseManager.users.insertOne(User(e.author.id, about = message))
                            return e.reply("Successfully updated your about description")
                        }

                        DatabaseManager.users.updateOne(User::id eq e.author.id, setValue(User::about, message))
                        e.reply("Successfully updated your about description")
                    }
                }
            } else {
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
                        "avatar": "${e.author.effectiveAvatarUrl}?size=2048",
                        "about": "${user.about}",
                        "level": ${user.level},
                        "points": ${user.points},
                        "background": "${user.background}",
                        "size": "$size"
                    }
                    """.trimIndent()

                val mediaType = MediaType.parse("application/json; charset=utf-8")
                val requestBody = RequestBody.create(mediaType, json)
                val apiUrl = Jeanne.config.apiUrl + "/jeanne/profile"
                val headers = mutableMapOf("authorization" to Jeanne.config.tokens.kurozero)
                headers.putAll(Jeanne.defaultHeaders)
                val request = Request.Builder()
                    .url(apiUrl)
                    .headers(Headers.of(headers))
                    .post(requestBody)
                    .build()

                Jeanne.httpClient.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, exception: IOException) {
                        Utils.catchAll("Exception occured in profile command", e.channel) {
                            throw exception
                        }
                    }

                    override fun onResponse(call: Call, response: Response) {
                        Utils.catchAll("Exception occured in profile command", e.channel) {
                            val byteStream = response.body()?.byteStream()
                            val message = response.message()
                            val code = response.code()

                            if (response.isSuccessful) {
                                if (byteStream == null)
                                    return e.reply("Failed to request profile card please try again later.")

                                e.reply(byteStream, "${e.author.name.toLowerCase().replace(" ", "_")}-profile-card-$size.png")
                            } else {
                                throw HttpException(code, message)
                            }
                        }
                    }
                })
            }
        }
    }
}
