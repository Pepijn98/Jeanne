package info.kurozeropb.jeanne

import info.kurozeropb.jeanne.commands.Registry
import info.kurozeropb.jeanne.managers.ConfigManager
import info.kurozeropb.jeanne.managers.DatabaseManager
import info.kurozeropb.jeanne.managers.EventManager
import info.kurozeropb.jeanne.core.Utils
import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.bot.sharding.ShardManager
import net.dv8tion.jda.core.entities.Game
import okhttp3.OkHttpClient
import java.awt.Color
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import com.github.natanbc.weeb4j.Weeb4J
import info.kurozeropb.jeanne.core.games
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.*
import kotlin.concurrent.schedule

object Jeanne {
    private val bootTime = System.currentTimeMillis()
    private val loggerLevels = mapOf(
            "org.mongodb.driver" to Level.WARN,
            "net.dv8tion.jda" to Level.INFO,
            "org.reflections.Reflections" to Level.INFO
    )

    lateinit var shardManager: ShardManager
    lateinit var embedColor: Color
    lateinit var config: Config
    lateinit var httpClient: OkHttpClient
    lateinit var defaultHeaders: Map<String, String>
    lateinit var weebApi: Weeb4J

    var isReady: Boolean = false
    val uptime: Long
        get() = System.currentTimeMillis() - bootTime

    @JvmStatic
    fun main(args: Array<String>) {
        Utils.catchAll("Exception occured in main", null) {
            // Disable certain loggers
            val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
            loggerLevels.forEach { key, value -> loggerContext.getLogger(key).level = value }

            // Initialize the bot config
            config = ConfigManager.read()

            // Get the bot token depending on which enviorment version
            val token = when {
                config.env.startsWith("dev") -> config.tokens.dev
                config.env.startsWith("beta") -> config.tokens.beta
                else -> config.tokens.prod
            }

            // Create a default http client
            httpClient =
                    if (config.proxy.enabled)
                        OkHttpClient.Builder().proxy(Proxy(Proxy.Type.SOCKS, InetSocketAddress(config.proxy.host, config.proxy.port))).build()
                    else
                        OkHttpClient.Builder().build()

            // Default embed color to use
            embedColor = Color.decode(config.defaultColor)

            // Initialize the database
            DatabaseManager.initialize(config)
            // Register all commands
            Registry().loadCommands()
            // Connect to discord
            connect(token)
        }
    }

    private fun connect(token: String) {
        Utils.catchAll("Failed to connect to discord", null) {
            // Login to discord
            shardManager = DefaultShardManagerBuilder()
                    .setShardsTotal(-1)
                    .setToken(token)
                    .setGame(Game.playing("https://jeannebot.info/"))
                    .setBulkDeleteSplittingEnabled(false)
                    .addEventListeners(EventManager())
                    .build()
        }

        // Updates the playing game every 10 minutes
        Timer().schedule(600_000, 600_000) {
            Utils.catchAll("Failed to update playing game", null) {
                val game = games[Math.floor((Math.random() * games.size)).toInt()]
                val name = game.name
                        .replace(Regex("%USERSIZE%"), shardManager.users.size.toString())
                        .replace(Regex("%GUILDSIZE%"), shardManager.guilds.size.toString())

                shardManager.setGame(Game.of(game.type, name))
            }
        }
    }
}

// All the damn bot lists to send stats to
enum class BotLists(val url: String) {
    BOTLIST_SPACE("https://botlist.space/api/bots/237578660708745216"),
    BOTSFORDISCORD("https://botsfordiscord.com/api/bot/237578660708745216"),
    BOTS_ONDISCORD("https://bots.ondiscord.xyz/bot-api/bots/237578660708745216/guilds"),
    DISCORDBOATS("https://discordboats.club/api/public/bot/stats"),
    DISCORDBOTS_ORG("https://discordbots.org/api/bots/237578660708745216/stats"),
    DISCORDBOT_WORLD("https://discordbot.world/api/bot/237578660708745216/stats"),
    DISCORD_BOTS_GG("https://discord.bots.gg/api/v1/bots/237578660708745216/stats"),
    DISCORDBOTS_GROUP("https://discordbots.group/api/bot/237578660708745216")
}

enum class ExitStatus(val code: Int) {
    // Non error
    UPDATE(10),
    SHUTDOWN(11),
    RESTART(12),
    NEW_CONFIG(13),

    // Error
    INVALID_TOKEN(20),
    CONFIG_MISSING(21),
    DUPLICATE_COMMAND_NAME(22)
}

enum class Status(val emote: String) {
    INITIALIZING("<:dnd:514793069766377472>"),
    INITIALIZED("<:dnd:514793069766377472>"),
    LOGGING_IN("<:away:514793069435027468>"),
    CONNECTING_TO_WEBSOCKET("<:away:514793069435027468>"),
    IDENTIFYING_SESSION("<:away:514793069435027468>"),
    AWAITING_LOGIN_CONFIRMATION("<:away:514793069435027468>"),
    LOADING_SUBSYSTEMS("<:away:514793069435027468>"),
    CONNECTED("<:online:514793069883686952>"),
    DISCONNECTED("<:offline:514793069640679434>"),
    RECONNECT_QUEUED("<:offline:514793069640679434>"),
    WAITING_TO_RECONNECT("<:dnd:514793069766377472>"),
    ATTEMPTING_TO_RECONNECT("<:away:514793069435027468>"),
    SHUTTING_DOWN("<:offline:514793069640679434>"),
    SHUTDOWN("<:offline:514793069640679434>"),
    FAILED_TO_LOGIN("<:offline:514793069640679434>")
}