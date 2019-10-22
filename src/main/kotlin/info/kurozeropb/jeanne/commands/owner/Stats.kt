package info.kurozeropb.jeanne.commands.owner

import info.kurozeropb.jeanne.Jeanne
import info.kurozeropb.jeanne.commands.Command
import info.kurozeropb.jeanne.core.Utils
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.lang.StringBuilder
import java.text.DecimalFormat

class Stats : Command(
        name = "stats",
        category = Category.OWNER,
        description = "Shows stats about Jeanne",
        isDeveloperOnly = true,
        isHidden = true,
        botPermissions = listOf(Permission.MESSAGE_WRITE)
) {

    private val dpFormatter = DecimalFormat("0.00")

    override suspend fun execute(args: List<String>, e: MessageReceivedEvent) {
        Utils.catchAll("Exception occured in stats command", e.channel) {
            val uptime = Utils.parseTime(Jeanne.uptime)
            val ramUsedRaw = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
            val ramUsedMB = ramUsedRaw / 1048576
            val ramUsedPercent = dpFormatter.format(ramUsedRaw.toDouble() / Runtime.getRuntime().totalMemory() * 100)
            val shardCount = Jeanne.shardManager.shardsTotal
            val averageShardLatency = Jeanne.shardManager.shards
                    .stream()
                    .map { shard -> shard.gatewayPing }
                    .reduce { a, b -> a + b }
                    .get() / shardCount

            val onlineShards = Jeanne.shardManager.shards.asSequence().filter { shard -> shard.status == JDA.Status.CONNECTED }.count()

            val sb = StringBuilder()
            sb.append("```md\n")
            sb.append("# Jeanne Stats\n")
            sb.append("- Version       ->   v${Jeanne.config.version}\n")
            sb.append("- Uptime        ->   $uptime\n")
            sb.append("- RAM Usage     ->   ${ramUsedMB}MB ($ramUsedPercent%)\n")
            sb.append("- Threads       ->   ${Thread.activeCount()}\n")
            sb.append("- Guilds        ->   ${Jeanne.shardManager.guilds.size} | Cached: ${Jeanne.shardManager.guildCache.size()}\n")
            sb.append("- Users         ->   ${Jeanne.shardManager.users.size} | Cached: ${Jeanne.shardManager.userCache.size()}\n")
            sb.append("- Shards Online ->   $onlineShards/$shardCount\n")
            sb.append("- Average Ping  ->   ${averageShardLatency}ms\n")
            sb.append("```")

            e.reply(sb.toString())
        }
    }
}
