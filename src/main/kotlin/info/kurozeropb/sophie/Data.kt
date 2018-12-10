package info.kurozeropb.sophie

import com.beust.klaxon.Json
import info.kurozeropb.sophie.commands.Command
import net.dv8tion.jda.core.Permission
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
        val beta: String,
        val prod: String,
        val exception_hook: String,
        val dev_exception_hook: String,
        val wolke: String,
        val imdb: String,
        val kurozero: String,
        val botlists: HashMap<String, String>
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
        val defaultColor: String,
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
data class CatData(val file: String)

// https://nekos.moe
data class Nekos(val images: List<Neko>)
data class Neko(val id: String)

// http://media.obutts.ru
// http://media.oboobs.ru
data class ONsfwData(val preview: String, val id: Int)

// http://api.program-o.com
data class ProgramO(@Json(name = "botsay") val reply: String)

// http://www.omdbapi.com/
data class OmdbTypeTest(val Response: String)
data class OmdbError(val Response: String, val Error: String)
data class Omdb(
        val Title: String,
        val Rated: String,
        val Released: String,
        val Runtime: String,
        val Genre: String,
        val Plot: String,
        val Language: String,
        val Awards: String,
        val Poster: String,
        val imdbRating: String,
        val imdbID: String,
        val Type: String? = null
)

data class PokemonNames(val fr: String, val de: String, val it: String, val en: String)
data class PokemonGenderRatio(val male: Double? = 0.0, val female: Double? = 0.0)
data class PokemonEvolution(val to: String, val level: Int? = null)
data class PokemonCategories(val en: String, val de: String)
data class PokemonData(
        val names: PokemonNames,
        val national_id: Int,
        val types: ArrayList<String>,
        val gender_ratios: PokemonGenderRatio? = null,
        val catch_rate: Int,
        val egg_groups: ArrayList<String>,
        val hatch_time: ArrayList<Int>,
        val height_us: String,
        val height_eu: String,
        val weight_us: String,
        val weight_eu: String,
        val leveling_rate: String,
        val categories: PokemonCategories,
        val evolutions: ArrayList<PokemonEvolution>
)

data class CommandData(
        val name: String,
        val category: Command.Category,
        val description: String,
        val usage: String? = null,
        val aliases: List<String> = listOf(),
        val subCommands: List<String> = listOf(),
        val cooldown: Long = 5,
        val isDonatorsOnly: Boolean = false,
        val allowPrivate: Boolean = true,
        val isDeveloperOnly: Boolean = false,
        val isHidden: Boolean = false,
        val userPermissions: List<Permission> = listOf(),
        val botPermissions: List<Permission> = listOf()
)