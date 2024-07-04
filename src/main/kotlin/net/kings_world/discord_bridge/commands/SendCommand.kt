package net.kings_world.discord_bridge.commands

import com.github.shynixn.mccoroutine.fabric.SuspendingCommand
import com.github.shynixn.mccoroutine.fabric.executesSuspend
import com.mojang.brigadier.arguments.StringArgumentType.getString
import com.mojang.brigadier.arguments.StringArgumentType.greedyString
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.kings_world.discord_bridge.DiscordBridge
import net.kings_world.discord_bridge.DiscordBridge.discord
import net.kings_world.discord_bridge.Utils.requirePermission
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text

class SendCommand : SuspendingCommand<ServerCommandSource> {
    val data: LiteralArgumentBuilder<ServerCommandSource> = literal("send")
        .requires(requirePermission("send", 4))
        .then(argument("message", greedyString()).executesSuspend(DiscordBridge, this))

    override suspend fun run(context: CommandContext<ServerCommandSource>): Int {
        context.source.sendFeedback({ Text.of("Sending your message to Discord") }, true)
        discord.sendMessage(getString(context, "message"))
        return 1
    }
}