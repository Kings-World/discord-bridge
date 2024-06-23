package net.kings_world.discord_bridge.config

import net.kings_world.discord_bridge.DiscordBridge.logger
import org.bspfsystems.yamlconfiguration.configuration.InvalidConfigurationException
import org.bspfsystems.yamlconfiguration.file.YamlConfiguration
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path

class Yaml(private val configFile: File, templateName: String?) {
    val config = YamlConfiguration()

    init {
        if (!configFile.exists() && templateName != null) {
            try {
                Files.createDirectories(Path.of(configFile.parent))
            } catch (e: IOException) {
                logger.error("Failed to create config directory", e)
            }

            try {
                val stream: InputStream? = Yaml::class.java.getResourceAsStream("/$templateName")
                logger.info("Creating config from template: $templateName")
                if (stream == null) {
                    logger.error("Failed to load template: $templateName")
                } else {
                    Files.copy(stream, configFile.toPath())
                }
            } catch (e: IOException) {
                logger.error("Failed to create config from template: $templateName", e)
            }
        }
    }

    fun load() {
        try {
            config.load(configFile)
        } catch (e: IOException) {
            logger.error("Failed to load config $configFile", e)
        } catch (e: InvalidConfigurationException) {
            logger.error("Failed to load config $configFile", e)
        }
    }

    @Suppress("unused")
    fun save() {
        try {
            config.save(configFile)
        } catch (e: IOException) {
            logger.error("Failed to save config $configFile", e)
        }
    }
}