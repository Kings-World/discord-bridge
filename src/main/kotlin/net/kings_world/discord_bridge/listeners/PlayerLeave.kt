package net.kings_world.discord_bridge.listeners

import net.kings_world.discord_bridge.DiscordBridge.config
import net.kings_world.discord_bridge.Utils.stringReplace
import net.kings_world.discord_bridge.discord.Discord
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text

object PlayerLeave {
    suspend fun run(player: ServerPlayerEntity, reason: Text) {
        if (!config.messages.playerLeave.enabled) return

        Discord.sendWebhook {
            username = player.name.string
            avatarUrl = config.avatarUrl.replace("{uuid}", player.uuid.toString())
            content = stringReplace(
                config.messages.playerLeave.content, mapOf(
                "name" to player.name.string,
                "uuid" to player.uuid.toString(),
                "reason" to reason.string
            ))
        }
    }
}