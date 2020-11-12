import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

group = "dev.vdbroek"
version = Versions.jeanne

plugins {
    application
    kotlin("jvm") version Versions.kotlin
    id("com.github.johnrengelman.shadow") version Versions.shadow
}

buildscript {
    repositories {
        mavenCentral()
        jcenter()
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}")
    }
}

application {
    mainClassName = "$group.${rootProject.name}.Jeanne"
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = Versions.jvmTarget
    }

    compileTestKotlin {
        kotlinOptions.jvmTarget = Versions.jvmTarget
    }

    test {
        useJUnit()
    }

    @Suppress("UnstableApiUsage")
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set(rootProject.name)
        archiveClassifier.set("")
        archiveVersion.set(Versions.jeanne)
        destinationDirectory.set(file("build/libs"))

        mergeServiceFiles()
        manifest {
            attributes(mapOf("Main-Class" to "dev.vdbroek.jeanne.Jeanne"))
        }
    }
}

defaultTasks("run")

repositories {
    mavenCentral()
    jcenter()
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("stdlib-jdk8"))

    implementation(Dependencies.Fuel.Result.core)
    implementation(Dependencies.Fuel.Result.coroutines)

    implementation(Dependencies.Kotlin.reflect)
    implementation(Dependencies.KotlinX.Coroutines.core)
    implementation(Dependencies.KotlinX.Coroutines.jdk8)

    implementation(Dependencies.Logging.slf4j)
    implementation(Dependencies.Logging.Logback.core)
    implementation(Dependencies.Logging.Logback.classic)

    implementation(Dependencies.azurlane)
    implementation(Dependencies.jda)
    implementation(Dependencies.klaxon)
    implementation(Dependencies.kmongo)
    implementation(Dependencies.mordant)
    implementation(Dependencies.reflections)
    implementation(Dependencies.toml4j)
    implementation(Dependencies.webhooks)
    implementation(Dependencies.weeb4j) // Unmaitained

    testImplementation(Dependencies.Test.junit)
}
