package info.kurozeropb.sophie.utils

import com.github.kittinunf.fuel.core.HttpException
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.entities.ChannelType
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.requests.RestAction
import kotlinx.coroutines.future.await
import info.kurozeropb.sophie.Sophie
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.MessageChannel
import net.dv8tion.jda.core.events.guild.GuildBanEvent
import net.dv8tion.jda.core.events.guild.GuildUnbanEvent
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent
import net.dv8tion.jda.core.events.guild.member.GuildMemberNickChangeEvent
import net.dv8tion.jda.webhook.WebhookClientBuilder
import net.dv8tion.jda.webhook.WebhookMessageBuilder
import java.awt.Color
import java.io.File
import java.io.InputStream
import java.lang.NumberFormatException
import java.util.function.Consumer
import java.util.logging.Logger

@Suppress("MemberVisibilityCanBePrivate")
class Utils(private val e: MessageReceivedEvent) {

    fun embedColor(): Color = e.guild.selfMember.color ?: Sophie.embedColor

    fun reply(msg: Message, success: Consumer<Message>? = null) {
        if (!e.isFromType(ChannelType.TEXT) || e.textChannel.canTalk()) {
            e.channel.sendMessage(stripEveryoneHere(msg)).queue(success)
        }
    }

    fun reply(builder: EmbedBuilder, success: Consumer<Message>? = null) {
        if (!e.isFromType(ChannelType.TEXT) || e.textChannel.canTalk()) {
            val embed = builder
                    .setColor(embedColor())
                    .build()
            e.channel.sendMessage(embed).queue(success)
        }
    }

    fun reply(data: InputStream, fileName: String, message: String? = null, success: Consumer<Message>? = null) {
        if (!e.isFromType(ChannelType.TEXT) || e.textChannel.canTalk()) {
            if (message != null)
                e.channel.sendFile(data, fileName, build(message)).queue(success)
            else
                e.channel.sendFile(data, fileName).queue(success)
        }
    }

    fun reply(file: File, fileName: String, message: String? = null, success: Consumer<Message>? = null) {
        if (!e.isFromType(ChannelType.TEXT) || e.textChannel.canTalk()) {
            if (message != null)
                e.channel.sendFile(file, fileName, build(message)).queue(success)
            else
                e.channel.sendFile(file, fileName).queue(success)
        }
    }

    fun reply(bytes: ByteArray, fileName: String, message: String? = null, success: Consumer<Message>? = null) {
        if (!e.isFromType(ChannelType.TEXT) || e.textChannel.canTalk()) {
            if (message != null)
                e.channel.sendFile(bytes, fileName, build(message)).queue(success)
            else
                e.channel.sendFile(bytes, fileName).queue(success)
        }
    }

    fun reply(text: String, success: Consumer<Message>? = null) {
        reply(build(text), success)
    }

