package info.kurozeropb.sophie.commands.`fun`

import info.kurozeropb.sophie.PokemonData
import info.kurozeropb.sophie.Sophie
import info.kurozeropb.sophie.commands.Command
import info.kurozeropb.sophie.utils.Utils
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import okhttp3.*
import java.io.IOException

// https://raw.githubusercontent.com/jalyna/oakdex-pokedex/master/data/pokemon.json
class Pokemon : Command(
        name = "pokemon",
        category = "fun",
        cooldown = 20,
        description = "Search for a pokemon by name",
        botPermissions = listOf(Permission.MESSAGE_WRITE)
) {

    override suspend fun execute(args: List<String>, e: MessageReceivedEvent) {
        Utils.catchAll("Exception occured in 8ball command", e.channel) {
            if (args.isEmpty())
                return e.reply("Which pokemon do you want to search for?")

            val headers = Sophie.defaultHeaders
            headers.putAll(mapOf("Accept" to "application/json"))
            val request = Request.Builder()
                    .headers(Headers.of(headers))
                    .url("https://raw.githubusercontent.com/jalyna/oakdex-pokedex/master/data/pokemon/${args.joinToString(" ").toLowerCase()}.json")
                    .build()

            Sophie.httpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, exception: IOException) {
                    throw exception
                }

                override fun onResponse(call: Call, response: Response) {
                    val respstring = response.body()?.string()
                    if (response.isSuccessful && respstring != null) {
                        val pokemon = PokemonData.Deserializer().deserialize(respstring)
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
                                        **Male:** ${pokemon.gender_ratios.male}
                                        **Female:** ${pokemon.gender_ratios.female}
                                    """.trimIndent(), true)
                                    .addField("Catch rate", pokemon.catch_rate, true)
                                    .addField("Egg groups", pokemon.egg_groups.joinToString("\n"), true)
                                    .addField("Hatch time", pokemon.hatch_time.joinToString("/"), true)
                                    .addField("Leveling rate", pokemon.leveling_rate, true)
                                    .addField("Evolutions", """
                                        **To:** ${if (pokemon.evolutions.isNotEmpty()) pokemon.evolutions[0].to else null}
                                        **At:** ${if (pokemon.evolutions.isNotEmpty()) pokemon.evolutions[0].level else null}
                                    """.trimIndent(), true)
                                    .addField("Categories", pokemon.categories.en, true)
                                    .addField("National pokedex id", pokemon.national_id.toString(), true))
                        } else {
                            e.reply("Something went wrong while deserializing the response string")
                        }
                    } else {
                        e.reply("Something went wrong while fetching the pokemon data, most likely **${args.joinToString(" ")}** isn't a valid name.")
                    }
                }
            })
        }
    }
}