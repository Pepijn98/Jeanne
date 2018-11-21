package info.kurozeropb.sophie

import info.kurozeropb.sophie.commands.Registry
import info.kurozeropb.sophie.managers.ConfigManager
import info.kurozeropb.sophie.managers.DatabaseManager
import info.kurozeropb.sophie.managers.EventManager
import info.kurozeropb.sophie.core.Utils
import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.bot.sharding.ShardManager
import net.dv8tion.jda.core.entities.Game
import okhttp3.OkHttpClient
import java.awt.Color
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import com.github.natanbc.weeb4j.Weeb4J
import info.kurozeropb.sophie.core.games
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.net.Proxy

object Sophie {
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
                    .setGame(Game.playing("https://sophiebot.info/"))
                    .setBulkDeleteSplittingEnabled(false)
                    .addEventListeners(EventManager())
                    .build()
        }

        GlobalScope.async {
            // Change the playing game ever 30 minutes
            Utils.setInterval(600_000) {
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
    BOTS_DISCORD_PW("https://bots.discord.pw/api/bots/237578660708745216/stats"),
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
