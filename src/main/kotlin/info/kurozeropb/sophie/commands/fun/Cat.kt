package info.kurozeropb.sophie.commands.`fun`

import info.kurozeropb.sophie.CatData
import info.kurozeropb.sophie.Sophie
import info.kurozeropb.sophie.commands.Command
import info.kurozeropb.sophie.utils.Utils
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import okhttp3.*
import java.io.IOException

class Cat : Command(
        name = "cat",
        category = "fun",
        description = "Sends random cat image from http://random.cat",
        cooldown = 10,
        botPermissions = listOf(Permission.MESSAGE_WRITE, Permission.MESSAGE_ATTACH_FILES)
) {

    override suspend fun execute(args: List<String>, e: MessageReceivedEvent) {
        Utils.catchAll("Exception occured in cat command", e.channel) {
            val headers = Sophie.defaultHeaders
            headers.putAll(mapOf("Accept" to "application/json"))
            val request = Request.Builder()
                    .headers(Headers.of(headers))
                    .url("https://aws.random.cat/meow")
                    .build()

            Sophie.httpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, exception: IOException) {
                    throw exception
                }

                override fun onResponse(call: Call, response: Response) {
                    val respstring = response.body()?.string()
                    if (response.isSuccessful && respstring != null) {
                        val cat = CatData.Deserializer().deserialize(respstring)
                        if (cat != null) {
                            headers.replace("Accept", "application/json", "image/*")
                            val newRequest = Request.Builder()
                                    .headers(Headers.of(headers))
                                    .url(cat.file)
                                    .build()

                            val resp = Sophie.httpClient.newCall(newRequest).execute()
                            if (resp.isSuccessful) {
                                val body = resp.body()
                                if (body != null)
                                    e.channel.sendFile(body.byteStream(), "random-cat.${body.contentType().toString().replace("image/", "")}").queue()
                                else {
                                    println("no")
                                    e.reply("Something went wrong while trying to fetch the image")
                                }
                            } else {
                                println(Sophie.defaultHeaders)
                                println(resp.message())
                                println("yes")
                                e.reply("Something went wrong while trying to fetch the image")
                            }
                        } else {
                            e.reply("Something went wrong while deserializing the response string")
                        }
                    } else {
                        e.reply("Something went wrong while trying to fetch a random image")
                    }
                    response.close()
                }
            })
        }
    }
}
