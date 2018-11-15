package info.kurozeropb.sophie.commands.moderation

import info.kurozeropb.sophie.Guild
import info.kurozeropb.sophie.Sophie
import info.kurozeropb.sophie.commands.Command
import info.kurozeropb.sophie.managers.DatabaseManager
import info.kurozeropb.sophie.core.Utils
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import org.litote.kmongo.SetTo
import org.litote.kmongo.eq
import org.litote.kmongo.set

@Suppress("unused")
class Settings : Command(
        name = "settings",
        category = Category.MODERATION,
        description = "Setup the settings for the current guild",
        allowPrivate = false,
        usage = "(Full usage at https://sophiebot.info/settings)",
        userPermissions = listOf(Permission.ADMINISTRATOR),
        botPermissions = listOf(Permission.MESSAGE_WRITE)
) {

    override suspend fun execute(args: List<String>, e: MessageReceivedEvent) {
        Utils.catchAll("Exception occured in settings command", e.channel) {
            if (args.isEmpty())
                return

            when (args[0]) {
                "prefix" -> {
                    val dbManager = DatabaseManager(e.guild)
                    val guild = dbManager.getGuildData()

                    val prefixArgs = args.subList(1, args.size)

                    if (prefixArgs.isEmpty()) {
                        var prefix = guild?.prefix ?: Sophie.config.prefix
                        if (prefix == "%mention%")
                            prefix = e.jda.selfUser.asMention
                        e.reply("Current prefix: **$prefix**")
                        return
                    }

                    var newPrefix = prefixArgs.joinToString(" ").replace("\\$$".toRegex(), "")

                    if (newPrefix == "reset" || newPrefix == "default")
                        newPrefix = Sophie.config.prefix

                    if (newPrefix.matches("^<@!?${e.jda.selfUser.id}>$".toRegex())) {
                        newPrefix = "%mention%"
                    } else if (newPrefix.length > 32) {
                        e.reply("That prefix is too long! Max length: **32**")
                        return
                    }

                    if (guild != null) {
                        DatabaseManager.guildPrefixes[e.guild.id] = newPrefix
                        DatabaseManager.guilds.updateOne(Guild::id eq e.guild.id, set(Guild::prefix, newPrefix))
                        var prefix = newPrefix
                        if (prefix == "%mention%")
                            prefix = e.jda.selfUser.asMention
                        e.reply("Prefix has been set to **$prefix**")
                    } else {
                        DatabaseManager.guildPrefixes[e.guild.id] = newPrefix
                        DatabaseManager.guilds.insertOne(Guild(e.guild.id, newPrefix))
                        var prefix = newPrefix
                        if (prefix == "%mention%")
                            prefix = e.jda.selfUser.asMention
                        e.reply("Prefix has been set to **$prefix**")
                    }
                }
                "welcome" -> {
                    val welcomeArgs = args.subList(1, args.size)

                    if (welcomeArgs.isEmpty()) {
                        e.reply("Please specify a welcome channel and welcome message")
                        return
                    }

                    val dbManager = DatabaseManager(e.guild)
                    val guild = dbManager.getGuildData()
                    val noEntry = guild == null

                    if (welcomeArgs[0] == "disable") {
                        if (noEntry || !guild!!.welcomeEnabled) {
                            e.reply("Welcome message is already disabled")
                            return
                        }

                        DatabaseManager.guilds.updateOne(Guild::id eq e.guild.id, set(Guild::welcomeEnabled, false))
                        e.reply("Welcome message has been disabled")
                    } else {
                        if (welcomeArgs.size == 1) {
                            e.reply("Please also specify a welcome message")
                            return
                        }

                        if (e.message.mentionedChannels.size < 1 || !welcomeArgs[0].matches("""^<#\d{17,18}>$""".toRegex())) {
                            e.reply("The first argument should be a channel mention")
                            return
                        }

                        val message = welcomeArgs.subList(1, welcomeArgs.size).joinToString(" ")
                        val channel = e.message.mentionedChannels[0]

                        if (message.isEmpty() || message.length > 1500) {
                            e.reply("Welcome message should be between 1 and 1500 characters, current amount is **${message.length}**")
                            return
                        }

                        if (noEntry) {
                            DatabaseManager.guilds.insertOne(Guild(e.guild.id,
                                    welcomeMessage = message,
                                    welcomeEnabled = true,
                                    welcomeChannel = channel.id
                            ))
                            e.reply("Welcome message has been enabled, current welcome message is: `$message`")
                            return
                        }

                        if (guild!!.welcomeEnabled && guild.welcomeMessage == message && channel.id == guild.welcomeChannel) {
                            e.reply("Welcome message is already enabled with the exact same message and the same channel")
                            return
                        }

                        DatabaseManager.guilds.updateOne(
                                Guild::id eq e.guild.id,
                                set(SetTo(Guild::welcomeEnabled, true), SetTo(Guild::welcomeChannel, channel.id), SetTo(Guild::welcomeMessage, message))
                        )
                        e.reply("Welcome message has been enabled, current welcome message is: `$message`")
                    }
                }
                "level" -> {
                    val levelArgs = args.subList(1, args.size)

                    if (levelArgs.isEmpty()) {
                        e.reply("Please specify whether to enable or disable level up messages")
                        return
                    }

                    val dbManager = DatabaseManager(e.guild)
                    val guild = dbManager.getGuildData()
                    val noEntry = guild == null

                    if (levelArgs[0] == "disable") {
                        if (noEntry || !guild!!.levelupEnabled) {
                            e.reply("Level up message is already disabled")
                            return
                        }

                        DatabaseManager.guilds.updateOne(
                                Guild::id eq e.guild.id,
                                set(SetTo(Guild::levelupEnabled, false), SetTo(Guild::levelupMessage, ""))
                        )
                        e.reply("Level up message has been disabled")
                    } else {
                        if (levelArgs.size == 1) {
                            e.reply("Please also specify a level up message")
                            return
                        }

                        if (levelArgs[0] != "enable") {
                            e.reply("The first argument should be \"enable\" to enable the level up message")
                            return
                        }

                        val message = levelArgs.subList(1, levelArgs.size).joinToString(" ")

                        if (message.isEmpty() || message.length > 1500) {
                            e.reply("Level up message should be between 1 and 1500 characters, current amount is **${message.length}**")
                            return
                        }

                        if (noEntry) {
                            DatabaseManager.guilds.insertOne(Guild(e.guild.id,
                                    levelupMessage = message,
                                    levelupEnabled = true
                            ))
                            e.reply("Leveling is enabled, level up message has been set to: **$message**")
                            return
                        }

                        if (guild!!.levelupEnabled && guild.levelupMessage == message) {
                            e.reply("Leveling is already enabled with the exact same level up message")
                            return
                        }

                        DatabaseManager.guilds.updateOne(
                                Guild::id eq e.guild.id,
                                set(SetTo(Guild::levelupEnabled, true), SetTo(Guild::levelupMessage, message))
                        )
                        e.reply("Leveling is enabled, level up message has been set to: **$message**")
                    }
                }
                "events" -> {
                    val eventArgs = args.subList(1, args.size)

                    if (eventArgs.isEmpty()) {
                        e.reply("Please specify whether to enable or disable events")
                        return
                    }

                    val dbManager = DatabaseManager(e.guild)
                    val guild = dbManager.getGuildData()
                    val noEntry = guild == null

                    when (eventArgs[0]) {
                        "setchannel", "channel", "chan" -> {
                            if (e.message.mentionedChannels.isNotEmpty()) {
                                if (noEntry) {
                                    DatabaseManager.guilds.insertOne(Guild(e.guild.id, logChannel = e.message.mentionedChannels[0].id))
                                } else {
                                    DatabaseManager.guilds.updateOne(Guild::id eq e.guild.id, set(Guild::logChannel, e.message.mentionedChannels[0].id))
                                }
                                e.reply("Set the log channel to **${e.message.mentionedChannels[0].name}**")
                            } else {
                                e.reply("Please mention the channel you want events to be logged in")
                            }
                        }
                        "enable", "+" -> {
                            val events = eventArgs.subList(1, eventArgs.size)

                            if (events.isEmpty()) {
                                e.reply("Please specify which events to enable")
                                return
                            }

                            if (noEntry) {
                                DatabaseManager.guilds.insertOne(Guild(e.guild.id, subbedEvents = ArrayList(events)))
                            } else {
                                guild!!.subbedEvents.addAll(events)
                                DatabaseManager.guilds.updateOne(Guild::id eq e.guild.id, set(Guild::subbedEvents, guild.subbedEvents))
                            }
                            e.reply("Subbed to the events: **${events.joinToString(", ")}**")
                        }
                        "disable", "-" -> {
                            if (noEntry) {
                                e.reply("Not subbed to any events yet")
                                return
                            }

                            val events = eventArgs.subList(1, eventArgs.size)

                            if (events.isEmpty()) {
                                e.reply("Please specify which events to disable")
                                return
                            }

                            guild!!.subbedEvents.removeAll(events)
                            DatabaseManager.guilds.updateOne(Guild::id eq e.guild.id, set(Guild::subbedEvents, guild.subbedEvents))
                            e.reply("Unsubbed to the events: **${events.joinToString(", ")}**")
                        }
                    }
                }
                "commands" -> {
                    // TODO: Ignore/unignore certain commands
                    e.reply("Will be added in a future update")
                }
                "check" -> {
                    val checkArgs = args.subList(1, args.size)

                    if (checkArgs.isEmpty()) {
                        e.reply("Please specify a setting to check, visit the website (https://sophiebot.info/settings) to see which settings you can check")
                        return
                    }

                    val dbManager = DatabaseManager(e.guild)
                    val guild = dbManager.getGuildData()
                    val noEntry = guild == null

                    when(checkArgs[0]) {
                        "prefix" -> {
                            var prefix = guild?.prefix ?: Sophie.config.prefix
                            if (prefix == "%mention%")
                                prefix = e.jda.selfUser.asMention
                            e.reply("Current prefix: **$prefix**")
                        }
                        "welcome" -> {
                            if (noEntry || guild!!.welcomeEnabled.not()) {
                                e.reply("Welcome message is not enabled")
                            } else {
                                e.reply("""
                                    Enabled: **${guild.welcomeEnabled}**
                                    Channel: **<#${guild.welcomeChannel}>**
                                    Message: `${guild.welcomeMessage}`
                                """.trimIndent())
                            }
                        }
                        "level" -> {
                            if (noEntry || guild!!.levelupEnabled.not()) {
                                e.reply("Levelup message is not enabled")
                            } else {
                                e.reply("""
                                    Enabled: **${guild.levelupEnabled}**
                                    Message: `${guild.levelupMessage}`
                                """.trimIndent())
                            }
                        }
                        "events" -> {
                            if (noEntry || guild!!.subbedEvents.isEmpty()) {
                                e.reply("This guild is not subbed to any events")
                            } else {
                                e.reply("""
                                    Channel: **<#${guild.logChannel}>**
                                    Events: **${guild.subbedEvents.joinToString(", ")}**
                                """.trimIndent())
                            }
                        }
                        "ignored" -> {
                            // TODO: Ignore/unignore certain commands
                            e.reply("Will be added in a future update")
                        }
                        else -> e.reply("This is not a valid setting to check, visit the website (https://sophiebot.info/settings) to see which settings you can check")
                    }
                }
                else -> e.reply("That is not a valid setting, visit the website (https://sophiebot.info/settings) to see which settings you can change")
            }
        }
    }
}
