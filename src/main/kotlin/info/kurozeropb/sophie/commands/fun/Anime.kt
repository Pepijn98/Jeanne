package info.kurozeropb.sophie.commands.`fun`

import info.kurozeropb.sophie.Sophie
import info.kurozeropb.sophie.commands.Command
import info.kurozeropb.sophie.utils.Utils
import info.kurozeropb.sophie.utils.Kitsu
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import okhttp3.*
import java.io.IOException
import net.dv8tion.jda.core.EmbedBuilder

class Anime : Command(
        name = "anime",
        category = "fun",
        description = "Find information about an anime",
        usage = "<anime_name: string>",
        cooldown = 10,
        botPermissions = listOf(Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS)
) {

    override suspend fun execute(args: List<String>, e: MessageReceivedEvent) {
        Utils.catchAll("Exception occured in anime command", e.channel) {
            val name = args.joinToString(" ")
            val headers = mutableMapOf(
                    "Content-Type" to "application/vnd.api+json",
                    "Accept" to "application/vnd.api+json"
            )
            headers.putAll(Sophie.defaultHeaders)
            val request = Request.Builder()
                    .headers(Headers.of(headers))
                    .url("${Kitsu.baseUrl}/anime?filter[text]=$name")
                    .build()

            Sophie.httpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, exception: IOException) {
                    throw exception
                }

                override fun onResponse(call: Call, response: Response) {
                    val respstring = response.body()?.string()
                    if (response.isSuccessful && respstring != null) {
                        val anime = Kitsu.Anime.Deserializer().deserialize(respstring)
                        if (anime != null && anime.data.size > 0) {
                            e.reply(EmbedBuilder()
                                    .setTitle(anime.data[0].attributes.titles.en_jp)
                                    .setDescription(anime.data[0].attributes.synopsis)
                                    .setThumbnail(anime.data[0].attributes.posterImage.original)
                                    .addField("Type", anime.data[0].attributes.showType, true)
                                    .addField("Episodes", anime.data[0].attributes.episodeCount.toString(), true)
                                    .addField("Status", anime.data[0].attributes.status, true)
                                    .addField("Rating", anime.data[0].attributes.averageRating, true)
                                    .addField("Rank", "#" + anime.data[0].attributes.ratingRank, true)
                                    .addField("Favorites", anime.data[0].attributes.favoritesCount.toString(), true)
                                    .addField("Start/End", anime.data[0].attributes.startDate + " until " + anime.data[0].attributes.endDate, false)
                            )
                        } else {
                            e.reply("Could not find an anime with the name **$name**")
                        }
                    } else {
                        e.reply("Could not find an anime with the name **$name**")
                    }
                }
            })
        }
    }
}