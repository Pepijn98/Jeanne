package info.kurozeropb.jeanne.managers

import com.github.natanbc.weeb4j.TokenType
import com.github.natanbc.weeb4j.Weeb4J
import info.kurozeropb.jeanne.*
import info.kurozeropb.jeanne.commands.Registry
import info.kurozeropb.jeanne.Cooldown
import info.kurozeropb.jeanne.Guild
import info.kurozeropb.jeanne.User
import info.kurozeropb.jeanne.core.Utils
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import kotlin.math.floor
import kotlin.math.sqrt
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.guild.GuildBanEvent
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent
import net.dv8tion.jda.api.events.guild.member.*
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent
import org.litote.kmongo.*
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.concurrent.schedule

class EventManager : ListenerAdapter() {

    private val cooldowns: MutableList<Cooldown> = mutableListOf()
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

    override fun onReady(e: ReadyEvent) {
        if (e.jda.shardInfo.shardId == Jeanne.shardManager.shardsTotal - 1) { // Wait for all shards to be ready
            val selfUser = e.jda.selfUser
            Jeanne.defaultHeaders = mapOf("User-Agent" to "${selfUser.name}/v${Jeanne.config.version} (jeannebot.info)")
            Jeanne.weebApi = Weeb4J.Builder()
                    .setToken(TokenType.WOLKE, Jeanne.config.tokens.wolke)
                    .setBotId(selfUser.idLong)
                    .setBotInfo(selfUser.name, Jeanne.config.version, Jeanne.config.env)
                    .build()

            println("""
            ||-=========================================================
            || Account info: ${selfUser.name}#${selfUser.discriminator} (ID: ${selfUser.id})
            || Connected to ${Jeanne.shardManager.guilds.size} guilds, ${Jeanne.shardManager.textChannels.size + Jeanne.shardManager.voiceChannels.size} channels
            || Default prefix: ${Jeanne.config.prefix}
            ||-=========================================================
            """.trimMargin("|"))

            Utils.updateCommandDatabase()

            Jeanne.isReady = true

            if (Jeanne.config.env.startsWith("prod")) {
                Timer().schedule(1_800_000, 1_800_000) {
                    Utils.sendGuildCountAll(Jeanne.shardManager.guilds.size, Jeanne.shardManager.shardsTotal)
                }
            }
        }
    }

    override fun onGuildMemberRoleAdd(event: GuildMemberRoleAddEvent) {
        if (event.guild.id == "240059867744698368" && event.roles.map { it.id }.contains("464548479792971786")) {
            val user = DatabaseManager.users.findOne(User::id eq event.user.id)
            if (user == null) {
                DatabaseManager.users.insertOne(User(event.user.id, donator = true))
            } else {
                DatabaseManager.users.updateOne(User::id eq event.user.id, setValue(User::donator, true))
            }
        }
    }

    override fun onGuildMemberRoleRemove(event: GuildMemberRoleRemoveEvent) {
        if (event.guild.id == "240059867744698368" && event.roles.map { it.id }.contains("464548479792971786")) {
            val user = DatabaseManager.users.findOne(User::id eq event.user.id)
            if (user == null) {
                DatabaseManager.users.insertOne(User(event.user.id, donator = false))
            } else {
                DatabaseManager.users.updateOne(User::id eq event.user.id, setValue(User::donator, false))
            }
        }
    }

