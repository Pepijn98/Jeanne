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
import info.kurozeropb.sophie.utils.Games
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
    lateinit var defaultHeaders: MutableMap<String, String>

    var isReady: Boolean = false
    val uptime: Long
        get() = System.currentTimeMillis() - bootTime

    @JvmStatic
    fun main(args: Array<String>) {
        val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
        loggerLevels.forEach { key, value -> loggerContext.getLogger(key).level = value }
        config = ConfigManager.read()
        val token = if (config.env.startsWith("dev")) config.tokens.dev else if (config.env.startsWith("test")) "" else config.tokens.prod
        httpClient = if (config.proxy.enabled) OkHttpClient.Builder().proxy(Proxy(Proxy.Type.SOCKS, InetSocketAddress(config.proxy.host, config.proxy.port))).build() else OkHttpClient.Builder().build()
        embedColor = Color.decode("0xBA2F6B")
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
                    .setGame(Game.playing("https://sophiebot.info/"))
                    .setBulkDeleteSplittingEnabled(false)
                    .addEventListeners(EventManager())
                    .build()
        }

        GlobalScope.async {
            Utils.setInterval(600000) {
                val game = Games.list[Math.floor((Math.random() * Games.list.size)).toInt()]
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
