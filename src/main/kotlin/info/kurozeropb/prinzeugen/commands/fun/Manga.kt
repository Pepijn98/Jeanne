package info.kurozeropb.prinzeugen.commands.`fun`

import info.kurozeropb.prinzeugen.Prinz
import info.kurozeropb.prinzeugen.commands.Command
import info.kurozeropb.prinzeugen.utils.Utils
import info.kurozeropb.prinzeugen.utils.Kitsu
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import okhttp3.*
import java.io.IOException
import net.dv8tion.jda.core.EmbedBuilder

class Manga : Command(
        name = "manga",
        category = "fun",
        description = "Find information about a manga",
        botPermissions = listOf(
                Permission.MESSAGE_WRITE,
                Permission.MESSAGE_EMBED_LINKS
        )
) {

    override suspend fun execute(args: List<String>, e: MessageReceivedEvent) {
        Utils.catchAll("Exception occured in manga command", e.channel) {
            val name = args.joinToString(" ")
            val headers = Prinz.defaultHeaders
            headers.putAll(mapOf(
                    "Content-Type" to "application/vnd.api+json",
                    "Accept" to "application/vnd.api+json"
            ))
            val request = Request.Builder()
                    .headers(Headers.of(headers))
                    .url("${Kitsu.baseUrl}/manga?filter[text]=$name")
                    .build()

            Prinz.httpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, exception: IOException) {
                    throw IOException(exception)
                }

                override fun onResponse(call: Call, response: Response) {
                    val respstring = response.body()?.string()
                    if (response.isSuccessful && respstring != null) {
                        val manga = Kitsu.Manga.Deserializer().deserialize(respstring)
                        if (manga != null && manga.data.size > 0) {
                            e.reply(EmbedBuilder()
                                    .setTitle(manga.data[0].attributes.titles.en_jp)
                                    .setDescription(manga.data[0].attributes.synopsis)
                                    .setThumbnail(manga.data[0].attributes.posterImage.original)
                                    .addField("Type", manga.data[0].attributes.mangaType, true)
                                    .addField("Chapters/Volumes", "${manga.data[0].attributes.chapterCount}/${manga.data[0].attributes.volumeCount}", true)
                                    .addField("Status", manga.data[0].attributes.status, true)
                                    .addField("Rating", manga.data[0].attributes.averageRating, true)
                                    .addField("Rank", "#" + manga.data[0].attributes.ratingRank, true)
                                    .addField("Favorites", manga.data[0].attributes.favoritesCount.toString(), true)
                                    .addField("Start/End", manga.data[0].attributes.startDate + " until " + manga.data[0].attributes.endDate, false)
                            )
                        } else {
                            e.reply("Could not find a manga with the name **$name**")
                        }
                    }
                }
            })
        }
    }
}