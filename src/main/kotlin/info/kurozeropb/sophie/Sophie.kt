package info.kurozeropb.sophie

import info.kurozeropb.sophie.commands.Registry
import info.kurozeropb.sophie.managers.ConfigManager
import info.kurozeropb.sophie.managers.DatabaseManager
import info.kurozeropb.sophie.managers.EventManager
import info.kurozeropb.sophie.utils.Utils
import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.bot.sharding.ShardManager
import net.dv8tion.jda.core.entities.Game
import okhttp3.OkHttpClient
import java.awt.Color
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import kotlinx.coroutines.experimental.async
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
    private val games = arrayListOf(
            PlayingGame("with Senpai", Game.GameType.DEFAULT),
            PlayingGame("with my master", Game.GameType.DEFAULT),
            PlayingGame("anime", Game.GameType.WATCHING),
            PlayingGame("secret things", Game.GameType.WATCHING),
            PlayingGame("with your feelings", Game.GameType.DEFAULT),
            PlayingGame("https://sophiebot.info", Game.GameType.WATCHING),
            PlayingGame("with %USERSIZE% users", Game.GameType.DEFAULT),
            PlayingGame("in %GUILDSIZE% servers", Game.GameType.DEFAULT),
            PlayingGame("%GUILDSIZE% servers", Game.GameType.WATCHING),
            PlayingGame("%USERSIZE% users", Game.GameType.WATCHING),
            PlayingGame("music", Game.GameType.LISTENING)
    )

    lateinit var shardManager: ShardManager
    lateinit var embedColor: Color
    lateinit var config: Config
    lateinit var httpClient: OkHttpClient
    lateinit var defaultHeaders: MutableMap<String, String>

    var isReady: Boolean = false
    val uptime: Long
        get() = System.currentTimeMillis() - bootTime

    @JvmStatic
    fun main(args: Array<String>) {
        val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
        loggerLevels.forEach { key, value -> loggerContext.getLogger(key).level = value }
        config = ConfigManager.read()
        val token = if (config.env.startsWith("dev")) config.devToken else config.token
        httpClient = if (config.proxy.enabled) OkHttpClient.Builder().proxy(Proxy(Proxy.Type.SOCKS, InetSocketAddress(config.proxy.host, config.proxy.port))).build() else OkHttpClient.Builder().build()
        embedColor = Color.decode("0x6d7293")
        Utils.catchAll("Exception occured in main", null) {
            DatabaseManager.initialize(config)
            Registry().loadCommands()
            connect(token)
        }
    }

    private fun connect(token: String) {
        Utils.catchAll("Failed to connect to discord", null) {
            shardManager = DefaultShardManagerBuilder()
                    .setShardsTotal(-1)
                    .setToken(token)
                    .setGame(Game.playing("Jeanne d'Arc will be renamed to Sophie Twilight after the rewrite! Yaay!!"))
                    .setBulkDeleteSplittingEnabled(false)
                    .addEventListeners(EventManager())
                    .build()
        }

        async {
            Utils.setInterval(600000) {
                val game = games[Math.floor((Math.random() * games.size)).toInt()]
                val name = game.name
                        .replace(Regex("%USERSIZE%"), shardManager.users.size.toString())
                        .replace(Regex("%GUILDSIZE%"), shardManager.guilds.size.toString())

                shardManager.setGame(Game.of(game.type, name))
            }
        }
    }
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
