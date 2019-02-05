package info.kurozeropb.jeanne.commands.owner

import info.kurozeropb.jeanne.Jeanne
import info.kurozeropb.jeanne.Status
import info.kurozeropb.jeanne.commands.Command
import info.kurozeropb.jeanne.core.Utils
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
            val shardCount = Jeanne.shardManager.shardsTotal
            val avgShardLatency = Jeanne.shardManager.shards
                    .stream()
                    .map { shard -> shard.ping }
                    .reduce { a, b -> a + b }
                    .get() / shardCount

            val embed = EmbedBuilder()
                    .setAuthor("Shards Info", null, e.jda.selfUser.effectiveAvatarUrl)
                    .setFooter("Total | Servers: ${Jeanne.shardManager.guilds.size}, Users: ${Jeanne.shardManager.users.size}, Avg. Ping: ${avgShardLatency}ms", null)

            Jeanne.shardManager.shards.reversed().forEach { shard ->
                embed.addField("Shard ${shard.shardInfo.shardId} ${Status.valueOf(shard.status.name).emote} ${if (e.guild.jda.shardInfo.shardId == shard.shardInfo.shardId) "(current)" else ""}", """
                    ${shard.guilds.size} guilds
                    ${shard.users.size} users
                    ${shard.ping}ms
                """.trimIndent(), true)
            }

            e.reply(embed)
        }
    }
}
