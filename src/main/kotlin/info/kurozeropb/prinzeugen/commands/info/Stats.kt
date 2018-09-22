package info.kurozeropb.prinzeugen.commands.info

import info.kurozeropb.prinzeugen.Prinz
import info.kurozeropb.prinzeugen.commands.Command
import info.kurozeropb.prinzeugen.utils.Utils
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import java.lang.StringBuilder
import java.text.DecimalFormat

class Stats : Command(
        name = "stats",
        category = "info",
        description = "Shows stats about Prinz",
        botPermissions = listOf(Permission.MESSAGE_WRITE)
) {

    private val dpFormatter = DecimalFormat("0.00")

    override suspend fun execute(args: List<String>, e: MessageReceivedEvent) {
        Utils.catchAll("Exception occured in stats command", e.channel) {
            val uptime = Utils.parseTime(Prinz.uptime)
            val ramUsedRaw = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
            val ramUsedMB = ramUsedRaw / 1048576
            val ramUsedPercent = dpFormatter.format(ramUsedRaw.toDouble() / Runtime.getRuntime().totalMemory() * 100)
            val shardCount = Prinz.shardManager.shardsTotal
            val averageShardLatency = Prinz.shardManager.shards
                    .stream()
                    .map { shard -> shard.ping }
                    .reduce { a, b -> a + b }
                    .get() / shardCount

            val onlineShards = Prinz.shardManager.shards.asSequence().filter { shard -> shard.status == JDA.Status.CONNECTED }.count()

            val sb = StringBuilder()
            sb.append("```md\n")
            sb.append("# Prinz Stats\n")
            sb.append("- Version       ->   v${Prinz.config.version}\n")
            sb.append("- Uptime        ->   $uptime\n")
            sb.append("- RAM Usage     ->   ${ramUsedMB}MB ($ramUsedPercent%)\n")
            sb.append("- Threads       ->   ${Thread.activeCount()}\n")
            sb.append("- Guilds        ->   ${Prinz.shardManager.guildCache.size()}\n")
            sb.append("- Users         ->   ${Prinz.shardManager.userCache.size()}\n")
            sb.append("- Shards Online ->   $onlineShards/$shardCount\n")
            sb.append("- Average Ping  ->   ${averageShardLatency}ms\n")
            sb.append("```")

            e.reply(sb.toString())
        }
    }
}
