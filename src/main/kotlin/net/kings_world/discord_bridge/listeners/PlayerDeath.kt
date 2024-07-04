package net.kings_world.discord_bridge.listeners

import net.kings_world.discord_bridge.DiscordBridge.config
import net.kings_world.discord_bridge.DiscordBridge.discord
import net.kings_world.discord_bridge.Utils.stringReplace
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text

object PlayerDeath {
    fun run(player: ServerPlayerEntity, message: Text) {
        if (!config.messages.playerDeath.enabled) return

        discord.sendWebhook {
            username = player.name.string
            avatarUrl = config.avatarUrl.replace("{uuid}", player.uuid.toString())
            content = stringReplace(
                config.messages.playerDeath.content, mapOf(
                    "name" to player.name.string,
                    "uuid" to player.uuid.toString(),
                    "message" to message.string
                )
            )
        }
    }
}