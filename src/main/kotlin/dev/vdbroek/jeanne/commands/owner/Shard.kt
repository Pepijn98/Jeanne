package dev.vdbroek.jeanne.commands.owner

import dev.vdbroek.jeanne.Jeanne
import dev.vdbroek.jeanne.Status
import dev.vdbroek.jeanne.commands.Command
import dev.vdbroek.jeanne.core.Utils
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

class Shard : Command(
    name = "shards",
    category = Category.OWNER,
    description = "Shows all shard statuses",
    allowPrivate = false,
    isDeveloperOnly = true,
    isHidden = true,
    botPermissions = listOf(Permission.MESSAGE_WRITE, Permission.MESSAGE_READ)
) {

    override suspend fun execute(args: List<String>, e: MessageReceivedEvent) {
        Utils.catchAll("Exception occured in stats command", e.channel) {
            val shards = Jeanne.shardManager.shards
            val shardCount = Jeanne.shardManager.shardsTotal

            val avgShardLatency = shards.stream()
                .map { shard -> shard.gatewayPing }
                .reduce { a, b -> a + b }
                .get() / shardCount

            val embed = EmbedBuilder()
                .setAuthor("Shards Info", null, e.jda.selfUser.effectiveAvatarUrl)
                .setFooter("Total | Servers: ${Jeanne.shardManager.guilds.size}, Cached Users: ${Jeanne.shardManager.userCache.size()}, Avg. Ping: ${avgShardLatency}ms", null)

            shards.reversed().forEach { shard ->
                embed.addField(
                    "Shard ${shard.shardInfo.shardId} ${Status.valueOf(shard.status.name).emote} ${if (e.guild.jda.shardInfo.shardId == shard.shardInfo.shardId) "(current)" else ""}",
                    """
                    ${shard.guilds.size} guilds
                    ${shard.userCache.size()} users (cached)
                    Gateway ${shard.gatewayPing}ms
                    Rest ${shard.restPing.complete()}ms
                    """.trimIndent(),
                    true
                )
            }

            e.reply(embed)
        }
    }
}
