package info.kurozeropb.prinzeugen.managers

import java.io.File
import info.kurozeropb.prinzeugen.Config
import com.moandjiezana.toml.Toml

object ConfigManager {
    private val configFile = File("config.toml")

    fun read(): Config {
        if (!configFile.exists()) {
            throw Exception("Could not find config file!")
        }

        return Toml().read(configFile).to(Config::class.java)
    }
}
