package info.kurozeropb.sophie.commands.moderation

import info.kurozeropb.sophie.commands.Command
import info.kurozeropb.sophie.utils.Utils
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.MessageReceivedEvent

class Kick : Command(
        name = "kick",
        category = "moderation",
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

                e.guild.controller
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
