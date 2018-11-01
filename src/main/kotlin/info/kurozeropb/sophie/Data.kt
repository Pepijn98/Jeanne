package info.kurozeropb.sophie

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import info.kurozeropb.sophie.commands.Command
import net.dv8tion.jda.core.entities.Game
import java.time.OffsetDateTime

data class Guild(
        val id: String,
        val prefix: String = Sophie.config.prefix,
        val blacklisted: Boolean = false,
        val subbedEvents: ArrayList<String> = arrayListOf(),
        val logChannel: String = "",
        val ignoredCommands: ArrayList<String> = arrayListOf(),
        val welcomeMessage: String = "Welcome %user% to %guild% you are member number %count%",
        val welcomeEnabled: Boolean = false,
        val welcomeChannel: String = "",
        val levelupMessage: String = "%user% has leveled up from %oldLevel% to %newLevel% and now has %points%!",
        val levelupEnabled: Boolean = false
)

data class User(
        val id: String,
        val level: Double = 0.0,
        val points: Double = 0.1,
        val about: String = "I'm a unicorn",
        val blacklisted: Boolean = false,
        val donator: Boolean = false,
        val background: String = "https://b.catgirlsare.sexy/o4xm.png"
)

data class Tokens(
        val dev: String,
        val test: String,
        val prod: String,
        val error: String,
        val wolke: String,
        val imdb: String,
        val kurozero: String,
        val lists: Map<String, String>
)

data class Database(
        val host: String,
        val port: Int,
        val name: String
)

data class Proxy(
        val enabled: Boolean,
        val host: String,
        val port: Int
)

data class Config(
        val version: String,
        val env: String,
        val prefix: String,
        val developer: String,
        val apiUrl: String,
        val tokens: Tokens,
        val database: Database,
        val proxy: Proxy
)

data class Cooldown(
        val id: String,
        val command: Command,
        val time: OffsetDateTime
)

data class PlayingGame(
        val name: String,
        val type: Game.GameType
)

data class QuestionCache(
        val id: String,
        var question: String
)

// API Data
// https://aws.random.cat/meow
data class CatData(
        val file: String
) {
    class Deserializer : ResponseDeserializable<CatData> {
        override fun deserialize(content: String): CatData? = Gson().fromJson(content, CatData::class.java)
    }
}

// https://nekos.moe
data class Nekos(
        val images: ArrayList<Neko>
) {
    class Deserializer : ResponseDeserializable<Nekos> {
        override fun deserialize(content: String): Nekos? = Gson().fromJson(content, Nekos::class.java)
    }
}

data class Neko(
        val id: String,
        val originalHash: String,
        val uploader: NekoUploader,
        val approver: NekoApprover?,
        val nsfw: Boolean,
        val artist: String,
        val tags: ArrayList<String>,
        val comments: ArrayList<String>,
        val createdAt: String,
        val likes: Int,
        val favorites: Int
)

data class NekoUploader(
        val id: String,
        val username: String
)

data class NekoApprover(
        val id: String,
        val username: String
)

// http://media.obutts.ru
// http://media.oboobs.ru
data class ONsfwData(
        val model: String?,
        val preview: String,
        val id: Int,
        val rank: Int,
        val author: String?
) {
    class Deserializer : ResponseDeserializable<ArrayList<ONsfwData>> {
        override fun deserialize(content: String): ArrayList<ONsfwData>? = Gson().fromJson(content, object: TypeToken<ArrayList<ONsfwData>>(){}.type)
    }
}

// http://api.program-o.com
data class ProgramO(
        val convo_id: String,
        val usersay: String,
        val botsay: String
) {
    class Deserializer : ResponseDeserializable<ProgramO> {
        override fun deserialize(content: String): ProgramO? = Gson().fromJson(content, ProgramO::class.java)
    }
}

