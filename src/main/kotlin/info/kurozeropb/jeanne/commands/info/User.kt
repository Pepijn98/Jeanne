package info.kurozeropb.jeanne.commands.info

import info.kurozeropb.jeanne.Jeanne
import info.kurozeropb.jeanne.commands.Command
import info.kurozeropb.jeanne.core.Utils
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

class User : Command(
        name = "userinfo",
        aliases = listOf("memberinfo", "member", "user"),
        category = Category.INFO,
        description = "Get info about a guild member",
        usage = "[username: mention|string]",
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
        Utils.catchAll("Exception occured in user command", e.channel) {
            val member = Utils.convertMember(args.joinToString(" "), e)
                    ?: when {
                        e.message.mentionedMembers.isNotEmpty() -> e.message.mentionedMembers[0]
                        args.isEmpty() -> e.member
                        else -> return e.reply("No member was found with the username **${args.joinToString(" ")}**")
                    }
                    ?: return

            val mutualGuildCount = Jeanne.shardManager.getMutualGuilds(member.user).size
            val joinedDate = member.timeJoined.format(formatter)
            val memberRoles = member.roles.joinToString(", ") { it.name }
            val creationDate = member.user.timeCreated.format(formatter)
            e.reply(EmbedBuilder()
                    .setTitle("User info of ${member.effectiveName}", member.user.effectiveAvatarUrl)
                    .setThumbnail(member.user.effectiveAvatarUrl)
                    .addField("Username", member.user.name, true)
                    .addField("Nickname", member.nickname ?: "-", true)
                    .addField("Bot", if (member.user.isBot) "Yes" else "No", true)
                    .addField("Mutual servers", mutualGuildCount.toString(), true)
                    .addField("Status", member.onlineStatus.key, true)
                    .addField("Playing", if (member.activities.size >= 1) member.activities[0].name else "-", true)
                    .addField("Joined on", joinedDate, false)
                    .addField("Roles", memberRoles, false)
                    .setFooter("ID: ${member.user.id} | Created on: $creationDate", null))
        }
    }
}