package info.kurozeropb.jeanne.commands.`fun`

import com.beust.klaxon.Klaxon
import info.kurozeropb.jeanne.Jeanne
import info.kurozeropb.jeanne.commands.Command
import info.kurozeropb.jeanne.core.AzurLane
import info.kurozeropb.jeanne.core.HttpException
import info.kurozeropb.jeanne.core.Utils
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import okhttp3.*
import java.io.IOException

class Ship : Command(
        name = "ship",
        category = Category.FUN,
        description = "Find information about an azur lane ship",
        usage = "<name: string>",
        cooldown = 10,
        botPermissions = listOf(Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS)
) {
    override suspend fun execute(args: List<String>, e: MessageReceivedEvent) {
        val name = args.joinToString(" ")
        val headers = mutableMapOf(
                "Content-Type" to "application/json",
                "Accept" to "application/json"
        )
        headers.putAll(Jeanne.defaultHeaders)
        val request = Request.Builder()
                .headers(Headers.of(headers))
                .url("${AzurLane.baseUrl}/ship?name=$name")
                .build()

        Jeanne.httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, exception: IOException) {
                Utils.catchAll("Exception occured in ship command", e.channel) {
                    throw exception
                }
            }

            override fun onResponse(call: Call, response: Response) {
                Utils.catchAll("Exception occured in ship command", e.channel) {
                    val respstring = response.body()?.string()
                    val message = response.message()
                    val code = response.code()
                    response.close()

                    if (response.isSuccessful) {
                        if (respstring.isNullOrBlank())
                            return e.reply("Could not find a ship with the name **$name**")

                        val alResponse: AzurLane.ShipResponse?
                        try {
                            alResponse = Klaxon().parse<AzurLane.ShipResponse>(respstring)
                        } catch (exception: Exception) {
                            return e.reply("Something went wrong while parsing the ship data.")
                        }

                        if (alResponse != null) {
                            val ship = alResponse.ship
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
                            e.reply("Could not find a ship with the name **$name**")
                        }
                    } else {
                        throw HttpException(code, message)
                    }
                }
            }
        })
    }
}