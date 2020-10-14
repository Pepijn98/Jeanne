import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

group = "info.kurozeropb"
version = "0.9.1"

plugins {
    java
    application
    kotlin("jvm") version "1.3.72"
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

buildscript {
    repositories {
        mavenCentral()
        jcenter()
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.72")
    }
}

application {
    mainClassName = "${group}.jeanne.Jeanne"
}


defaultTasks("run")

repositories {
    mavenCentral()
    jcenter()
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.4.10")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.0-M1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.4.0-M1")
    implementation("net.dv8tion:JDA:4.2.0_209")
    implementation("org.reflections:reflections:0.9.12")
    implementation("org.litote.kmongo:kmongo:4.1.3")
    implementation("org.slf4j:slf4j-api:1.7.30")
    implementation("com.moandjiezana.toml:toml4j:0.7.2")
    implementation("ch.qos.logback:logback-core:1.2.3")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("com.beust:klaxon:5.4")
    implementation("com.github.natanbc:weeb4j:3.5") // Unmaitained
    implementation("com.github.ajalt:mordant:1.2.1")
    implementation("club.minnced:discord-webhooks:0.5.0")
    implementation("com.github.KurozeroPB:AzurLaneKt:1.5.0")
    implementation("com.github.kittinunf.result:result:3.1.0")
    implementation("com.github.kittinunf.result:result-coroutines:3.1.0")
    testCompile("junit:junit:4.13.1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<ShadowJar> {
    baseName = "jeanne"
    classifier = ""
    version = version
    destinationDir = file("build/libs")
}
