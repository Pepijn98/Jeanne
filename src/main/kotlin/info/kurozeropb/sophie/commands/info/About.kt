package info.kurozeropb.sophie.commands.info

import info.kurozeropb.sophie.Sophie
import info.kurozeropb.sophie.commands.Command
import info.kurozeropb.sophie.utils.Utils
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.JDAInfo
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.MessageReceivedEvent

class About : Command(
        name = "about",
        category = "info",
        description = "Shows info about me",
        botPermissions = listOf(Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS)
) {

    override suspend fun execute(args: List<String>, e: MessageReceivedEvent) {
        Utils.catchAll("Exception occured in about command", e.channel) {
            val dev = e.jda.users.find { it.id == Sophie.config.developer }
            e.reply(EmbedBuilder()
                    .setTitle("${e.jda.selfUser.name} v${Sophie.config.version}")
                    .setThumbnail(e.jda.selfUser.effectiveAvatarUrl)
                    .addField("Developer", dev?.asMention, false)
                    .addField("Language", "Kotlin", true)
                    .addField("Library", "JDA ${JDAInfo.VERSION}", true)
                    .addField("Website", "https://sophiebot.info", true)
                    .addField("Support Server", "https://discord.gg/p895czC", true)
            )
        }
    }
}
