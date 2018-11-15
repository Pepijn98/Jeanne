package info.kurozeropb.sophie.commands.reactions

import info.kurozeropb.sophie.core.HttpException
import com.github.natanbc.weeb4j.image.HiddenMode
import com.github.natanbc.weeb4j.image.NsfwFilter
import info.kurozeropb.sophie.Sophie
import info.kurozeropb.sophie.commands.Command
import info.kurozeropb.sophie.core.Utils
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import okhttp3.Headers
import okhttp3.Request

class Cuddle : Command(
        name = "cuddle",
        category = Category.REACTIONS,
        description = "Wholesome cuddle images",
        allowPrivate = false,
        botPermissions = listOf(Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_ATTACH_FILES)
) {

    override suspend fun execute(args: List<String>, e: MessageReceivedEvent) {
        Utils.catchAll("Exception occured in ${this.name} command", e.channel) {
            Sophie.weebApi.imageProvider.getRandomImage(this.name, HiddenMode.DEFAULT, NsfwFilter.NO_NSFW).async({ image ->
                val headers = mutableMapOf("Accept" to "image/*")
                headers.putAll(Sophie.defaultHeaders)
                val request = Request.Builder()
                        .headers(Headers.of(headers))
                        .url(image.url)
                        .build()

                val member = (if (e.message.mentionedMembers.size > 0) e.message.mentionedMembers[0] else Utils.convertMember(args.joinToString(" "), e))
                        ?: return@async e.reply("Could not find a member with the name **${args.joinToString(" ")}**")

                val resp = Sophie.httpClient.newCall(request).execute()
                if (resp.isSuccessful) {
                    val body = resp.body()
                    if (body != null)
                        e.reply(body.byteStream(), "${image.id}.${image.fileType.name.toLowerCase()}", "**${e.member.effectiveName}** cuddles with **${member.effectiveName}**")
                    else
                        e.reply("Something went wrong while trying to fetch the image")
                } else {
                    throw HttpException(resp.code(), resp.message())
                }
            }, { exception ->
                e.reply(exception.message ?: "Unkown exception")
            })
        }
    }
}