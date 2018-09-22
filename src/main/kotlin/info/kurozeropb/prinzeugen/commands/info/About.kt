package info.kurozeropb.prinzeugen.commands.info

import info.kurozeropb.prinzeugen.Prinz
import info.kurozeropb.prinzeugen.commands.Command
import info.kurozeropb.prinzeugen.utils.Utils
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.JDAInfo
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.MessageReceivedEvent

class About : Command(
        name = "about",
        category = "info",
        description = "Shows info about me",
        botPermissions = listOf(
                Permission.MESSAGE_WRITE,
                Permission.MESSAGE_EMBED_LINKS
        )
) {

    override suspend fun execute(args: List<String>, e: MessageReceivedEvent) {
        Utils.catchAll("Exception occured in about command", e.channel) {
            val me = e.jda.users.find { it.id == Prinz.config.developer }
            e.reply(EmbedBuilder()
                    .setTitle("${e.jda.selfUser.name} v${Prinz.config.version}")
                    .addField("Developer", me?.asMention, false)
                    .addField("Language", "Kotlin", true)
                    .addField("Library", "JDA ${JDAInfo.VERSION}", true)
                    .addField("Website", "https://prinz-eugen.info", true)
                    .addField("Support Server", "https://discord.gg/p895czC", true)
                    .setThumbnail(e.jda.selfUser.effectiveAvatarUrl)
            )
        }
    }
}
