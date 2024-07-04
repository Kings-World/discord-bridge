package net.kings_world.discord_bridge.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.loader.api.FabricLoader
import net.kings_world.discord_bridge.DiscordBridge.MOD_ID
import net.kings_world.discord_bridge.Utils.requirePermission
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text

class VersionCommand {
    val data: LiteralArgumentBuilder<ServerCommandSource> = literal("version")
        .requires(requirePermission("version", 0))
        .executes { run(it) }

    private fun run(context: CommandContext<ServerCommandSource>): Int {
        val mod = FabricLoader.getInstance().getModContainer(MOD_ID)
        if (mod.isEmpty) {
            context.source.sendError(Text.of("DiscordBridge is not installed on the server"))
            return 0
        }

        val metadata = mod.get().metadata
        context.source.sendFeedback({ Text.of("${metadata.name} v${metadata.version}") }, true)
        return 1
    }
}