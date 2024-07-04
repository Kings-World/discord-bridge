package net.kings_world.discord_bridge.listeners

import net.kings_world.discord_bridge.DiscordBridge.config
import net.kings_world.discord_bridge.DiscordBridge.discord

object ServerStopping {
    suspend fun run() {
        discord.sendConfigMessage(config.messages.stopped)
        discord.shutdown()
    }
}