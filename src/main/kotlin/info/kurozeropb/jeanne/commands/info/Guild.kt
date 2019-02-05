package info.kurozeropb.jeanne.commands.info

import info.kurozeropb.jeanne.commands.Command
import info.kurozeropb.jeanne.core.Utils
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.OnlineStatus
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.concurrent.TimeUnit

class Guild : Command(
        name = "guildinfo",
        aliases = listOf("serverinfo", "guild", "server"),
        category = Category.INFO,
        description = "Get info about the guild",
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
        Utils.catchAll("Exception occured in guild command", e.channel) {
            e.reply(EmbedBuilder()
                    .setTitle(e.guild.name)
                    .setThumbnail(e.guild.iconUrl ?: "https://discordapp.com/assets/6debd47ed13483642cf09e832ed0bc1b.png")
                    .addField("Members", """
                        **${e.guild.members.size}** Total
                        **${e.guild.members.filter { it.user.isBot }.size}** Bots
                        **${e.guild.members.filter { it.user.isBot.not() }.size}** People
                        **${e.guild.members.filter { it.onlineStatus == OnlineStatus.ONLINE }.size}** Online
                    """.trimIndent(), true)
                    .addField("Channels", """
                        **${e.guild.textChannels.size}** Text
                        **${e.guild.voiceChannels.size}** Voice
                        **${e.guild.categories.size}** Categories
                        **AFK:** ${if (e.guild.afkChannel != null) "#${e.guild.afkChannel.name}" else "-"}
                        **AFK Timeout:** ${TimeUnit.SECONDS.toMinutes(e.guild.afkTimeout.seconds.toLong()).toInt()} Minutes
                        ${Utils.ZERO_WIDTH_SPACE}
                    """.trimIndent(), true)
                    .addField("Defaults", """
                        **Channel:** ${e.guild.defaultChannel?.asMention ?: "-"}
                        **Notifications:** ${e.guild.defaultNotificationLevel.name.toLowerCase().replace("_", " ").capitalize()}
                    """.trimIndent(), true)
                    .addField("Others", """
                        **Region:** ${e.guild.regionRaw.capitalize()}
                        **Verification Level:** ${e.guild.verificationLevel.name.toLowerCase().replace("_", " ").capitalize()}
                        **MFA:** ${e.guild.requiredMFALevel.name.toLowerCase().replace("_", " ").capitalize()}
                        **Roles:** ${e.guild.roles.size}
                    """.trimIndent(), true)
                    .setFooter("ID: ${e.guild.id} | Created on: ${e.guild.creationTime.format(formatter)}", null)
            )
        }
    }
}