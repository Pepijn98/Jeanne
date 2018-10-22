package info.kurozeropb.sophie.commands.nsfw

import info.kurozeropb.sophie.ONsfwData
import info.kurozeropb.sophie.Sophie
import info.kurozeropb.sophie.commands.Command
import info.kurozeropb.sophie.utils.Utils
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import okhttp3.*
import java.io.IOException

class Boobs : Command(
        name = "boobs",
        aliases = listOf("tits"),
        category = "nsfw",
        description = "Sends a random boobs pic",
        cooldown = 10,
        botPermissions = listOf(Permission.MESSAGE_WRITE, Permission.MESSAGE_ATTACH_FILES)
) {

    override suspend fun execute(args: List<String>, e: MessageReceivedEvent) {
        Utils.catchAll("Exception occured in boobs command", e.channel) {
            if (e.textChannel.isNSFW.not()) {
                e.reply("This command can only be used in NSFW channels")
                return
            }

            // TODO: Donators check

            val headers = Sophie.defaultHeaders
            headers.putAll(mapOf("Accept" to "application/json"))
            val request = Request.Builder()
                    .headers(Headers.of(headers))
                    .url("http://api.oboobs.ru/boobs/0/1/random")
                    .build()

            Sophie.httpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, exception: IOException) {
                    throw IOException(exception)
                }

                override fun onResponse(call: Call, response: Response) {
                    val respstring = response.body()?.string()
                    if (response.isSuccessful && respstring != null) {
                        val boobs = ONsfwData.Deserializer().deserialize(respstring)
                        if (boobs != null && boobs.isNotEmpty()) {
                            headers.replace("Accept", "application/json", "image/*")
                            val newRequest = Request.Builder()
                                    .headers(Headers.of(headers))
                                    .url("http://media.oboobs.ru/${boobs[0].preview.replace("boobs_preview", "boobs")}")
                                    .build()

                            val resp = Sophie.httpClient.newCall(newRequest).execute()
                            if (resp.isSuccessful) {
                                val body = resp.body()
                                if (body != null)
                                    e.channel.sendFile(body.byteStream(), "obutt-${boobs[0].id}.${body.contentType().toString().replace("image/", "")}").queue()
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
