package info.kurozeropb.sophie.commands.`fun`

import info.kurozeropb.sophie.ProgramO
import info.kurozeropb.sophie.QuestionCache
import info.kurozeropb.sophie.Sophie
import info.kurozeropb.sophie.commands.Command
import info.kurozeropb.sophie.utils.Utils
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import okhttp3.*
import java.io.IOException

class Cleverbot : Command(
        name = "cleverbot",
        aliases = listOf("cb"),
        category = Category.FUN,
        description = "Chat with the bot",
        usage = "<question: string>",
        botPermissions = listOf(Permission.MESSAGE_WRITE)
) {

    private val questionCache: ArrayList<QuestionCache> = arrayListOf()
    private fun spamCheck(userId: String, question: String): Boolean {
        val value = questionCache.find { it.id == userId }

        if (value == null) {
            questionCache.add(QuestionCache(userId, question))
            return true
        }
        if (value.question == question)
            return false

        value.question = question
        return true
    }

    override suspend fun execute(args: List<String>, e: MessageReceivedEvent) {
        Utils.catchAll("Exception occured in cleverbot command", e.channel) {
            if (args.isEmpty())
                return e.reply("**${e.member.effectiveName}**, What do you want to talk about?")

            val question = args.joinToString(" ")
            val user = e.author
            val spamCheck = spamCheck(user.id, question)

            if (spamCheck) {
                e.channel.sendTyping().queue()

                val owner = e.jda.getUserById(Sophie.config.developer)
                val headers = mutableMapOf("Accept" to "application/json")
                headers.putAll(Sophie.defaultHeaders)
                val request = Request.Builder()
                        .headers(Headers.of(headers))
                        .url("http://api.program-o.com/v2/chatbot/?bot_id=12&say=$question&convo_id=${user.id}&format=json")
                        .build()

                Sophie.httpClient.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, exception: IOException) {
                        throw exception
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val respstring = response.body()?.string()
                        if (response.isSuccessful && respstring != null) {
                            val resp = ProgramO.Deserializer().deserialize(respstring)
                            if (resp != null) {
                                val reply = resp.botsay
                                        .replace("Chatmundo", e.jda.selfUser.name, true)
                                        .replace("<br/> ", "\n", true)
                                        .replace("<br/>", "\n", true)
                                        .replace("Elizabeth", "${owner.name}#${owner.discriminator}", true)
                                        .replace("elizaibeth", "${owner.name}#${owner.discriminator}", true)

                                e.reply("**${e.member.effectiveName}**, $reply")
                            } else {
                                e.reply("Something went wrong while deserializing the response string")
                            }
                        } else {
                            e.reply("Something went wrong while trying to get a response")
                        }
                    }
                })
            }
        }
    }
}
