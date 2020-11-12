package dev.vdbroek.jeanne.managers

import com.moandjiezana.toml.Toml
import dev.vdbroek.jeanne.Config
import java.io.File

object ConfigManager {
    private val configFile = File("config.toml")

    fun read(): Config {
        if (!configFile.exists()) {
            throw Exception("Could not find config file!")
        }

        return Toml().read(configFile).to(Config::class.java)
    }
}
