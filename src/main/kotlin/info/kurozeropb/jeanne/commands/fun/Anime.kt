package info.kurozeropb.jeanne.commands.`fun`

import info.kurozeropb.jeanne.Jeanne
import info.kurozeropb.jeanne.commands.Command
import info.kurozeropb.jeanne.core.Utils
import info.kurozeropb.jeanne.core.Kitsu
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import okhttp3.*
import java.io.IOException
import net.dv8tion.jda.core.EmbedBuilder
import com.beust.klaxon.Klaxon
import info.kurozeropb.jeanne.core.HttpException

class Anime : Command(
        name = "anime",
        category = Category.FUN,
        description = "Find information about an anime",
        usage = "<anime_name: string>",
        cooldown = 10,
        botPermissions = listOf(Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS)
) {

    override suspend fun execute(args: List<String>, e: MessageReceivedEvent) {
//        e.reply("⚠ This command is temporarily disabled ⚠\n\n" +
//                "This command uses Kitsu's api but Kitsu is having trouble paying for their host\n" +
//                "To help them out and to get this command back donate at: https://oof.kitsu.io/")
        val name = args.joinToString("-").toLowerCase()
        val headers = mutableMapOf(
                "Content-Type" to "application/vnd.api+json",
                "Accept" to "application/vnd.api+json"
        )
        headers.putAll(Jeanne.defaultHeaders)
        val request = Request.Builder()
                .headers(Headers.of(headers))
                .url("${Kitsu.baseUrl}/anime?filter[text]=$name")
                .build()

        Jeanne.httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, exception: IOException) {
                Utils.catchAll("Exception occured in anime command", e.channel) {
                    throw exception
                }
            }

            override fun onResponse(call: Call, response: Response) {
                Utils.catchAll("Exception occured in anime command", e.channel) {
                    val respstring = response.body()?.string()
                    val message = response.message()
                    val code = response.code()
                    response.close()

                    if (response.isSuccessful) {
                        if (respstring.isNullOrBlank())
                            return e.reply("Could not find an anime with the name **$name**")

                        val anime: Kitsu.Anime?
                        try {
                            anime = Klaxon().parse<Kitsu.Anime>(respstring)
                        } catch (exception: Exception) {
                            return e.reply("Something went wrong while parsing the anime data.")
                        }

                        if (anime != null && anime.data.size > 0) {
                            val releaseDate =
                                    if (anime.data[0].attributes.startDate.isNullOrEmpty())
                                        "TBA"
                                    else
                                        "${anime.data[0].attributes.startDate} until ${anime.data[0].attributes.endDate ?: "Unkown"}"

                            val title = anime.data[0].attributes.titles.en_jp ?: anime.data[0].attributes.titles.en ?: anime.data[0].attributes.titles.ja_jp ?: "-"
                            e.reply(EmbedBuilder()
                                    .setTitle(title)
                                    .setDescription(anime.data[0].attributes.synopsis)
                                    .setThumbnail(anime.data[0].attributes.posterImage?.original)
                                    .addField("Type", anime.data[0].attributes.showType, true)
                                    .addField("Episodes", anime.data[0].attributes.episodeCount?.toString() ?: "Unkown", true)
                                    .addField("Status", anime.data[0].attributes.status, true)
                                    .addField("Rating", anime.data[0].attributes.averageRating ?: "Unkown", true)
                                    .addField("Rank", if (anime.data[0].attributes.ratingRank != null) "#${anime.data[0].attributes.ratingRank}" else "Unkown", true)
                                    .addField("Favorites", anime.data[0].attributes.favoritesCount.toString(), true)
                                    .addField("Start/End", releaseDate, false))
                        } else {
                            e.reply("Could not find an anime with the name **$name**")
                        }
                    } else {
                        throw HttpException(code, message)
                    }
                }
            }
        })
    }
}