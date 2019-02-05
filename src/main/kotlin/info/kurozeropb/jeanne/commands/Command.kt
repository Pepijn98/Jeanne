package info.kurozeropb.jeanne.commands

import info.kurozeropb.jeanne.CommandData
import info.kurozeropb.jeanne.ExitStatus
import info.kurozeropb.jeanne.core.Utils
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
        val category: Category,
        val description: String,
        val example: String? = null,
        val nsfw: Boolean = false,
        val usage: String? = null,
        val aliases: List<String> = listOf(),
        val subCommands: List<String> = listOf(),
        val cooldown: Long = 5,
        val isDonatorsOnly: Boolean = false,
        val allowPrivate: Boolean = true,
        val isDeveloperOnly: Boolean = false,
        val isHidden: Boolean = false,
        val userPermissions: List<Permission> = listOf(),
        val botPermissions: List<Permission> = listOf()
) : EventListener {

    init {
        register()
    }

    enum class Category(val lower: String) {
        FUN("fun"),
        INFO("info"),
        MODERATION("moderation"),
        NSFW("nsfw"),
        OWNER("owner"),
        REACTIONS("reactions")
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

    fun asData(): CommandData
            = CommandData(name = this.name, category = this.category, description = this.description, usage = this.usage, aliases = this.aliases, subCommands = this.subCommands, cooldown = this.cooldown, isDonatorsOnly = this.isDonatorsOnly, allowPrivate = this.allowPrivate, isDeveloperOnly = this.isDeveloperOnly, isHidden = this.isHidden, userPermissions = this.userPermissions, botPermissions = this.botPermissions)
}
