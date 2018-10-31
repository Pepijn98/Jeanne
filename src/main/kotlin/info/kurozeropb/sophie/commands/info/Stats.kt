package info.kurozeropb.sophie.commands.info

import info.kurozeropb.sophie.Sophie
import info.kurozeropb.sophie.commands.Command
import info.kurozeropb.sophie.utils.Utils
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import java.lang.StringBuilder
import java.text.DecimalFormat

class Stats : Command(
        name = "stats",
        category = Category.INFO,
        description = "Shows stats about Sophie",
        cooldown = 10,
        botPermissions = listOf(Permission.MESSAGE_WRITE)
) {

    private val dpFormatter = DecimalFormat("0.00")

    override suspend fun execute(args: List<String>, e: MessageReceivedEvent) {
        Utils.catchAll("Exception occured in stats command", e.channel) {
            val uptime = Utils.parseTime(Sophie.uptime)
            val ramUsedRaw = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
            val ramUsedMB = ramUsedRaw / 1048576
            val ramUsedPercent = dpFormatter.format(ramUsedRaw.toDouble() / Runtime.getRuntime().totalMemory() * 100)
            val shardCount = Sophie.shardManager.shardsTotal
            val averageShardLatency = Sophie.shardManager.shards
                    .stream()
                    .map { shard -> shard.ping }
                    .reduce { a, b -> a + b }
                    .get() / shardCount

            val onlineShards = Sophie.shardManager.shards.asSequence().filter { shard -> shard.status == JDA.Status.CONNECTED }.count()

            val sb = StringBuilder()
            sb.append("```md\n")
            sb.append("# Sophie Stats\n")
            sb.append("- Version       ->   v${Sophie.config.version}\n")
            sb.append("- Uptime        ->   $uptime\n")
            sb.append("- RAM Usage     ->   ${ramUsedMB}MB ($ramUsedPercent%)\n")
            sb.append("- Threads       ->   ${Thread.activeCount()}\n")
            sb.append("- Guilds        ->   ${Sophie.shardManager.guildCache.size()}\n")
            sb.append("- Users         ->   ${Sophie.shardManager.userCache.size()}\n")
            sb.append("- Shards Online ->   $onlineShards/$shardCount\n")
            sb.append("- Average Ping  ->   ${averageShardLatency}ms\n")
            sb.append("```")

            e.reply(sb.toString())
        }
    }
}