// http://www.omdbapi.com/
data class OmdbTypeTest (
        val Type: String,
        val Response: String
)  {
    class Deserializer : ResponseDeserializable<OmdbTypeTest> {
        override fun deserialize(content: String): OmdbTypeTest? = Gson().fromJson(content, OmdbTypeTest::class.java)
    }
}

data class OmdbError (
        val Response: String,
        val Error: String
)  {
    class Deserializer : ResponseDeserializable<OmdbError> {
        override fun deserialize(content: String): OmdbError? = Gson().fromJson(content, OmdbError::class.java)
    }
}

data class OmdbMovie (
    val Title: String,
    val Year: String,
    val Rated: String,
    val Released: String,
    val Runtime: String,
    val Genre: String,
    val Director: String,
    val Writer: String,
    val Actors: String,
    val Plot: String,
    val Language: String,
    val Country: String,
    val Awards: String,
    val Poster: String,
    val Metascore: String,
    val imdbRating: String,
    val imdbVotes: String,
    val imdbID: String,
    val Type: String,
    val Response: String
)  {
    class Deserializer : ResponseDeserializable<OmdbMovie> {
        override fun deserialize(content: String): OmdbMovie? = Gson().fromJson(content, OmdbMovie::class.java)
    }
}

data class OmdbTvshow (
    val Title: String,
    val Year: String,
    val Rated: String,
    val Released: String,
    val Runtime: String,
    val Genre: String,
    val Director: String,
    val Writer: String,
    val Actors: String,
    val Plot: String,
    val Language: String,
    val Country: String,
    val Awards: String,
    val Poster: String,
    val Metascore: String,
    val imdbRating: String,
    val imdbVotes: String,
    val imdbID: String,
    val Type: String,
    val Response: String,
    val totalSeasons: String
)  {
    class Deserializer : ResponseDeserializable<OmdbTvshow> {
        override fun deserialize(content: String): OmdbTvshow? = Gson().fromJson(content, OmdbTvshow::class.java)
    }
}

data class OmdbEpisode (
    val Title: String,
    val Released: String,
    val Episode: String,
    val Type: String,
    val imdbRating: String,
    val imdbID: String,
    val imdbVotes: String,
    val Year: String,
    val Rated: String,
    val Runtime: String,
    val Genre: String,
    val Director: String,
    val Writer: String,
    val Actors: String,
    val Plot: String,
    val Language: String,
    val Country: String,
    val Awards: String,
    val Poster: String,
    val Metascore: String,
    val Response: String
)  {
    class Deserializer : ResponseDeserializable<OmdbEpisode> {
        override fun deserialize(content: String): OmdbEpisode? = Gson().fromJson(content, OmdbEpisode::class.java)
    }
}

data class PokemonData(
    val names: PokemonNames,
    val national_id: Int,
    val types: ArrayList<String>,
    val abilities: ArrayList<PokemonAbility>,
    val gender_ratios: PokemonGenderRatio,
    val catch_rate: String,
    val egg_groups: ArrayList<String>,
    val hatch_time: ArrayList<Int>,
    val height_us: String,
    val height_eu: String,
    val weight_us: String,
    val weight_eu: String,
    val base_exp_yield: String,
    val leveling_rate: String,
    val color: String,
    val base_friendship: String,
    val evolution_from: String,
    val categories: PokemonCategories,
    val evolutions: ArrayList<PokemonEvolution>
) {
    class Deserializer : ResponseDeserializable<PokemonData> {
        override fun deserialize(content: String): PokemonData? = Gson().fromJson(content, PokemonData::class.java)
    }
}

data class PokemonNames(
        val fr: String,
        val de: String,
        val it: String,
        val en: String
)

data class PokemonAbility(
        val name: String,
        val hidden: Boolean?
)

data class PokemonGenderRatio(
        val male: Float,
        val female: Float
)

data class PokemonEvolution(
        val to: String,
        val level: Int
)

data class PokemonCategories(
        val en: String,
        val de: String
)