package net.kings_world.discord_bridge.listeners

import com.github.shynixn.mccoroutine.fabric.mcCoroutineConfiguration
import net.kings_world.discord_bridge.DiscordBridge
import net.kings_world.discord_bridge.DiscordBridge.logger

object ServerStopped {
    fun run() {
        logger.info("Disposing plugin session")
        DiscordBridge.mcCoroutineConfiguration.disposePluginSession()
    }
}