package net.kings_world.discord_bridge.listeners

import dev.kord.rest.builder.message.allowedMentions
import net.kings_world.discord_bridge.DiscordBridge.config
import net.kings_world.discord_bridge.Utils.stringReplace
import net.kings_world.discord_bridge.discord.Discord
import net.minecraft.network.message.SignedMessage
import net.minecraft.server.network.ServerPlayerEntity

object ChatMessage {
    suspend fun run(message: SignedMessage, sender: ServerPlayerEntity) {
        if (!config.messages.chatMessage.enabled) return

        Discord.sendWebhook {
            username = sender.name.string
            avatarUrl = config.avatarUrl.replace("{uuid}", sender.uuid.toString())
            content = stringReplace(
                config.messages.chatMessage.content, mapOf(
                    "name" to sender.name.string,
                    "uuid" to sender.uuid.toString(),
                    "content" to message.content.string
                )
            )
            allowedMentions { }
        }
    }
}