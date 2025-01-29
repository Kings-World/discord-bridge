package net.kings_world.discord_bridge.listeners

import net.kings_world.discord_bridge.DiscordBridge.config
import net.kings_world.discord_bridge.Utils.stringReplace
import net.kings_world.discord_bridge.discord.Discord
import net.minecraft.server.network.ServerPlayerEntity

object PlayerJoin {
    suspend fun run(player: ServerPlayerEntity) {
        if (!config.messages.playerJoin.enabled) return

        Discord.sendWebhook {
            username = player.name.string
            avatarUrl = config.avatarUrl.replace("{uuid}", player.uuid.toString())
            content = stringReplace(
                config.messages.playerJoin.content, mapOf(
                "name" to player.name.string,
                "uuid" to player.uuid.toString()
            ))
        }
    }
}