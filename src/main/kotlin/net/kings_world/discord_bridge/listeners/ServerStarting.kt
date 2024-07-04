package net.kings_world.discord_bridge.listeners

import com.github.shynixn.mccoroutine.fabric.launch
import com.github.shynixn.mccoroutine.fabric.mcCoroutineConfiguration
import net.kings_world.discord_bridge.DiscordBridge
import net.kings_world.discord_bridge.DiscordBridge.config
import net.kings_world.discord_bridge.DiscordBridge.discord
import net.kings_world.discord_bridge.DiscordBridge.logger
import net.kings_world.discord_bridge.config.Config
import net.kings_world.discord_bridge.discord.Discord
import net.minecraft.server.MinecraftServer
import java.util.concurrent.Executor

object ServerStarting {
    fun connect(server: MinecraftServer) {
        // Connect Native Minecraft Scheduler and MCCoroutine.
        DiscordBridge.mcCoroutineConfiguration.minecraftExecutor = Executor { runnable ->
            server.submitAndJoin(runnable)
        }
        DiscordBridge.launch { run(server) }
    }

    suspend fun run(server: MinecraftServer) {
        logger.info("_  _ _ _  _ ____ ____    _ _ _ ____ ____ _    ___  ")
        logger.info("|_/  | |\\ | | __ [__     | | | |  | |__/ |    |  \\ ")
        logger.info("| \\_ | | \\| |__] ___]    |_|_| |__| |  \\ |___ |__/ ")
        logger.info("                                                   ")

        DiscordBridge.server = server
        config = Config("config.yml")
        discord = Discord(config)

        discord.init(server)
    }
}