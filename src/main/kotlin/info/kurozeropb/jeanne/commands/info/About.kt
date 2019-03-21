package info.kurozeropb.jeanne.commands.info

import info.kurozeropb.jeanne.Jeanne
import info.kurozeropb.jeanne.commands.Command
import info.kurozeropb.jeanne.core.Utils
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.JDAInfo
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.MessageReceivedEvent

class About : Command(
        name = "about",
        category = Category.INFO,
        description = "Shows info about me",
        botPermissions = listOf(Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS)
) {

    override suspend fun execute(args: List<String>, e: MessageReceivedEvent) {
        Utils.catchAll("Exception occured in about command", e.channel) {
            val dev = e.jda.getUserById(Jeanne.config.developer)
            e.reply(EmbedBuilder()
                    .setTitle("${e.jda.selfUser.name} v${Jeanne.config.version}")
                    .setThumbnail(e.jda.selfUser.effectiveAvatarUrl)
                    .addField("Developer", dev?.asMention ?: "<@!${Jeanne.config.developer}>", false)
                    .addField("Language", "Kotlin v${KotlinVersion.CURRENT}", true)
                    .addField("Library", "JDA ${JDAInfo.VERSION}", true)
                    .addField("Website", "https://jeannebot.info", true)
                    .addField("Support Server", "https://discord.gg/p895czC", true)
            )
        }
    }
}
