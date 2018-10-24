package info.kurozeropb.sophie.commands.info

import info.kurozeropb.sophie.commands.Command
import info.kurozeropb.sophie.utils.Utils
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

class Channel : Command(
        name = "channelinfo",
        aliases = listOf("channel"),
        category = "info",
        description = "Get info about the current channel",
        allowPrivate = false,
        botPermissions = listOf(Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS)
) {

    private val formatter = DateTimeFormatterBuilder()
            .appendValue(ChronoField.MONTH_OF_YEAR, 2)
            .appendPattern("/")
            .appendValue(ChronoField.DAY_OF_MONTH, 2)
            .appendPattern("/")
            .appendValue(ChronoField.YEAR, 4)
            .toFormatter()

    override suspend fun execute(args: List<String>, e: MessageReceivedEvent) {
        Utils.catchAll("Exception occured in channel command", e.channel) {
            val channel = e.textChannel
            e.reply(EmbedBuilder()
                    .setTitle("Channel info of " + channel.name)
                    .setThumbnail(e.guild.iconUrl ?: "https://discordapp.com/assets/6debd47ed13483642cf09e832ed0bc1b.png")
                    .addField("Guild", channel.guild.name, true)
                    .addField("Name", channel.name, true)
                    .addField("Position", channel.position.toString(), true)
                    .addField("Extra", """
                        **NSFW:** ${if (channel.isNSFW) "yes" else "no"}
                        **Category:** ${channel.parent?.name ?: "-"}
                    """.trimIndent(), true)
                    .addField("Topic", channel.topic ?: "-", false)
                    .setFooter("ID: ${channel.id} | Created on: ${channel.creationTime.format(formatter)}", null)
            )
        }
    }
}