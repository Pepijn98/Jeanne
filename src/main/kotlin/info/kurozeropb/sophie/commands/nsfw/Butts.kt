package info.kurozeropb.sophie.commands.nsfw

import info.kurozeropb.sophie.ONsfwData
import info.kurozeropb.sophie.Sophie
import info.kurozeropb.sophie.commands.Command
import info.kurozeropb.sophie.utils.Utils
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import okhttp3.*
import java.io.IOException

class Butts : Command(
        name = "butts",
        aliases = listOf("ass", "butt"),
        category = Category.NSFW,
        description = "Sends a random butt pic",
        cooldown = 10,
        botPermissions = listOf(Permission.MESSAGE_WRITE, Permission.MESSAGE_ATTACH_FILES)
) {

    override suspend fun execute(args: List<String>, e: MessageReceivedEvent) {
        Utils.catchAll("Exception occured in butts command", e.channel) {
            if (e.textChannel.isNSFW.not()) {
                e.reply("This command can only be used in NSFW channels")
                return
            }

            // TODO: Donators check

            val headers = mutableMapOf("Accept" to "application/json")
            headers.putAll(Sophie.defaultHeaders)
            val request = Request.Builder()
                    .headers(Headers.of(headers))
                    .url("http://api.obutts.ru/butts/0/1/random")
                    .build()

            Sophie.httpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, exception: IOException) {
                    throw IOException(exception)
                }

                override fun onResponse(call: Call, response: Response) {
                    val respstring = response.body()?.string()
                    if (response.isSuccessful && respstring != null) {
                        val ass = ONsfwData.Deserializer().deserialize(respstring)
                        if (ass != null && ass.isNotEmpty()) {
                            headers.replace("Accept", "application/json", "image/*")
                            val newRequest = Request.Builder()
                                    .headers(Headers.of(headers))
                                    .url("http://media.obutts.ru/${ass[0].preview.replace("butts_preview", "butts")}")
                                    .build()

                            val resp = Sophie.httpClient.newCall(newRequest).execute()
                            if (resp.isSuccessful) {
                                val body = resp.body()
                                if (body != null)
                                    e.reply(body.byteStream(), "obutt-${ass[0].id}.${body.contentType().toString().replace("image/", "")}")
                                else
                                    e.reply("Something went wrong while trying to fetch the image")
                            } else {
                                e.reply("Something went wrong while trying to fetch the image")
                            }
                        } else {
                            e.reply("Something went wrong while deserializing the response string")
                        }
                    } else {
                        e.reply("Something went wrong while trying to fetch a random image")
                    }
                }
            })
        }
    }
}
