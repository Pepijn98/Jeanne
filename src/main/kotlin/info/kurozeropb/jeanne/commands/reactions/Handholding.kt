package info.kurozeropb.jeanne.commands.reactions

import info.kurozeropb.jeanne.core.HttpException
import com.github.natanbc.weeb4j.image.HiddenMode
import com.github.natanbc.weeb4j.image.NsfwFilter
import info.kurozeropb.jeanne.Jeanne
import info.kurozeropb.jeanne.commands.Command
import info.kurozeropb.jeanne.core.Utils
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import okhttp3.Headers
import okhttp3.Request

class Handholding : Command(
        name = "handholding",
        category = Category.REACTIONS,
        description = "Wholesome handholding images",
        allowPrivate = false,
        botPermissions = listOf(Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_ATTACH_FILES)
) {

    override suspend fun execute(args: List<String>, e: MessageReceivedEvent) {
        Utils.catchAll("Exception occured in ${this.name} command", e.channel) {
            Jeanne.weebApi.imageProvider.getRandomImage(this.name, HiddenMode.DEFAULT, NsfwFilter.NO_NSFW).async({ image ->
                val headers = mutableMapOf("Accept" to "image/*")
                headers.putAll(Jeanne.defaultHeaders)
                val request = Request.Builder()
                        .headers(Headers.of(headers))
                        .url(image.url)
                        .build()

                val member = (if (e.message.mentionedMembers.size > 0) e.message.mentionedMembers[0] else Utils.convertMember(args.joinToString(" "), e))
                        ?: return@async e.reply("Could not find a member with the name **${args.joinToString(" ")}**")

                val resp = Jeanne.httpClient.newCall(request).execute()
                if (resp.isSuccessful) {
                    val body = resp.body()
                    if (body != null)
                        e.reply(body.byteStream(), "${image.id}.${image.fileType.name.toLowerCase()}", "**${e.member?.effectiveName}** holds **${member.effectiveName}**'s hand")
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