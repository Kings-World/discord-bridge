package net.kings_world.discord_bridge.listeners

import net.kings_world.discord_bridge.DiscordBridge
import net.kings_world.discord_bridge.DiscordBridge.config
import net.kings_world.discord_bridge.DiscordBridge.logger
import net.kings_world.discord_bridge.config.Config
import net.kings_world.discord_bridge.discord.Discord
import net.minecraft.server.MinecraftServer

object ServerStarting {
    suspend fun run(server: MinecraftServer) {
        logger.info("_  _ _ _  _ ____ ____    _ _ _ ____ ____ _    ___  ")
        logger.info("|_/  | |\\ | | __ [__     | | | |  | |__/ |    |  \\ ")
        logger.info("| \\_ | | \\| |__] ___]    |_|_| |__| |  \\ |___ |__/ ")
        logger.info("                                                   ")

        DiscordBridge.server = server
        config = Config("config.yml")

        Discord.prepare(config.startingActivity)
    }
}