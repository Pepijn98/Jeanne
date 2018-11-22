package info.kurozeropb.sophie.commands.`fun`

import com.beust.klaxon.Klaxon
import info.kurozeropb.sophie.PokemonData
import info.kurozeropb.sophie.Sophie
import info.kurozeropb.sophie.commands.Command
import info.kurozeropb.sophie.core.HttpException
import info.kurozeropb.sophie.core.Utils
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import okhttp3.*
import java.io.IOException

// https://raw.githubusercontent.com/jalyna/oakdex-pokedex/master/data/pokemon.json
class Pokemon : Command(
        name = "pokemon",
        category = Category.FUN,
        cooldown = 20,
        description = "Search for a pokemon by name",
        usage = "<pokemon_name: string>",
        botPermissions = listOf(Permission.MESSAGE_WRITE)
) {

    override suspend fun execute(args: List<String>, e: MessageReceivedEvent) {
        if (args.isEmpty())
            return e.reply("Which pokemon do you want to search for?")

        val headers = mutableMapOf("Accept" to "application/json")
        headers.putAll(Sophie.defaultHeaders)
        val request = Request.Builder()
                .headers(Headers.of(headers))
                .url("https://raw.githubusercontent.com/jalyna/oakdex-pokedex/master/data/pokemon/${args.joinToString(" ").toLowerCase()}.json")
                .build()

        Sophie.httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, exception: IOException) {
                Utils.catchAll("Exception occured in pokemon command", e.channel) {
                    throw exception
                }
            }

            override fun onResponse(call: Call, response: Response) {
                Utils.catchAll("Exception occured in pokemon command\nCommand args: ${args.joinToString(" ")}", e.channel) {
                    val respstring = response.body()?.string()
                    val message = response.message()
                    val code = response.code()
                    response.close()

                    if (response.isSuccessful) {
                        if (respstring.isNullOrBlank())
                            return e.reply("Could not find a pokemon with the name **${args.joinToString(" ")}**")

                        val pokemon = Klaxon().parse<PokemonData>(respstring)
                        if (pokemon != null) {
                            e.reply(EmbedBuilder()
                                    .setTitle(pokemon.names.en)
                                    .setImage("https://img.pokemondb.net/artwork/${pokemon.names.en.toLowerCase().replace(Regex(" "), "-")}.jpg")
                                    .addField("Names", """
                                        **France:** ${pokemon.names.fr}
                                        **German:** ${pokemon.names.de}
                                        **Italian:** ${pokemon.names.it}
                                        **English:** ${pokemon.names.en}
                                    """.trimIndent(), true)
                                    .addField("Height/Weight", """
                                        **Height:** ${pokemon.height_eu} (${pokemon.height_us})
                                        **Weight:** ${pokemon.weight_eu} (${pokemon.weight_us})
                                    """.trimIndent(), true)
                                    .addField("Types", pokemon.types.joinToString("\n"), true)
                                    .addField("Gender ratios", """
                                        **Male:** ${pokemon.gender_ratios?.male ?: "-"}
                                        **Female:** ${pokemon.gender_ratios?.female ?: "-"}
                                    """.trimIndent(), true)
                                    .addField("Catch rate", pokemon.catch_rate.toString(), true)
                                    .addField("Egg groups", pokemon.egg_groups.joinToString("\n"), true)
                                    .addField("Hatch time", pokemon.hatch_time.joinToString("/"), true)
                                    .addField("Leveling rate", pokemon.leveling_rate, true)
                                    .addField("Evolutions", """
                                        **To:** ${if (pokemon.evolutions.isNotEmpty()) pokemon.evolutions[0].to else "-"}
                                        **At:** ${if (pokemon.evolutions.isNotEmpty()) pokemon.evolutions[0].level?.toString() ?: "-" else "-"}
                                    """.trimIndent(), true)
                                    .addField("Categories", pokemon.categories.en, true)
                                    .addField("National pokedex id", pokemon.national_id.toString(), true))
                        } else {
                            e.reply("Could not find a pokemon with the name **${args.joinToString(" ")}**")
                        }
                    } else {
                        throw HttpException(code, message)
                    }
                }
            }
        })
    }
}