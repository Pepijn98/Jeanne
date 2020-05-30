package info.kurozeropb.jeanne.commands.info

import info.kurozeropb.jeanne.commands.Command
import info.kurozeropb.jeanne.core.Utils
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

class Support : Command(
        name = "support",
        category = Category.INFO,
        description = "Sends you an invite to the support discord server",
        botPermissions = listOf(Permission.MESSAGE_WRITE)
) {

    override suspend fun execute(args: List<String>, e: MessageReceivedEvent) {
        Utils.catchAll("Exception occured in support command", e.channel) {
            e.author.openPrivateChannel().queue { channel ->
                channel.sendMessage("To ask a question about me or get support join: https://discord.gg/p895czC\nGo to the appropriate channel and ask your question!").queue({
                    e.reply("✅ | Invite has been send to your DMs")
                }, {
                    e.reply("❌ | Could not send invite, please open your DMs (this is to prevent the bot from sending the invite in servers were this is not allowed)")
                })
            }
        }
    }
}
