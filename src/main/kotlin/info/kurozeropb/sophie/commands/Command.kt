package info.kurozeropb.sophie.commands

import info.kurozeropb.sophie.ExitStatus
import info.kurozeropb.sophie.utils.Utils
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import java.awt.Color
import java.io.File
import java.io.InputStream
import java.util.*
import java.util.function.Consumer
import java.util.logging.Logger
import kotlin.system.exitProcess

abstract class Command(
        val name: String,
        val category: String,
        val description: String,
        val usage: String? = null,
        val aliases: List<String> = listOf(),
        val subCommands: List<String> = listOf(),
        val cooldown: Long = 5,
        val allowPrivate: Boolean = true,
        val isDeveloperOnly: Boolean = false,
        val isHidden: Boolean = false,
        val userPermissions: List<Permission> = listOf(),
        val botPermissions: List<Permission> = listOf()
) : EventListener {

    init {
        register()
    }

    abstract suspend fun execute(args: List<String>, e: MessageReceivedEvent)

    private fun register() {
        try {
            Registry.registerCommand(this)
        } catch (e: Exception) {
            val logger = Logger.getGlobal()
            logger.warning(e.message)
            exitProcess(ExitStatus.DUPLICATE_COMMAND_NAME.code)
        }
    }

    fun <E> List<E>.random(random: java.util.Random): E = get(random.nextInt(size))

    fun String.toMessage(): Message = MessageBuilder().append(this).build()

    fun MessageReceivedEvent.embedColor(): Color = Utils(this).embedColor()

    fun MessageReceivedEvent.reply(msg: Message, success: Consumer<Message>? = null)
            = Utils(this).reply(msg, success)

    fun MessageReceivedEvent.reply(builder: EmbedBuilder, success: Consumer<Message>? = null)
            = Utils(this).reply(builder, success)

    fun MessageReceivedEvent.reply(text: String, success: Consumer<Message>? = null)
            = Utils(this).reply(text, success)

    fun MessageReceivedEvent.reply(data: InputStream, fileName: String, message: String? = null, success: Consumer<Message>? = null)
            = Utils(this).reply(data, fileName, message, success)

    fun MessageReceivedEvent.reply(file: File, fileName: String, message: String? = null, success: Consumer<Message>? = null)
            = Utils(this).reply(file, fileName, message, success)

    fun MessageReceivedEvent.reply(bytes: ByteArray, fileName: String, message: String? = null, success: Consumer<Message>? = null)
            = Utils(this).reply(bytes, fileName, message, success)
}
