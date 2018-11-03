package info.kurozeropb.sophie.managers

import com.github.natanbc.weeb4j.TokenType
import com.github.natanbc.weeb4j.Weeb4J
import info.kurozeropb.sophie.Guild
import info.kurozeropb.sophie.Sophie
import info.kurozeropb.sophie.User
import info.kurozeropb.sophie.commands.Registry
import info.kurozeropb.sophie.utils.Utils
import net.dv8tion.jda.core.entities.ChannelType
import net.dv8tion.jda.core.events.ReadyEvent
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import org.litote.kmongo.SetTo
import org.litote.kmongo.eq
import org.litote.kmongo.set
import kotlin.math.floor
import kotlin.math.sqrt
import info.kurozeropb.sophie.Cooldown
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.events.guild.GuildBanEvent
import net.dv8tion.jda.core.events.guild.GuildUnbanEvent
import net.dv8tion.jda.core.events.guild.member.*
import org.litote.kmongo.findOne
import java.time.temporal.ChronoUnit

class EventManager : ListenerAdapter() {

    private val cooldowns: MutableList<Cooldown> = mutableListOf()

    override fun onReady(e: ReadyEvent) {
        if (e.jda.shardInfo.shardId == Sophie.shardManager.shardsTotal - 1) {
            val selfUser = e.jda.selfUser
            Sophie.defaultHeaders = mutableMapOf("User-Agent" to "${selfUser.name.replace(" ", "_")}/v${Sophie.config.version} (sophiebot.info)")
            Sophie.isReady = true
            Sophie.weebApi = Weeb4J.Builder()
                    .setToken(TokenType.WOLKE, Sophie.config.tokens.wolke)
                    .setBotId(selfUser.idLong)
                    .setBotInfo(selfUser.name, Sophie.config.version, Sophie.config.env)
                    .build()

            println("""
            ||-=========================================================
            || Account info: ${selfUser.name}#${selfUser.discriminator} (ID: ${selfUser.id})
            || Connected to ${Sophie.shardManager.guilds.size} guilds, ${Sophie.shardManager.textChannels.size + Sophie.shardManager.voiceChannels.size} channels
            || Default prefix: ${Sophie.config.prefix}
            ||-=========================================================
            """.trimMargin("|"))

            GlobalScope.async {
                Utils.setInterval(1_800_000) { // Every 30 minutes
                    Utils.sendGuildCountAll(Sophie.shardManager.guilds.size, Sophie.shardManager.shardsTotal)
                }
            }
        }
    }

    override fun onGuildMemberRoleAdd(event: GuildMemberRoleAddEvent?) {
        if (event == null)
            return

        if (event.guild.id == "240059867744698368" && event.roles.map { it.id }.contains("464548479792971786")) {
            val user = DatabaseManager.users.findOne(User::id eq event.user.id)
            if (user == null) {
                DatabaseManager.users.insertOne(User(event.user.id, donator = true))
            } else {
                DatabaseManager.users.updateOne(User::id eq event.user.id, set(User::donator, true))
            }
        }
    }

    override fun onGuildMemberRoleRemove(event: GuildMemberRoleRemoveEvent?) {
        if (event == null)
            return

        if (event.guild.id == "240059867744698368" && event.roles.map { it.id }.contains("464548479792971786")) {
            val user = DatabaseManager.users.findOne(User::id eq event.user.id)
            if (user == null) {
                DatabaseManager.users.insertOne(User(event.user.id, donator = false))
            } else {
                DatabaseManager.users.updateOne(User::id eq event.user.id, set(User::donator, false))
            }
        }
    }

