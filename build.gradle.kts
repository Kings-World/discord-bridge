import org.apache.tools.ant.filters.ReplaceTokens
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.0.21"
    id("fabric-loom") version "1.8-SNAPSHOT"
    id("io.github.p03w.machete") version "2.0.1"
}

group = property("maven_group")!!
version = property("mod_version")!!

repositories {
    // YamlConfiguration
    maven { url = uri("https://oss.sonatype.org/content/repositories/releases") }

    // Fabric Permissions API & Kord Snapshots
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
}

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    mappings("net.fabricmc:yarn:${property("yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")

    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_version")}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${property("fabric_kotlin_version")}")
    modImplementation("me.lucko:fabric-permissions-api:${property("fabric_permissions_api_version")}")

    include(implementation("com.github.shynixn.mccoroutine:mccoroutine-fabric-api:${property("mccoroutine_version")}")!!)
    include(implementation("com.github.shynixn.mccoroutine:mccoroutine-fabric-core:${property("mccoroutine_version")}")!!)

    include(implementation("org.bspfsystems:yamlconfiguration:${property("yaml_config_version")}")!!)
    include(implementation("dev.kord:kord-core:${property("kord_version")}")!!)
    include(implementation("io.ktor:ktor-client-java-jvm:${property("ktor_version")}")!!)

    // needed by YamlConfiguration
    include("org.yaml:snakeyaml:${property("snakeyaml_version")}")

    // Kord JVM
    include("dev.kord.cache:cache-api-jvm:${property("kord_cache_version")}")
    include("dev.kord.cache:cache-map-jvm:${property("kord_cache_version")}")
    include("dev.kord:kord-common-jvm:${property("kord_version")}")
    include("dev.kord:kord-core-jvm:${property("kord_version")}")
    include("dev.kord:kord-gateway-jvm:${property("kord_version")}")
    include("dev.kord:kord-rest-jvm:${property("kord_version")}")

    // Ktor JVM (needed by Kord)
    include("io.ktor:ktor-client-cio-jvm:${property("ktor_version")}")
    include("io.ktor:ktor-client-content-negotiation-jvm:${property("ktor_version")}")
    include("io.ktor:ktor-client-core-jvm:${property("ktor_version")}")
    include("io.ktor:ktor-client-okhttp-jvm:${property("ktor_version")}")
    /* not needed? */    include("io.ktor:ktor-client-websockets-jvm:${property("ktor_version")}")
    include("io.ktor:ktor-events-jvm:${property("ktor_version")}")
    include("io.ktor:ktor-http-cio-jvm:${property("ktor_version")}")
    include("io.ktor:ktor-http-jvm:${property("ktor_version")}")
    include("io.ktor:ktor-io-jvm:${property("ktor_version")}")
    include("io.ktor:ktor-network-jvm:${property("ktor_version")}")
    include("io.ktor:ktor-network-tls-jvm:${property("ktor_version")}")
    include("io.ktor:ktor-serialization-jvm:${property("ktor_version")}")
    include("io.ktor:ktor-serialization-kotlinx-json-jvm:${property("ktor_version")}")
    include("io.ktor:ktor-serialization-kotlinx-jvm:${property("ktor_version")}")
    include("io.ktor:ktor-utils-jvm:${property("ktor_version")}")
    /* not needed? */    include("io.ktor:ktor-websocket-serialization-jvm:${property("ktor_version")}")
    include("io.ktor:ktor-websockets-jvm:${property("ktor_version")}")

    // Kotlin Logging JVM (mu.KotlinLogging) needed by Kord Rest
    include("io.github.oshai:kotlin-logging-jvm:${property("kotlin_logging_version")}")
    include("io.github.microutils:kotlin-logging-jvm:${property("mu_logging_version")}")

    // OkHttp JVM (needed by Ktor's OkHttp client)
    include("com.squareup.okhttp3:okhttp:${property("okhttp_version")}")
    include("com.squareup.okio:okio:${property("okio_version")}")

    // Stately JVM (needed by Kord Cache)
    /* not needed? */    include("co.touchlab:stately-concurrency-jvm:${property("stately_version")}")
    include("co.touchlab:stately-concurrent-collections-jvm:${property("stately_version")}")
    /* not needed? */    include("co.touchlab:stately-strict-jvm:${property("stately_version")}")
}

base {
    archivesName.set(property("archives_base_name")!! as String)
}

tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand(mutableMapOf(
            "version" to project.version,
            "minecraft_version" to project.extra["minecraft_version"],
            "fabric_kotlin_version" to project.extra["fabric_kotlin_version"]
        ))
    }

    filesMatching("config.yml") {
        filter<ReplaceTokens>("beginToken" to "\${", "endToken" to "}", "tokens" to mapOf(
            "version" to project.version
        ))
    }
}

tasks.withType<JavaCompile> {
    // Minecraft 1.18 (1.18-pre2) upwards uses Java 17.
    options.release.set(17)
}

java {
    // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
    // if it is present.
    // If you remove this line, sources will not be generated.
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${base.archivesName.get()}" }
    }
}

kotlin {
    jvmToolchain(23)
}

machete {
    enabled = true
    keepOriginal = true
}

tasks.compileKotlin {
    compilerOptions.jvmTarget = JvmTarget.JVM_17
}
