package info.kurozeropb.sophie.commands.`fun`

import com.beust.klaxon.Klaxon
import info.kurozeropb.sophie.Nekos
import info.kurozeropb.sophie.Sophie
import info.kurozeropb.sophie.commands.Command
import info.kurozeropb.sophie.core.HttpException
import info.kurozeropb.sophie.core.Utils
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import okhttp3.*
import java.io.IOException

class Catgirl : Command(
        name = "catgirl",
        aliases = listOf("catgirls", "neko", "nekos"),
        category = Category.FUN,
        description = "Sends a cute catgirl from https://nekos.moe",
        usage = "[\"nsfw\"]",
        cooldown = 10,
        botPermissions = listOf(Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS)
) {

    override suspend fun execute(args: List<String>, e: MessageReceivedEvent) {
        val url = if (args.isNotEmpty() && args[0] == "nsfw") {
            if ((e.message.channel as TextChannel).isNSFW.not())
                return e.reply("You can only use the NSFW option in NSFW channels")
            "https://nekos.moe/api/v1/random/image?nsfw=true"
        } else
            "https://nekos.moe/api/v1/random/image?nsfw=false"

        val headers = mutableMapOf("Accept" to "application/json")
        headers.putAll(Sophie.defaultHeaders)
        val request = Request.Builder()
                .headers(Headers.of(headers))
                .url(url)
                .build()

        Sophie.httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, exception: IOException) {
                Utils.catchAll("Exception occured in catgirl command", e.channel) {
                    throw exception
                }
            }

            override fun onResponse(call: Call, response: Response) {
                Utils.catchAll("Exception occured in catgirl command", e.channel) {
                    val respstring = response.body()?.string()
                    val message = response.message()
                    val code = response.code()
                    response.close()

                    if (response.isSuccessful) {
                        if (respstring.isNullOrBlank())
                            return e.reply("Could not find a catgirl, please try again later")

                        val neko = Klaxon().parse<Nekos>(respstring)
                        if (neko != null) {
                            e.reply(EmbedBuilder()
                                    .setDescription("[Full image](https://nekos.moe/image/${neko.images[0].id})")
                                    .setImage("https://nekos.moe/image/${neko.images[0].id}")
                                    .setFooter("All images are from https://nekos.moe", null))
                        } else {
                            e.reply("Could not find a catgirl, please try again later")
                        }
                    } else {
                        throw HttpException(code, message)
                    }
                }
            }
        })
    }
}