    override fun onMessageReceived(e: MessageReceivedEvent) {
        if (Sophie.isReady.not())
            return

        val content = e.message.contentRaw
        val selfId = e.jda.selfUser.id
        val ctx = Utils(e)

        if (e.guild != null && !e.guild.isAvailable)
            return
        if (e.isWebhookMessage || e.author.isFake || e.author.isBot || e.author.id == selfId)
            return

        var prefix = DatabaseManager.guildPrefixes[e.guild?.id] ?: Sophie.config.prefix
        if (prefix == "%mention%")
            prefix = e.jda.selfUser.asMention

        if (content.matches("^<@!?$selfId>$".toRegex())) {
            ctx.reply("My prefix for this guild is: **$prefix**")
            return
        }

        val isMentionPrefix = content.matches("^<@!?$selfId>\\s.*".toRegex())
        if (isMentionPrefix.not() && content.startsWith(prefix, true).not()) {
            if (e.isFromType(ChannelType.PRIVATE))
                return

            val authorData = DatabaseManager.usersData[e.author.id]
            if (authorData != null) {
                var points = authorData["points"]!!
                val level = authorData["level"]!!
                points = points.plus(1.0)

                val currLevel = floor(0.1 * sqrt(points))
                if (currLevel > level) {
                    DatabaseManager.usersData[e.author.id]!!["level"] = currLevel
                    DatabaseManager.usersData[e.author.id]!!["points"] = points
                    DatabaseManager.users.updateOne(
                            User::id eq e.author.id,
                            set(SetTo(User::level, currLevel), SetTo(User::points, points))
                    )

                    val dbManager = DatabaseManager(e.guild)
                    val guild = dbManager.getGuildData()
                    if (guild != null && guild.levelupEnabled && !arrayOf("110373943822540800", "264445053596991498").contains(e.guild.id)) {
                        var message = guild.levelupMessage
                        message = message.replace("%user%", e.author.name)
                        message = message.replace("%mention%", e.member.asMention)
                        message = message.replace("%oldLevel%", level.toString())
                        message = message.replace("%newLevel%", currLevel.toString())
                        message = message.replace("%points%", points.toString())
                        ctx.reply(message)
                    }
                } else {
                    DatabaseManager.usersData[e.author.id]!!["points"] = points
                    DatabaseManager.users.updateOne(User::id eq e.author.id, set(User::points, points))
                }
            } else {
                DatabaseManager.usersData[e.author.id] = mutableMapOf("level" to 0.0, "points" to 0.0)
                DatabaseManager.users.insertOne(User(e.author.id, 0.0, 1.0))
            }
            return
        }

        prefix = if (isMentionPrefix) content.substring(0, content.indexOf('>') + 1) else prefix
        val index = if (isMentionPrefix) prefix.length + 1 else prefix.length

        val allArgs = content.substring(index).split("\\s+".toRegex())
        val command = Registry.getCommandByName(allArgs[0])
        val args = allArgs.drop(1)

        if (command != null) {
            if (e.isFromType(ChannelType.PRIVATE) && command.allowPrivate.not()) {
                ctx.reply("This command can only be used in a server")
                return
            }

            if (command.isDonatorsOnly) {
                val user = DatabaseManager.users.findOne(User::id eq e.author.id) ?: return
                if (user.donator.not())
                    return ctx.reply("This command can only be used by donators\nCheck out the donate command for more info")
            }

            if (e.author.id != Sophie.config.developer) {
                val cooldown = cooldowns.find { it.id == e.author.id && it.command.name == command.name }

                if (cooldown != null) {
                    val timeUntil = cooldown.time.until(e.message.creationTime, ChronoUnit.SECONDS)
                    val timeLeft = command.cooldown - timeUntil

                    if (timeUntil < command.cooldown && command.name == cooldown.command.name) {
                        ctx.reply("Command is on cooldown, $timeLeft seconds left.")
                        return
                    }

                    if (timeUntil >= command.cooldown && command.name == cooldown.command.name)
                        cooldowns.remove(cooldown)
                }

                cooldowns.add(Cooldown(e.author.id, command, e.message.creationTime))
            }

            if (command.isDeveloperOnly && e.author.id != Sophie.config.developer) {
                ctx.reply("This command can only be used by my developer")
                return
            }

            if (e.isFromType(ChannelType.PRIVATE).not() && command.botPermissions.isNotEmpty()) {
                val hasPerms = e.guild.selfMember.hasPermission(e.textChannel, command.botPermissions)
                if (!hasPerms) {
                    ctx.reply("""
                        The bot is missing certain permissions required by this command
                        Required permissions are: ${command.botPermissions.joinToString(", ") { it.getName() }}
                        """.trimIndent())
                    return
                }
            }

            if (e.isFromType(ChannelType.PRIVATE).not() && command.userPermissions.isNotEmpty()) {
                val hasPerms = e.member.hasPermission(e.textChannel, command.userPermissions)
                if (!hasPerms && e.author.id != Sophie.config.developer) {
                    ctx.reply("""
                        You are missing certain permissions required by this command
                        Required permissions are: ${command.userPermissions.joinToString(", ")}
                        """.trimIndent())
                    return
                }
            }

            GlobalScope.async {
                command.execute(args, e)
            }
        }
    }

