package net.kings_world.discord_bridge.listeners

import net.kings_world.discord_bridge.DiscordBridge.config
import net.kings_world.discord_bridge.DiscordBridge.discord
import net.kings_world.discord_bridge.Utils.stringReplace
import net.minecraft.advancement.AdvancementDisplay
import net.minecraft.server.network.ServerPlayerEntity

object PlayerAdvancement {
    fun run(player: ServerPlayerEntity, advancement: AdvancementDisplay) {
        if (advancement.title.string.isBlank()) return
        if (!config.messages.playerAdvancement.enabled) return

        discord.sendWebhook {
            username = player.name.string
            avatarUrl = config.avatarUrl.replace("{uuid}", player.uuid.toString())
            content = stringReplace(
                config.messages.playerAdvancement.content, mapOf(
                "name" to player.name.string,
                "uuid" to player.uuid.toString(),
                "title" to advancement.title.string,
                "description" to advancement.description.string
            ))
        }
    }
}