package info.kurozeropb.sophie.commands.moderation

import info.kurozeropb.sophie.commands.Command
import info.kurozeropb.sophie.core.Utils
import info.kurozeropb.sophie.core.isBannableBy
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.MessageReceivedEvent

class Ban : Command(
        name = "ban",
        category = Category.MODERATION,
        allowPrivate = false,
        description = "Ban a guild member",
        usage = "<member: mention> [days: number] [reason: string]",
        cooldown = 0,
        userPermissions = listOf(Permission.BAN_MEMBERS),
        botPermissions = listOf( Permission.MESSAGE_WRITE, Permission.BAN_MEMBERS)
) {

    override suspend fun execute(args: List<String>, e: MessageReceivedEvent) {
        Utils.catchAll("Exception occured in ban command", e.channel) {
            if (args.isEmpty())
                return

            if (e.message.mentionedMembers.isNotEmpty()) {
                if (args[0].matches("^<@!?\\d+>$".toRegex()).not()) {
                    e.reply("The member to ban needs to be the first argument")
                    return
                }

                var deleteDays = 0
                var reason = "No reason was provided"
                var reasonList = listOf<String>()

                if (args.size >= 2) {
                    try {
                        deleteDays = args[1].toInt()
                    } catch (exception: NumberFormatException) {
                        reasonList = args.subList(1, args.size)
                        reason = reasonList.joinToString(" ")
                    }
                }

                if (reasonList.isEmpty() && args.size >= 3) {
                    reasonList = args.subList(2, args.size)
                    reason = reasonList.joinToString(" ")
                }

                val mentionedMember = e.message.mentionedMembers[0]
                val isBannable = mentionedMember.isBannableBy(e.member)

                if (mentionedMember == e.member)
                    return e.reply("You can't ban yourself")

                if (isBannable.not())
                    return e.reply("I can't ban this member")

                e.guild.controller
                        .ban(mentionedMember, deleteDays)
                        .reason(reason)
                        .queue({
                            e.reply("Successfully banned ${mentionedMember.effectiveName}")
                        }, {
                            e.reply("Something went wrong while trying to ban ${mentionedMember.effectiveName}")
                        })
            }
        }
    }
}
