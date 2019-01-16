package info.kurozeropb.sophie.commands.nsfw

import com.beust.klaxon.Klaxon
import info.kurozeropb.sophie.ONsfwData
import info.kurozeropb.sophie.Sophie
import info.kurozeropb.sophie.commands.Command
import info.kurozeropb.sophie.core.HttpException
import info.kurozeropb.sophie.core.Utils
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import okhttp3.*
import java.io.IOException

class Boobs : Command(
        name = "boobs",
        aliases = listOf("tits"),
        category = Category.NSFW,
        nsfw = true,
        description = "Sends a random boobs pic",
        cooldown = 10,
        isDonatorsOnly = true,
        botPermissions = listOf(Permission.MESSAGE_WRITE, Permission.MESSAGE_ATTACH_FILES)
) {

    override suspend fun execute(args: List<String>, e: MessageReceivedEvent) {
        if (e.textChannel.isNSFW.not()) {
            e.reply("This command can only be used in NSFW channels")
            return
        }

        val headers = mutableMapOf("Accept" to "application/json")
        headers.putAll(Sophie.defaultHeaders)
        val request = Request.Builder()
                .headers(Headers.of(headers))
                .url("http://api.oboobs.ru/boobs/0/1/random")
                .build()

        Sophie.httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, exception: IOException) {
                Utils.catchAll("Exception occured in boobs command", e.channel) {
                    throw exception
                }
            }

            override fun onResponse(call: Call, response: Response) {
                Utils.catchAll("Exception occured in boobs command", e.channel) {
                    val respstring = response.body()?.string()
                    val message = response.message()
                    val code = response.code()
                    response.close()

                    if (response.isSuccessful) {
                        if (respstring.isNullOrBlank())
                            return e.reply("Could not find a boob picture, please try again later")

                        val boobs = Klaxon().parseArray<ONsfwData>(respstring)
                        if (boobs != null && boobs.isNotEmpty()) {
                            headers.replace("Accept", "application/json", "image/*")
                            val newRequest = Request.Builder()
                                    .headers(Headers.of(headers))
                                    .url("http://media.oboobs.ru/${boobs[0].preview.replace("boobs_preview", "boobs")}")
                                    .build()

                            val newResponse = Sophie.httpClient.newCall(newRequest).execute()
                            val byteStream = newResponse.body()?.byteStream()
                            val contentType = newResponse.body()?.contentType()
                            val newMessage = newResponse.message()
                            val newCode = newResponse.code()

                            if (newResponse.isSuccessful) {
                                if (byteStream != null)
                                    e.reply(byteStream, "oboobs-${boobs[0].id}.${contentType.toString().replace("image/", "")}")
                                else
                                    e.reply("Could not find a boob picture, please try again later")
                            } else {
                                throw HttpException(newCode, newMessage)
                            }
                        } else {
                            e.reply("Could not find a boob picture, please try again later")
                        }
                    } else {
                        throw HttpException(code, message)
                    }
                }
            }
        })
    }
}
