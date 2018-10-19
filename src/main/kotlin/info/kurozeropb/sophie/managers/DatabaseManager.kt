package info.kurozeropb.sophie.managers

import com.mongodb.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import info.kurozeropb.sophie.Config
import info.kurozeropb.sophie.User as dbUser
import info.kurozeropb.sophie.Guild as dbGuild
import net.dv8tion.jda.core.entities.Guild
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
        var guildPrefixes = mutableMapOf<String, String>()
        var usersData = mutableMapOf<String, MutableMap<String, Double>>()

        fun initialize(config: Config) {
            println("Connecting to the database... ")
            val milli = measureTimeMillis {
                client = KMongo.createClient(config.db.host, config.db.port)
                db = client.getDatabase(config.db.name)
                guilds = db.getCollection<dbGuild>("guilds")
                users = db.getCollection<dbUser>("users")
                val allGuilds = guilds.find("{}")
                val allUsers = users.find("{}")
                allGuilds.forEach { guildPrefixes[it.id] = it.prefix }
                allUsers.forEach { usersData[it.id] = mutableMapOf("level" to it.level, "points" to it.points) }
            }
            println("Done! (${milli}ms)")
        }
    }
}
