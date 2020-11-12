package dev.vdbroek.jeanne.commands.`fun`

import com.beust.klaxon.Klaxon
import dev.vdbroek.jeanne.CatData
import dev.vdbroek.jeanne.Jeanne
import dev.vdbroek.jeanne.commands.Command
import dev.vdbroek.jeanne.core.HttpException
import dev.vdbroek.jeanne.core.Utils
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import okhttp3.*
import java.io.IOException

class Cat : Command(
    name = "cat",
    category = Category.FUN,
    description = "Sends random cat image from http://random.cat",
    cooldown = 10,
    botPermissions = listOf(Permission.MESSAGE_WRITE, Permission.MESSAGE_ATTACH_FILES)
) {

    override suspend fun execute(args: List<String>, e: MessageReceivedEvent) {
        val headers = mutableMapOf("Accept" to "application/json")
        headers.putAll(Jeanne.defaultHeaders)
        val request = Request.Builder()
            .headers(Headers.of(headers))
            .url("https://aws.random.cat/meow")
            .build()

        Jeanne.httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, exception: IOException) {
                Utils.catchAll("Exception occured in cat command", e.channel) {
                    throw exception
                }
            }

            override fun onResponse(call: Call, response: Response) {
                Utils.catchAll("Exception occured in cat command", e.channel) {
                    val message = response.message()
                    val code = response.code()
                    val respstring = response.body()?.string()
                    response.close()

                    if (response.isSuccessful) {
                        if (respstring.isNullOrBlank())
                            return e.reply("Could not find a cat picture, please try again later")

                        val cat = Klaxon().parse<CatData>(respstring)
                        if (cat != null && cat.file.isNotEmpty()) {
                            headers.replace("Accept", "application/json", "image/*")
                            val newRequest = Request.Builder()
                                .headers(Headers.of(headers))
                                .url(cat.file)
                                .build()

                            val newResponse = Jeanne.httpClient.newCall(newRequest).execute()
                            val byteStream = newResponse.body()?.byteStream()
                            val contentType = newResponse.body()?.contentType()
                            val newMessage = newResponse.message()
                            val newCode = newResponse.code()

                            if (newResponse.isSuccessful) {
                                if (byteStream != null)
                                    e.reply(byteStream, "random-cat.${contentType.toString().replace("image/", "")}")
                                else
                                    e.reply("Could not find a cat picture, please try again later")
                                response.close()
                            } else {
                                response.close()
                                throw HttpException(newCode, newMessage)
                            }
                        } else {
                            e.reply("Could not find a cat picture, please try again later")
                        }
                    } else {
                        throw HttpException(code, message)
                    }
                }
            }
        })
    }
}
