package info.kurozeropb.jeanne.commands.info

import info.kurozeropb.jeanne.commands.Command
import info.kurozeropb.jeanne.core.Utils
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.MessageReceivedEvent

class Avatar : Command(
        name = "avatar",
        aliases = listOf("ava", "pfp", "avi"),
        category = Category.INFO,
        description = "Get your or someone else's avatar",
        usage = "[username: mention|string]",
        allowPrivate = false,
        botPermissions = listOf(Permission.MESSAGE_WRITE)
) {

    override suspend fun execute(args: List<String>, e: MessageReceivedEvent) {
        Utils.catchAll("Exception occured in avatar command", e.channel) {
            val member = Utils.convertMember(args.joinToString(" "), e) ?: e.member

            e.reply(EmbedBuilder()
                    .setDescription("${member.effectiveName}'s Avatar\n[Full image](${member.user.effectiveAvatarUrl})")
                    .setThumbnail(member.user.effectiveAvatarUrl))
        }
    }
}
