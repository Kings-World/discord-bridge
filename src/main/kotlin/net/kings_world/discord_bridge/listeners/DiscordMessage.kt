package net.kings_world.discord_bridge.listeners

import dev.kord.core.entity.Message
import net.kings_world.discord_bridge.DiscordBridge.config
import net.kings_world.discord_bridge.DiscordBridge.server
import net.kings_world.discord_bridge.Utils.stringReplace
import net.kings_world.discord_bridge.discord.Formatters
import net.minecraft.text.Text

object DiscordMessage {
    suspend fun run(message: Message) {
        val template = if (message.referencedMessage != null)
            config.messages.discordMessage.reply else
            config.messages.discordMessage.standard

        if (!template.enabled) return
        if (message.interaction != null) return

        server.playerManager?.broadcast(
            Text.of(
                stringReplace(template.content, mapOf(
                "name" to Formatters.formatAuthor(message),
                "content" to Formatters.formatMessage(message),
                "reference_name" to Formatters.formatReference(message)
            ))
            ),
            false
        )
    }
}