    override fun onMessageReceived(e: MessageReceivedEvent) {
        if (Jeanne.isReady.not())
            return

        val content = e.message.contentRaw
        val selfId = e.jda.selfUser.id
        val ctx = Utils(e)

        if (!e.guild.isAvailable)
            return

        if (e.isWebhookMessage || e.author.isFake || e.author.isBot || e.author.id == selfId)
            return

        var prefix = DatabaseManager.guildPrefixes[e.guild.id] ?: Jeanne.config.prefix
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
                        message = message.replace("%mention%", e.member?.asMention ?: "")
                        message = message.replace("%oldLevel%", level.toString())
                        message = message.replace("%newLevel%", currLevel.toString())
                        message = message.replace("%points%", points.toString())
                        ctx.reply(message)
                    }
                } else {
                    DatabaseManager.usersData[e.author.id]!!["points"] = points
                    DatabaseManager.users.updateOne(User::id eq e.author.id, setValue(User::points, points))
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

            if (command.isDonatorsOnly && e.author.id != Jeanne.config.developer) {
                val user = DatabaseManager.users.findOne(User::id eq e.author.id)
                if (user == null || user.donator.not())
                    return ctx.reply("This command can only be used by donators\nCheck out the donate command for more info")
            }

            if (e.author.id != Jeanne.config.developer) {
                val cooldown = cooldowns.find { it.id == e.author.id && it.command.name == command.name }

                if (cooldown != null) {
                    val timeUntil = cooldown.time.until(e.message.timeCreated, ChronoUnit.SECONDS)
                    val timeLeft = command.cooldown - timeUntil

                    if (timeUntil < command.cooldown && command.name == cooldown.command.name) {
                        ctx.reply("Command is on cooldown, $timeLeft seconds left.")
                        return
                    }

                    if (timeUntil >= command.cooldown && command.name == cooldown.command.name)
                        cooldowns.remove(cooldown)
                }

                cooldowns.add(Cooldown(e.author.id, command, e.message.timeCreated))
            }

            if (command.isDeveloperOnly && e.author.id != Jeanne.config.developer) {
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
                val hasPerms = e.member?.hasPermission(e.textChannel, command.userPermissions)
                if (hasPerms != null && !hasPerms && e.author.id != Jeanne.config.developer) {
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

    override fun onUserUpdateName(event: UserUpdateNameEvent) {
        val newName = event.newName
        if (newName.startsWith("Deleted User", true)) {
            val userID = event.entity.id
            DatabaseManager.users.findOneAndDelete(User::id eq userID)
        }
    }

    override fun onGuildLeave(e: GuildLeaveEvent) {
        val db = DatabaseManager(e.guild)
        val guild = db.getGuildData() ?: return

        DatabaseManager.guilds.findOneAndDelete(Guild::id eq guild.id)
        DatabaseManager.guildPrefixes.remove(guild.id)
    }

    override fun onGuildMemberJoin(e: GuildMemberJoinEvent) {
        val db = DatabaseManager(e.guild)
        val guild = db.getGuildData() ?: return

        if (guild.welcomeEnabled && guild.welcomeChannel.isNotBlank()) {
            val channel = e.guild.getTextChannelById(guild.welcomeChannel) ?: return
            if (channel.canTalk()) {
                var message = guild.welcomeMessage
                message = message.replace("%user%", e.user.name)
                message = message.replace("%mention%", e.member.asMention)
                message = message.replace("%guild%", e.guild.name)
                message = message.replace("%count%", e.guild.members.size.toString())
                channel.sendMessage(message).queue()
            }
        }

        if (guild.subbedEvents.contains("memberjoined") && guild.logChannel.isNotBlank()) {
            val channel = e.guild.getTextChannelById(guild.logChannel) ?: return
            channel.sendMessage(EmbedBuilder()
                    .setColor(Utils.embedColor(e))
                    .setTitle("Member joined")
                    .addField("Name", e.user.name, true)
                    .setThumbnail(e.user.effectiveAvatarUrl)
                    .setTimestamp(e.user.timeCreated)
                    .build()).queue()
        }
    }

    override fun onGuildMemberLeave(e: GuildMemberLeaveEvent) {
        val db = DatabaseManager(e.guild)
        val guild = db.getGuildData() ?: return

        if (guild.subbedEvents.contains("memberleft") && guild.logChannel.isNotBlank()) {
            val channel = e.guild.getTextChannelById(guild.logChannel) ?: return
            channel.sendMessage(EmbedBuilder()
                    .setColor(Utils.embedColor(e))
                    .setTitle("Member left")
                    .addField("Name", e.user.name, true)
                    .setThumbnail(e.user.effectiveAvatarUrl)
                    .setTimestamp(e.user.timeCreated)
                    .build()).queue()
        }
    }

    override fun onGuildBan(e: GuildBanEvent) {
        val db = DatabaseManager(e.guild)
        val guild = db.getGuildData() ?: return

        if (guild.subbedEvents.contains("memberbanned") && guild.logChannel.isNotBlank()) {
            val channel = e.guild.getTextChannelById(guild.logChannel) ?: return
            channel.sendMessage(EmbedBuilder()
                    .setColor(Utils.embedColor(e))
                    .setTitle("Member banned")
                    .addField("Name", e.user.name, true)
                    .setThumbnail(e.user.effectiveAvatarUrl)
                    .setTimestamp(e.user.timeCreated)
                    .build()).queue()
        }
    }

    override fun onGuildUnban(e: GuildUnbanEvent) {
        val db = DatabaseManager(e.guild)
        val guild = db.getGuildData() ?: return

        if (guild.subbedEvents.contains("memberunbanned") && guild.logChannel.isNotBlank()) {
            val channel = e.guild.getTextChannelById(guild.logChannel) ?: return
            channel.sendMessage(EmbedBuilder()
                    .setColor(Utils.embedColor(e))
                    .setTitle("Member unbanned")
                    .addField("Name", e.user.name, true)
                    .setThumbnail(e.user.effectiveAvatarUrl)
                    .setTimestamp(e.user.timeCreated)
                    .build()).queue()
        }
    }

    override fun onGuildMemberUpdateNickname(e: GuildMemberUpdateNicknameEvent) {
        val db = DatabaseManager(e.guild)
        val guild = db.getGuildData() ?:  return

        if (guild.subbedEvents.contains("nicknamechanged") && guild.logChannel.isNotBlank()) {
            val channel = e.guild.getTextChannelById(guild.logChannel) ?: return
            channel.sendMessage(EmbedBuilder()
                    .setColor(Utils.embedColor(e))
                    .setTitle("Nickname changed")
                    .addField("Old", e.oldNickname ?: "-", true)
                    .addField("New", e.newNickname ?: "-", true)
                    .setThumbnail(e.user.effectiveAvatarUrl)
                    .setTimestamp(e.user.timeCreated)
                    .build()).queue()
        }
    }
}
