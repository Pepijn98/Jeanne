package info.kurozeropb.jeanne.commands.`fun`

import info.kurozeropb.jeanne.commands.Command
import info.kurozeropb.jeanne.core.Utils
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

class Raffle : Command(
        name = "raffle",
        category = Category.FUN,
        description = "Gets a random member from the guild this command is used in.",
        allowPrivate = false,
        isHidden = false,
        isDeveloperOnly = false
) {

    private val formatter = DateTimeFormatterBuilder()
            .appendValue(ChronoField.MONTH_OF_YEAR, 2)
            .appendPattern("/")
            .appendValue(ChronoField.DAY_OF_MONTH, 2)
            .appendPattern("/")
            .appendValue(ChronoField.YEAR, 4)
            .toFormatter()

    override suspend fun execute(args: List<String>, e: MessageReceivedEvent) {
        Utils.catchAll("Exception occured in raffle command", e.channel) {
            val allMembers = e.guild.members
            val members = allMembers.filter { m -> m.user.isBot.not() }
            val random = members.random()

            e.reply(EmbedBuilder()
                    .setTitle("The winner is:")
                    .setDescription("${random.asMention}\n[${random.user.name}#${random.user.discriminator} - (${random.user.id})]\n\n**Congratulations** <a:NekoHype:425781622814539776>")
                    .setThumbnail(random.user.effectiveAvatarUrl)
                    .setFooter("Joined on: ${random.joinDate.format(formatter)}", null))
        }
    }
}