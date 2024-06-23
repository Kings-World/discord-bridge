package net.kings_world.discord_bridge.discord

import dev.kord.common.entity.Choice
import dev.kord.common.entity.optional.Optional
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.suggest
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.core.event.interaction.AutoCompleteInteractionCreateEvent
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.interaction.string
import net.kings_world.discord_bridge.DiscordBridge
import net.minecraft.server.MinecraftServer
import net.minecraft.text.Text

object DiscordCommands {
    private suspend fun list(interaction: ChatInputCommandInteraction, server: MinecraftServer) {
        val players = server.playerManager.playerList.map {
            "- ${it.name.string} ${it.pingMilliseconds} ms)"
        }

        val plural = if (players.size == 1) "" else "s"
        val colon = if (players.isEmpty()) "" else ":"

        interaction.respondEphemeral {
            content = """
                There are ${players.size}/${server.maxPlayerCount} player$plural online$colon
                ${players.joinToString("\n")}
            """.trimIndent()
        }
    }

    private suspend fun message(interaction: ChatInputCommandInteraction, server: MinecraftServer) {
        val playerName = interaction.command.strings.getValue("player")
        val message = interaction.command.strings.getValue("content")

        val player = server.playerManager.getPlayer(playerName)
        if (player == null) {
            interaction.respondEphemeral { content = "The provided player could not be found" }
            return
        }

        if (player.isDisconnected) {
            interaction.respondEphemeral { content = "${player.name.string} is not currently online" }
            return
        }

        player.sendMessage(Text.of(message))
        interaction.respondEphemeral { content = "Message sent to ${player.name.string}" }
    }

    suspend fun registerCommands(kord: Kord) {
        DiscordBridge.logger.info("Registering slash commands")

        kord.createGlobalChatInputCommand("list", "List all players on the server")
        kord.createGlobalChatInputCommand("message", "Send a message to a player on the server") {
            string("player", "The player to send the message to") {
                required = true
                autocomplete = true
            }
            string("content", "The content of the message") {
                required = true
            }
        }
    }

    suspend fun registerEvents(kord: Kord, server: MinecraftServer) {
        DiscordBridge.logger.info("Loading interaction events")

        kord.on<AutoCompleteInteractionCreateEvent> {
            val focused = interaction.focusedOption.value
            val choices = server.playerManager.playerList.stream()
                .filter { if (focused.isNotBlank()) it.name.string.contains(focused, true) else true }
                .map { Choice.StringChoice(name = it.name.string, value = it.name.string, nameLocalizations = Optional()) }
                .toList()
            interaction.suggest(choices)
        }

        kord.on<ChatInputCommandInteractionCreateEvent> {
            when (interaction.invokedCommandName) {
                "list" -> list(interaction, server)
                "message" -> message(interaction, server)
            }
        }
    }
}