package info.kurozeropb.jeanne.commands.`fun`

import com.github.kittinunf.result.Result
import info.kurozeropb.jeanne.Jeanne
import info.kurozeropb.jeanne.commands.Command
import info.kurozeropb.jeanne.core.Utils
import info.kurozeropb.jeanne.managers.DatabaseManager
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.awt.Color

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
            Jeanne.azurlane.getShipByName(args.joinToString(" ")).complete { result ->
                val (ship, exception) = result
                when (result) {
                    is Result.Success -> {
                        if (ship != null) {
                            e.reply(EmbedBuilder()
                                    .setTitle(ship.names.en)
                                    .setThumbnail(ship.thumbnail)
                                    .setDescription("cn: ${ship.names.cn}, jp: ${ship.names.jp}, kr: ${ship.names.kr}")
                                    .addField("Construction Time", ship.buildTime ?: "Unkown", true)
                                    .addField("Rarity", ship.rarity, true)
                                    .addField("Stars", ship.stars.value ?: "Unkown", true)
                                    .addField("Class", ship.`class` ?: "Unkown", true)
                                    .addField("Nationality", ship.nationality ?: "Unkown", true)
                                    .addField("Hull Type", ship.hullType, true))
                        } else {
                            e.reply("Oops, something went wrong! I didn't receive any info about this ship...")
                        }
                    }
                    is Result.Failure -> {
                        if (exception != null) {
                            e.reply(EmbedBuilder()
                                    .setTitle("${exception.statusCode} ${exception.statusMessage}")
                                    .setColor(0x990000)
                                    .setDescription(exception.message ?: "No further info about this error was provided")
                                    .build())
                        } else {
                            val db = DatabaseManager(e.guild)
                            val guild = db.getGuildData()
                            val dev = e.jda.getUserById(Jeanne.config.developer)
                            e.reply(EmbedBuilder()
                                    .setTitle("Request Failed")
                                    .setColor(0x990000)
                                    .setDescription("The request failed without any error message, try again in a couple minutes!\nIf this keeps happening you can contact ${dev?.asTag}\nUse `${guild?.prefix ?: Jeanne.config.prefix}support` to get an invite to the discord server")
                                    .build())
                        }
                    }
                }
            }
        }
    }
}