    override fun onGuildLeave(e: GuildLeaveEvent) {
        val dbManager = DatabaseManager(e.guild)
        val guild = dbManager.getGuildData()
        val noEntry = guild == null

        if (!noEntry) {
            DatabaseManager.guilds.findOneAndDelete(Guild::id eq e.guild.id)
            DatabaseManager.guildPrefixes.remove(e.guild.id)
        }
    }

    override fun onGuildMemberJoin(e: GuildMemberJoinEvent) {
        val dbManager = DatabaseManager(e.guild)
        val guild = dbManager.getGuildData()
        val noEntry = guild == null

        if (!noEntry && guild!!.welcomeEnabled) {
            val channel = e.guild.getTextChannelById(guild.welcomeChannel)
            if (channel.canTalk()) {
                var message = guild.welcomeMessage
                message = message.replace("%user%", e.user.name)
                message = message.replace("%mention%", e.member.asMention)
                message = message.replace("%guild%", e.guild.name)
                message = message.replace("%count%", e.guild.members.size.toString())
                channel.sendMessage(message).queue()
            }
        }

        if (!noEntry && guild!!.subbedEvents.contains("memberjoined")) {
            val channel = e.guild.getTextChannelById(guild.logChannel)
            channel.sendMessage(EmbedBuilder()
                    .setColor(Utils.embedColor(e))
                    .setTitle("Member joined")
                    .addField("Name", e.user.name, true)
                    .setThumbnail(e.user.effectiveAvatarUrl)
                    .setTimestamp(e.user.creationTime)
                    .build()).queue()
        }
    }

    override fun onGuildMemberLeave(e: GuildMemberLeaveEvent) {
        val dbManager = DatabaseManager(e.guild)
        val guild = dbManager.getGuildData()
        val noEntry = guild == null

        if (noEntry || !guild!!.subbedEvents.contains("memberleft"))
            return

        val channel = e.guild.getTextChannelById(guild.logChannel)
        channel.sendMessage(EmbedBuilder()
                .setColor(Utils.embedColor(e))
                .setTitle("Member left")
                .addField("Name", e.user.name, true)
                .setThumbnail(e.user.effectiveAvatarUrl)
                .setTimestamp(e.user.creationTime)
                .build()).queue()
    }

    override fun onGuildBan(e: GuildBanEvent) {
        val dbManager = DatabaseManager(e.guild)
        val guild = dbManager.getGuildData()
        val noEntry = guild == null

        if (noEntry || !guild!!.subbedEvents.contains("memberbanned"))
            return

        val channel = e.guild.getTextChannelById(guild.logChannel)
        channel.sendMessage(EmbedBuilder()
                .setColor(Utils.embedColor(e))
                .setTitle("Member banned")
                .addField("Name", e.user.name, true)
                .setThumbnail(e.user.effectiveAvatarUrl)
                .setTimestamp(e.user.creationTime)
                .build()).queue()
    }

    override fun onGuildUnban(e: GuildUnbanEvent) {
        val dbManager = DatabaseManager(e.guild)
        val guild = dbManager.getGuildData()
        val noEntry = guild == null

        if (noEntry || !guild!!.subbedEvents.contains("memberunbanned"))
            return

        val channel = e.guild.getTextChannelById(guild.logChannel)
        channel.sendMessage(EmbedBuilder()
                .setColor(Utils.embedColor(e))
                .setTitle("Member unbanned")
                .addField("Name", e.user.name, true)
                .setThumbnail(e.user.effectiveAvatarUrl)
                .setTimestamp(e.user.creationTime)
                .build()).queue()
    }

    override fun onGuildMemberNickChange(e: GuildMemberNickChangeEvent) {
        val dbManager = DatabaseManager(e.guild)
        val guild = dbManager.getGuildData()
        val noEntry = guild == null

        if (noEntry || !guild!!.subbedEvents.contains("nicknamechanged"))
            return

        val channel = e.guild.getTextChannelById(guild.logChannel)
        channel.sendMessage(EmbedBuilder()
                .setColor(Utils.embedColor(e))
                .setTitle("Nickname changed")
                .addField("Old", e.prevNick ?: "Null", true)
                .addField("New", e.newNick ?: "Null", true)
                .setThumbnail(e.user.effectiveAvatarUrl)
                .setTimestamp(e.user.creationTime)
                .build()).queue()
    }
}
