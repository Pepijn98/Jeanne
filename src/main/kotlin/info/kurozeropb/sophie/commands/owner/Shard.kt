package info.kurozeropb.sophie.commands.owner

import info.kurozeropb.sophie.Sophie
import info.kurozeropb.sophie.commands.Command
import info.kurozeropb.sophie.core.Utils
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.MessageReceivedEvent

class Shard : Command(
        name = "shards",
        category = Category.OWNER,
        description = "Shows all shard statuses",
        isDeveloperOnly = true,
        isHidden = true,
        botPermissions = listOf(Permission.MESSAGE_WRITE, Permission.MESSAGE_READ)
) {

    override suspend fun execute(args: List<String>, e: MessageReceivedEvent) {
        Utils.catchAll("Exception occured in stats command", e.channel) {
            val shardCount = Sophie.shardManager.shardsTotal
            val avgShardLatency = Sophie.shardManager.shards
                    .stream()
                    .map { shard -> shard.ping }
                    .reduce { a, b -> a + b }
                    .get() / shardCount

            val embed = EmbedBuilder()
                    .setAuthor("Shards Info", null, e.jda.selfUser.effectiveAvatarUrl)
                    .setFooter("Total | Servers: ${Sophie.shardManager.guilds.size}, Users: ${Sophie.shardManager.users.size}, Avg. Ping: ${avgShardLatency}ms", null)

            Sophie.shardManager.shards.reversed().forEach { shard ->
                embed.addField("Shard ${shard.shardInfo.shardId} ${Utils.statusMap[shard.status.name]} ${if (e.guild.jda.shardInfo.shardId == shard.shardInfo.shardId) "(current)" else ""}", """
                    ${shard.guilds.size} guilds
                    ${shard.users.size} users
                    ${shard.ping}ms
                """.trimIndent(), true)
            }

            e.reply(embed)
        }
    }
}
