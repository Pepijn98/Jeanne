package dev.vdbroek.jeanne.commands.`fun`

import com.beust.klaxon.Klaxon
import dev.vdbroek.jeanne.Jeanne
import dev.vdbroek.jeanne.commands.Command
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import okhttp3.Request
import org.w3c.dom.Element
import org.w3c.dom.Node
import javax.xml.parsers.DocumentBuilderFactory

data class Item(val link: String)
data class Image(val large: String, val full: String)

// TODO : Fix blocking method
@Suppress("BlockingMethodInNonBlockingContext")
class Zerochan : Command(
    name = "zerochan",
    category = Category.FUN,
    description = "Get a random image from zerochan using a tag",
    usage = "<tag>",
    cooldown = 15,
    botPermissions = listOf(Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS)
) {

    override suspend fun execute(args: List<String>, e: MessageReceivedEvent) {
        try {
            var xmlRequest = Request.Builder().url("https://www.zerochan.net/${args.joinToString("+") { it.capitalize() }}?xml=true").build()
            var xmlResponse = Jeanne.httpClient.newCall(xmlRequest).execute()

            val xmlUrl = xmlResponse.request().url().toString()
            if (xmlUrl.contains("?xml=true").not()) {
                xmlResponse.close()
                xmlRequest = Request.Builder().url("$xmlUrl?xml=true").build()
                xmlResponse = Jeanne.httpClient.newCall(xmlRequest).execute()
            }

            val xmlBody = xmlResponse.body() ?: return e.reply("Could not find an image for **${args.joinToString(" ")}**")

            val xmlDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlBody.byteStream())
            xmlDoc.documentElement.normalize()
            val xmlItems = xmlDoc.getElementsByTagName("item")
            val items: MutableList<Item> = mutableListOf()
            for (i in 0 until xmlItems.length) {
                val item = xmlItems.item(i)
                if (item.nodeType == Node.ELEMENT_NODE) {
                    val element = item as Element
                    items.add(Item(element.getElementsByTagName("link").item(0).textContent))
                }
            }
            if (items.isNullOrEmpty())
                return e.reply("Could not find an image for **${args.joinToString(" ")}**")

            val item = items.random()
            val jsonRequest = Request.Builder().url("${item.link}?json=true").build()
            val jsonResponse = Jeanne.httpClient.newCall(jsonRequest).execute()
            val jsonBody = jsonResponse.body() ?: return e.reply("Could not find an image for **${args.joinToString(" ")}**")
            val image = Klaxon().parse<Image>(jsonBody.byteStream()) ?: return e.reply("Could not find an image for **${args.joinToString(" ")}**")
            e.reply(
                EmbedBuilder()
                    .setDescription("[Open post](${item.link}) | [Full image](${image.full})")
                    .setImage(image.large)
            )
        } catch (t: Throwable) {
            e.reply("Could not find an image for **${args.joinToString(" ")}**")
        }
    }
}