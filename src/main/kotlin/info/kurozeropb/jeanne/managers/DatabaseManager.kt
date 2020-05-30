package info.kurozeropb.jeanne.managers

import com.mongodb.ConnectionString
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import info.kurozeropb.jeanne.CommandData
import info.kurozeropb.jeanne.Config
import info.kurozeropb.jeanne.User as dbUser
import info.kurozeropb.jeanne.Guild as dbGuild
import net.dv8tion.jda.api.entities.Guild
import org.litote.kmongo.*
import kotlin.system.measureTimeMillis

class DatabaseManager(guild: Guild) {

    private val id = guild.id

    fun getGuildData(): dbGuild? {
        return guilds.findOne(dbGuild::id eq id)
    }

    companion object {
        private lateinit var client: MongoClient
        private lateinit var db: MongoDatabase
        lateinit var guilds: MongoCollection<dbGuild>
        lateinit var users: MongoCollection<dbUser>
        lateinit var commands: MongoCollection<CommandData>
        var guildPrefixes = mutableMapOf<String, String>()
        var usersData = mutableMapOf<String, MutableMap<String, Double>>()

        fun initialize(config: Config) {
            println("Connecting to the database... ")
            val milli = measureTimeMillis {
                client = KMongo.createClient(ConnectionString("mongodb://${config.database.user}:${config.database.passwd}@${config.database.host}:${config.database.port}/${config.database.name}?retryWrites=true"))
                db = client.getDatabase(config.database.name)
                guilds = db.getCollection<dbGuild>("guilds")
                users = db.getCollection<dbUser>("users")
                commands = db.getCollection<CommandData>("commands")
                val allGuilds = guilds.find("{}")
                val allUsers = users.find("{}")
                allGuilds.forEach { guildPrefixes[it.id] = it.prefix }
                allUsers.forEach { usersData[it.id] = mutableMapOf("level" to it.level, "points" to it.points) }
            }
            println("Done! (${milli}ms)")
        }
    }
}
