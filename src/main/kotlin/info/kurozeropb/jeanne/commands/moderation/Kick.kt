package info.kurozeropb.jeanne.commands.moderation

import info.kurozeropb.jeanne.commands.Command
import info.kurozeropb.jeanne.core.Utils
import info.kurozeropb.jeanne.core.isKickableBy
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

class Kick : Command(
        name = "kick",
        category = Category.MODERATION,
        allowPrivate = false,
        description = "Kick a guild member",
        usage = "<member: mention> [reason: string]",
        cooldown = 0,
        userPermissions = listOf(Permission.KICK_MEMBERS),
        botPermissions = listOf(Permission.MESSAGE_WRITE, Permission.KICK_MEMBERS)
) {

    override suspend fun execute(args: List<String>, e: MessageReceivedEvent) {
        Utils.catchAll("Exception occured in kick command", e.channel) {
            if (args.isEmpty())
                return

            if (e.message.mentionedMembers.isNotEmpty()) {
                if (args[0].matches("^<@!?\\d+>$".toRegex()).not()) {
                    e.reply("The member to kick needs to be the first argument")
                    return
                }

                var reason = "No reason was provided"
                if (args.size >= 2)
                    reason = args.subList(1, args.size).joinToString(" ")

                val mentionedMember = e.message.mentionedMembers[0]
                val isKickable = mentionedMember.isKickableBy(e.member)

                if (mentionedMember == e.member)
                    return e.reply("You can't kick yourself")

                if (isKickable.not())
                    return e.reply("I can't kick this member")

                e.guild
                        .kick(mentionedMember)
                        .reason(reason)
                        .queue({
                            e.reply("Successfully kicked ${mentionedMember.effectiveName}")
                        }, {
                            e.reply("Something went wrong while trying to kick ${mentionedMember.effectiveName}")
                        })
            }
        }
    }
}
