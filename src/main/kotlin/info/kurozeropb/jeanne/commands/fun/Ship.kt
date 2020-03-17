package info.kurozeropb.jeanne.commands.`fun`

import info.kurozeropb.jeanne.Jeanne
import info.kurozeropb.jeanne.commands.Command
import info.kurozeropb.jeanne.core.Utils
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

class Ship : Command(
        name = "ship",
        category = Category.FUN,
        description = "Find information about an azur lane ship",
        usage = "<name: string>",
        cooldown = 10,
        botPermissions = listOf(Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS)
) {
    override suspend fun execute(args: List<String>, e: MessageReceivedEvent) {
        Utils.catchAll("Exception occured in ship command", e.channel) {
            val ship = Jeanne.azurlane.getShipByName(args.joinToString(" "))
            e.reply(EmbedBuilder()
                    .setTitle(ship.names.en)
                    .setThumbnail(ship.thumbnail)
                    .setDescription("cn: ${ship.names.cn}, jp: ${ship.names.jp}, kr: ${ship.names.kr}")
                    .addField("Construction Time", ship.buildTime ?: "Unkown", true)
                    .addField("Rarity", ship.rarity, true)
                    .addField("Stars", ship.starts.value ?: "Unkown", true)
                    .addField("Class", ship.`class` ?: "Unkown", true)
                    .addField("Nationality", ship.nationality ?: "Unkown", true)
                    .addField("Hull Type", ship.hullType, true))
        }
    }
}