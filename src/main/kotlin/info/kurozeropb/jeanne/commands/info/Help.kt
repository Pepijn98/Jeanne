package info.kurozeropb.jeanne.commands.info

import info.kurozeropb.jeanne.Jeanne
import info.kurozeropb.jeanne.commands.Command
import info.kurozeropb.jeanne.commands.Registry
import info.kurozeropb.jeanne.managers.DatabaseManager
import info.kurozeropb.jeanne.core.Utils
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.lang.StringBuilder

class Help : Command(
        name = "help",
        category = Category.INFO,
        cooldown = 2,
        description = "Shows this help message",
        allowPrivate = false,
        usage = "[<\"cmd/command\"|\"ctg/category\"> <command_name|category_name: string>]",
        subCommands = listOf("command", "cmd", "category", "ctg"),
        botPermissions = listOf(Permission.MESSAGE_WRITE)
) {

    override suspend fun execute(args: List<String>, e: MessageReceivedEvent) {
        Utils.catchAll("Exception occured in help command", e.channel) {
            var prefix = DatabaseManager.guildPrefixes[e.guild.id] ?: Jeanne.config.prefix
            if (prefix == "%mention%")
                prefix = e.jda.selfUser.asMention

            when (args.size) {
                0 -> {
                    val commandBuilder = StringBuilder()
                    commandBuilder.append("# Categories\n\n")
                    val categories = Registry.commands.map { it.category.lower }
                    categories.asSequence().distinct().sorted().toList().forEach { category -> commandBuilder.append("- $category\n") }
                    commandBuilder.append("\n> Use ${prefix}help category <category_name>")
                    commandBuilder.append("\n> Or ${prefix}help ctg <category_name>")

                    val messages = Utils.splitText(commandBuilder.toString(), 1900)
                    val actions = messages.map { e.channel.sendMessage("```md\n$it```") }
                    Utils.queueInOrder(actions)
                }
                1 -> e.reply("Please tell me the category/command name to search")
                else -> {
                    when (args[0]) {
                        "category", "ctg" -> {
                            val category = args.subList(1, args.size).joinToString(" ")
                            val commands = Registry.commands
                                    .asSequence()
                                    .filter { it.category.lower == category }
                                    .sortedWith(compareBy(Command::name, Command::cooldown))
                                    .toList()

                            if (commands.isNotEmpty()) {
                                val commandBuilder = StringBuilder()
                                commandBuilder.append("# Commands for the category $category\n\n")
                                commands.forEach { cmd ->
                                    if ((cmd.isHidden || cmd.isDeveloperOnly) && e.author.id != Jeanne.config.developer)
                                        return@forEach

                                    commandBuilder.append("- ${cmd.name}\n")
                                    commandBuilder.append("     - ${cmd.description}\n")
                                }
                                commandBuilder.append("\n> Use ${prefix}help command <command_name>")
                                commandBuilder.append("\n> Or ${prefix}help cmd <command_name>")

                                val messages = Utils.splitText(commandBuilder.toString(), 1900)
                                val actions = messages.map { e.channel.sendMessage("```md\n$it```") }
                                Utils.queueInOrder(actions)
                            } else {
                                e.reply("No commands found for the category **$category**")
                            }
                        }
                        "command", "cmd" -> {
                            val cmdName = args.subList(1, args.size).joinToString(" ")
                            val command = Registry.getCommandByName(cmdName)
                            if (command != null) {
                                if (command.isHidden && e.author.id != Jeanne.config.developer)
                                    return e.reply("This command is hidden and can't be shown by any help command")

                                if (command.isDeveloperOnly && e.author.id != Jeanne.config.developer)
                                    return e.reply("This command is developer only and can only be seen by my developer")

                                val sb = StringBuilder()
                                sb.append("```md\n")
                                sb.append("# ${command.name}\n")
                                sb.append("> ${command.description}\n\n")
                                sb.append("- Aliases           ->   ${command.aliases.joinToString(", ")}\n")
                                sb.append("- Sub Commands      ->   ${command.subCommands.joinToString(", ")}\n")
                                if (command.usage != null)
                                    sb.append("- Usage             ->   $prefix${command.name} ${command.usage}\n")
                                sb.append("- Category          ->   ${command.category.lower}\n")
                                sb.append("- Allow Private     ->   ${if (command.allowPrivate) "Yes" else "No"}\n")
                                sb.append("- Developer Only    ->   ${if (command.isDeveloperOnly) "Yes" else "No"}\n")
                                sb.append("- User permissions  ->   ${command.userPermissions.joinToString(", ")}\n")
                                sb.append("- Bot permissions   ->   ${command.botPermissions.joinToString(", ")}\n\n")
                                sb.append("<> = required\n")
                                sb.append("[] = optional\n")
                                sb.append("```")
                                e.reply(sb.toString())
                            } else {
                                e.reply("No command with the name **$cmdName** exists")
                            }
                        }
                    }
                }
            }
        }
    }
}
