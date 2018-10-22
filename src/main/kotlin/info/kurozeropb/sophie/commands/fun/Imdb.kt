package info.kurozeropb.sophie.commands.`fun`

import info.kurozeropb.sophie.*
import info.kurozeropb.sophie.commands.Command
import info.kurozeropb.sophie.utils.Utils
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import okhttp3.*
import java.io.IOException

class Imdb : Command(
        name = "imdb",
        category = "fun",
        description = "Search for a movie on imdb",
        usage = "<movie|serie: string>",
        cooldown = 20,
        botPermissions = listOf(Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS)
) {

    override suspend fun execute(args: List<String>, e: MessageReceivedEvent) {
        Utils.catchAll("Exception occured in imdb command", e.channel) {
            if (args.isEmpty())
                return e.reply("Which movie/serie do you want to search?")

            val baseUrl = "http://www.omdbapi.com/?apikey=${Sophie.config.api.imdbKey}"
            val headers = Sophie.defaultHeaders
            headers.putAll(mapOf("Accept" to "application/json"))
            val request = Request.Builder()
                    .headers(Headers.of(headers))
                    .url("$baseUrl&t=${args.joinToString(" ")}")
                    .build()

            Sophie.httpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, exception: IOException) {
                    throw exception
                }

                override fun onResponse(call: Call, response: Response) {
                    val respstring = response.body()?.string()
                    if (response.isSuccessful && respstring != null) {
                        val omdb = OmdbTypeTest.Deserializer().deserialize(respstring)
                        if (omdb != null) {
                            if (omdb.Response == "False") {
                                val error = OmdbError.Deserializer().deserialize(respstring)!!
                                return e.reply(error.Error)
                            }

                            val embed = EmbedBuilder()
                            when (omdb.Type) {
                                "movie" -> {
                                    val movie = OmdbMovie.Deserializer().deserialize(respstring)
                                    if (movie != null) {
                                        embed.setTitle(movie.Title, "https://www.imdb.com/title/${movie.imdbID}")
                                                .setDescription(movie.Plot)
                                                .setThumbnail(movie.Poster)
                                                .addField("Rated", movie.Rated, true)
                                                .addField("Runtime", movie.Runtime, true)
                                                .addField("Languages", movie.Language, true)
                                                .addField("Rating", movie.imdbRating, true)
                                                .addField("Type", movie.Type, true)
                                                .addField("Genres", movie.Type, true)
                                                .addField("Awards", movie.Awards, false)
                                                .addField("Released", movie.Released, false)
                                    } else {
                                        e.reply("Something went wrong while deserializing the response string")
                                    }
                                }
                                "serie" -> {
                                    val serie = OmdbTvshow.Deserializer().deserialize(respstring)
                                    if (serie != null) {
                                        embed.setTitle(serie.Title, "https://www.imdb.com/title/${serie.imdbID}")
                                                .setDescription(serie.Plot)
                                                .setThumbnail(serie.Poster)
                                                .addField("Rated", serie.Rated, true)
                                                .addField("Runtime", serie.Runtime, true)
                                                .addField("Languages", serie.Language, true)
                                                .addField("Awards", serie.Awards, true)
                                                .addField("Rating", serie.imdbRating, true)
                                                .addField("Type", serie.Type, true)
                                                .addField("Genres", serie.Type, false)
                                                .addField("Released", serie.Released, false)
                                    } else {
                                        e.reply("Something went wrong while deserializing the response string")
                                    }
                                }
                                "episode" -> {
                                    val episode = OmdbEpisode.Deserializer().deserialize(respstring)
                                    if (episode != null) {
                                        embed.setTitle(episode.Title, "https://www.imdb.com/title/${episode.imdbID}")
                                                .setDescription(episode.Plot)
                                                .setThumbnail(episode.Poster)
                                                .addField("Rated", episode.Rated, true)
                                                .addField("Runtime", episode.Runtime, true)
                                                .addField("Languages", episode.Language, true)
                                                .addField("Awards", episode.Awards, true)
                                                .addField("Rating", episode.imdbRating, true)
                                                .addField("Type", episode.Type, true)
                                                .addField("Genres", episode.Type, false)
                                                .addField("Released", episode.Released, false)
                                    } else {
                                        e.reply("Something went wrong while deserializing the response string")
                                    }
                                }
                                else -> {
                                    e.reply("Oops looks like something when wrong while fetching the data")
                                }
                            }
                            e.reply(embed)
                        }
                    } else {
                        e.reply("Something went wrong while trying to fetch a random image")
                    }
                }
            })
        }
    }
}
