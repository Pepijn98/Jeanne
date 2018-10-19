package info.kurozeropb.sophie

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

data class DBConfig(
        val host: String,
        val port: Int,
        val name: String
)

data class ApiConfig(
        val url: String,
        val token: String
)

data class ProxyConfig(
        val enabled: Boolean,
        val host: String,
        val port: Int
)

data class Config(
        val version: String,
        val env: String,
        val prefix: String,
        val devToken: String,
        val token: String,
        val eWebhook: String,
        val developer: String,
        val db: DBConfig,
        val api: ApiConfig,
        val proxy: ProxyConfig
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