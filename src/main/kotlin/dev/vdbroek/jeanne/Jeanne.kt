package dev.vdbroek.jeanne

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import com.github.azurlane_api.api.AzurLane
import com.github.azurlane_api.api.entities.Options
import com.github.natanbc.weeb4j.Weeb4J
import dev.vdbroek.jeanne.commands.Registry
import dev.vdbroek.jeanne.core.Utils
import dev.vdbroek.jeanne.core.getRandomActivity
import dev.vdbroek.jeanne.core.minutes
import dev.vdbroek.jeanne.managers.ConfigManager
import dev.vdbroek.jeanne.managers.DatabaseManager
import dev.vdbroek.jeanne.managers.EventManager
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.api.sharding.ShardManager
import net.dv8tion.jda.api.utils.MemberCachePolicy
import okhttp3.OkHttpClient
import org.slf4j.LoggerFactory
import java.awt.Color
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.*
import java.util.concurrent.TimeUnit
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
    lateinit var azurlane: AzurLane

    fun isShardManagerInitialized(): Boolean = Jeanne::shardManager.isInitialized

    var isReady: Boolean = false
    val uptime: Long
        get() = System.currentTimeMillis() - bootTime

    @JvmStatic
    fun main(args: Array<String>) {
        Utils.catchAll("Exception occured in main", null) {
            // Disable certain loggers
            val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
            loggerLevels.forEach { (key, value) -> loggerContext.getLogger(key).level = value }

            // Initialize the bot config
            config = ConfigManager.read()

            azurlane = AzurLane(
                Options(
                    userAgent = "JeanneBot/v${config.version} (github.com/Pepijn98/Jeanne)",
                    token = config.tokens.azurlane
                )
            )

            // Get the bot token depending on which enviorment version
            val token = when {
                config.env.startsWith("dev") -> config.tokens.development
                config.env.startsWith("beta") -> config.tokens.beta
                else -> config.tokens.production
            }

            // Create a default http client
            httpClient =
                if (config.proxy.enabled)
                    OkHttpClient.Builder()
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)
                        .proxy(Proxy(Proxy.Type.SOCKS, InetSocketAddress(config.proxy.host, config.proxy.port)))
                        .build()
                else
                    OkHttpClient.Builder()
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)
                        .build()

            // Default embed color to use
            embedColor = Color.decode(config.defaultColor)

            // Initialize the database
            try {
                DatabaseManager.initialize(config)
            } catch (e: Exception) {
                println(e)
            }
            // Register all commands
            Registry().loadCommands()
            // Connect to discord
            connect(token)
        }
    }

    private fun connect(token: String) {
        Utils.catchAll("Failed to connect to discord", null) {
            // Login to discord
            shardManager = DefaultShardManagerBuilder.createDefault(token)
                .enableIntents(GatewayIntent.GUILD_MEMBERS)
                .disableIntents(
                    GatewayIntent.GUILD_PRESENCES,
                    GatewayIntent.GUILD_MESSAGE_TYPING,
                    GatewayIntent.DIRECT_MESSAGES,
                    GatewayIntent.DIRECT_MESSAGE_REACTIONS,
                    GatewayIntent.DIRECT_MESSAGE_TYPING
                )
                .setMemberCachePolicy(MemberCachePolicy.OWNER)
                .setShardsTotal(-1)
                .setBulkDeleteSplittingEnabled(false)
                .addEventListeners(EventManager())
                .build()
        }

        // Updates the playing game every 30 minutes
        Timer().schedule(30.minutes, 30.minutes) {
            Utils.catchAll("Failed to update playing game", null) {
                val (game, name) = getRandomActivity()
                shardManager.setActivity(Activity.of(game.type, name))
            }
        }
    }
}

// All the damn bot lists to send stats to
enum class BotLists(val url: String) {
    BOTLIST_SPACE("https://api.botlist.space/v1/bots/237578660708745216"),
    BOTSFORDISCORD("https://botsfordiscord.com/api/bot/237578660708745216"),
    BOTS_ONDISCORD("https://bots.ondiscord.xyz/bot-api/bots/237578660708745216/guilds"),
    DISCORDBOATS("https://discord.boats/api/public/bot/stats"),
    DISCORDBOTS_ORG("https://top.gg/api/bots/237578660708745216/stats"),
    DISCORDBOT_WORLD("https://discordbot.world/api/bot/237578660708745216/stats")
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