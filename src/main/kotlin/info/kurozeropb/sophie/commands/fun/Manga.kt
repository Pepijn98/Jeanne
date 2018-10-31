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

class Manga : Command(
        name = "manga",
        category = "fun",
        description = "Find information about a manga",
        usage = "<manga_name: string>",
        cooldown = 10,
        botPermissions = listOf(Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS)
) {

    override suspend fun execute(args: List<String>, e: MessageReceivedEvent) {
        Utils.catchAll("Exception occured in manga command", e.channel) {
            val name = args.joinToString("-").toLowerCase()
            val headers = mutableMapOf(
                    "Content-Type" to "application/vnd.api+json",
                    "Accept" to "application/vnd.api+json"
            )
            headers.putAll(Sophie.defaultHeaders)
            val request = Request.Builder()
                    .headers(Headers.of(headers))
                    .url("${Kitsu.baseUrl}/manga?filter[text]=$name")
                    .build()

            Sophie.httpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, exception: IOException) {
                    throw exception
                }

                override fun onResponse(call: Call, response: Response) {
                    val respstring = response.body()?.string()
                    if (response.isSuccessful && respstring != null) {
                        val manga = Kitsu.Manga.Deserializer().deserialize(respstring)
                        if (manga != null && manga.data.size > 0) {
                            val releaseDate =
                                    if (manga.data[0].attributes.startDate.isNullOrEmpty())
                                        "TBA"
                                    else
                                        "${manga.data[0].attributes.startDate} until ${manga.data[0].attributes.endDate ?: "TBA"}"

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
                                    .addField("Start/End", releaseDate, false)
                            )
                        } else {
                            e.reply("Could not find a manga with the name **$name**")
                        }
                    } else {
                        e.reply("HTTP Exception ${response.code()} ${response.message()}")
                    }
                }
            })
        }
    }
}