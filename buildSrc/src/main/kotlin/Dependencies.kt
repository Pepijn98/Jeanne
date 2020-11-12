@file:JvmName("Deps")

import org.gradle.api.JavaVersion

object Versions {
    val jvmTarget = JavaVersion.VERSION_1_8.toString()

    const val fuel = "3.1.0"
    const val jeanne = "0.10.0"
    const val kotlin = "1.4.10"
    const val kotlinx = "1.4.1"
    const val logback = "1.2.3"
    const val shadow = "5.2.0"
}

object Dependencies {
    const val azurlane = "com.github.KurozeroPB:AzurLaneKt:1.5.0"
    const val jda = "net.dv8tion:JDA:4.2.0_214"
    const val klaxon = "com.beust:klaxon:5.4"
    const val kmongo = "org.litote.kmongo:kmongo:4.2.0"
    const val mordant = "com.github.ajalt:mordant:1.2.1"
    const val reflections = "org.reflections:reflections:0.9.12"
    const val toml4j = "com.moandjiezana.toml:toml4j:0.7.2"
    const val webhooks = "club.minnced:discord-webhooks:0.5.0"
    const val weeb4j = "com.github.natanbc:weeb4j:3.5"

    object Kotlin {
        const val reflect = "org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin}"
    }

    object KotlinX {

        object Coroutines {
            const val core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlinx}"
            const val jdk8 = "org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:${Versions.kotlinx}"
        }
    }

    object Fuel {

        object Result {
            const val core = "com.github.kittinunf.result:result:${Versions.fuel}"
            const val coroutines = "com.github.kittinunf.result:result-coroutines:${Versions.fuel}"
        }
    }

    object Logging {
        const val slf4j = "org.slf4j:slf4j-api:1.7.30"

        object Logback {
            const val core = "ch.qos.logback:logback-core:${Versions.logback}"
            const val classic = "ch.qos.logback:logback-classic:${Versions.logback}"
        }
    }

    object Test {
        const val junit = "junit:junit:4.13.1"
    }
}
