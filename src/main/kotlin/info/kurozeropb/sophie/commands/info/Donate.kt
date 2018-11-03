package info.kurozeropb.sophie.commands.info

import info.kurozeropb.sophie.commands.Command
import info.kurozeropb.sophie.utils.Utils
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.MessageReceivedEvent

class Donate : Command(
        name = "donate",
        category = Category.INFO,
        description = "Get info about donating to the bot",
        botPermissions = listOf(Permission.MESSAGE_READ, Permission.MESSAGE_WRITE)
) {

    override suspend fun execute(args: List<String>, e: MessageReceivedEvent) {
        Utils.catchAll("Exception occured in donate command", e.channel) {
            e.reply("If you wish to donate there are three ways to do so,\n" +
                    "**PayPal:** <https://www.paypal.me/PvdBroek>\n" +
                    "**Patreon:** <https://www.patreon.com/Kurozero>\n" +
                    "**Donatebot** <https://donatebot.io/checkout/240059867744698368>" +
                    "\n" +
                    "To get access to the restricted commands please be in the support server, this is because the bot checks if you have the supporter role.\n" +
                    "If you donate using paypal make sure to leave a note with your discord id and/or username+discriminator\n" +
                    "because paypal is a one time only donation the minimum accepted amount is $10")
        }
    }
}