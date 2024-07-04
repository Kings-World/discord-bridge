package net.kings_world.discord_bridge.listeners

import net.kings_world.discord_bridge.DiscordBridge.MOD_ID
import net.kings_world.discord_bridge.DiscordBridge.config
import net.kings_world.discord_bridge.DiscordBridge.discord
import net.kings_world.discord_bridge.commands.ReloadCommand
import net.kings_world.discord_bridge.commands.SendCommand
import net.kings_world.discord_bridge.commands.VersionCommand
import net.minecraft.server.MinecraftServer
import net.minecraft.server.command.CommandManager.literal

object ServerStarted {
    suspend fun run(server: MinecraftServer) {
        discord.sendConfigMessage(config.messages.started)
        discord.setPresence(config.startedActivity)

        server.commandManager.dispatcher.register(
            literal(MOD_ID)
                .then(ReloadCommand().data)
                .then(SendCommand().data)
                .then(VersionCommand().data)
        )
    }
}