    companion object {
        const val ZERO_WIDTH_SPACE = "\u200E"
        val userMentionPattern = Regex("<@!?(\\d{17,20})>")
        val channelMentionPattern = Regex("<#(\\d{17,20})>")
        val roleMentionPattern = Regex("<@&\\d{17,20}>")
        val emotePattern = Regex("<:.+?:(\\d{17,20})>")
        val userDiscrimPattern = Regex("(.{1,32})#(\\d{4})")
        val nullToNull = null to null

        fun edit(msg: Message, newContent: String) {
            if (!msg.isFromType(ChannelType.TEXT) || msg.textChannel.canTalk())
                msg.editMessage(newContent).queue()
        }

        fun build(o: Any): Message = MessageBuilder().append(o).build()

        private fun stripEveryoneHere(text: String): String = text.replace("@here", "@\u180Ehere")
                .replace("@everyone", "@\u180Eeveryone")

        fun stripEveryoneHere(msg: Message): Message = build(stripEveryoneHere(msg.contentRaw))

        fun stripFormatting(text: String): String = text.replace("@", "\\@")
                .replace("~~", "\\~\\~")
                .replace("*", "\\*")
                .replace("`", "\\`")
                .replace("_", "\\_")

        fun parseTime(milliseconds: Long): String {
            val seconds = milliseconds / 1000 % 60
            val minutes = milliseconds / (1000 * 60) % 60
            val hours = milliseconds / (1000 * 60 * 60) % 24
            val days = milliseconds / (1000 * 60 * 60 * 24)

            return String.format("%02d:%02d:%02d:%02d", days, hours, minutes, seconds)
        }

        // Code from: https://github.com/KawaiiBot/KawaiiBot/blob/master/src/main/kotlin/me/alexflipnote/kawaiibot/utils/Helpers.kt#L48
        fun splitText(content: String, limit: Int): Array<String> {
            val pages = ArrayList<String>()

            val lines = content.trim { it <= ' ' }.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            var chunk = StringBuilder()

            for (line in lines) {
                if (chunk.isNotEmpty() && chunk.length + line.length > limit) {
                    pages.add(chunk.toString())
                    chunk = StringBuilder()
                }

                if (line.length > limit) {
                    val lineChunks = line.length / limit

                    for (i in 0 until lineChunks) {
                        val start = limit * i
                        val end = start + limit
                        pages.add(line.substring(start, end))
                    }
                } else {
                    chunk.append(line).append("\n")
                }
            }

            if (chunk.isNotEmpty())
                pages.add(chunk.toString())

            return pages.toTypedArray()

        }

        // Code from: https://github.com/KawaiiBot/KawaiiBot/blob/master/src/main/kotlin/me/aurieh/ichigo/extensions/RestAction.kt
        suspend fun <T : Any> queueInOrder(actions: Collection<RestAction<T>>): List<T> {
            return actions.map { it.await() }
        }

        suspend fun <T> RestAction<T>.await(): T {
            return submit().await()
        }

        inline fun setInterval(millis: Long, action: () -> Unit) {
            while (true) {
                catchAll("Exception occured in interval func", null) {
                    Thread.sleep(millis)
                    action()
                }
            }
        }

        inline fun catchAll(message: String, channel: MessageChannel?, action: () -> Unit) {
            try {
                action()
            } catch (exception: Throwable) {
                when (exception) {
                    is HttpException -> {
                        val errorMessage = """```diff
                            |$message:
                            |- ${exception.message ?: "Unkown exception"}
                            |```""".trimMargin("|")
                        channel?.sendMessage(errorMessage)?.queue()
                    }
                    else -> {
                        val webhook = WebhookClientBuilder(Sophie.config.eWebhook).build()
                        val logger = Logger.getGlobal()
                        val errorMessage = """```diff
                            |$message:
                            |- ${exception.message ?: "Unkown exception"}
                            |```""".trimMargin("|")
                        channel?.sendMessage(errorMessage)?.queue()
                        val webhookMessage = WebhookMessageBuilder()
                                .setAvatarUrl(Sophie.shardManager.applicationInfo.jda.selfUser.effectiveAvatarUrl)
                                .setUsername(Sophie.shardManager.applicationInfo.jda.selfUser.name)
                                .setContent(errorMessage)
                                .build()
                        webhook.send(webhookMessage)
                        webhook.close()
                        logger.warning("$message > ${exception.message ?: "Unkown exception"}")
                    }
                }
            }
        }

        fun embedColor(e: GuildMemberJoinEvent): Color = e.guild.selfMember.color ?: Sophie.embedColor
        fun embedColor(e: GuildMemberLeaveEvent): Color = e.guild.selfMember.color ?: Sophie.embedColor
        fun embedColor(e: GuildBanEvent): Color = e.guild.selfMember.color ?: Sophie.embedColor
        fun embedColor(e: GuildUnbanEvent): Color = e.guild.selfMember.color ?: Sophie.embedColor
        fun embedColor(e: GuildMemberNickChangeEvent): Color = e.guild.selfMember.color ?: Sophie.embedColor

        fun convertMember(str: String, e: MessageReceivedEvent): Member? {
            if (str.isEmpty())
                return null

            val id = userMentionPattern.find(str)?.groups?.get(1)?.value ?: str
            val member = try {
                e.guild.getMemberById(id)
            } catch (e: NumberFormatException) {
                null
            }
            if (member != null)
                return member

            val (username, discrim) = convertUsernameDiscrim(str)
            return if (discrim != null)
                e.guild.members.find { it.user.name == username && it.user.discriminator == discrim }
            else
                e.guild.members.find { it.user.name == str }
        }

        fun convertUsernameDiscrim(str: String): Pair<String?, String?> {
            return userDiscrimPattern.find(str).let {
                if (it == null)
                    nullToNull
                else
                    it.groups[1]?.value to it.groups[2]?.value
            }
        }
    }
}
