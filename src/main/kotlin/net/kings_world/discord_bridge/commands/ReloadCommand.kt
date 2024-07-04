package net.kings_world.discord_bridge.commands

import com.github.shynixn.mccoroutine.fabric.SuspendingCommand
import com.github.shynixn.mccoroutine.fabric.executesSuspend
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.kings_world.discord_bridge.DiscordBridge
import net.kings_world.discord_bridge.DiscordBridge.config
import net.kings_world.discord_bridge.DiscordBridge.discord
import net.kings_world.discord_bridge.Utils.requirePermission
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text

class ReloadCommand : SuspendingCommand<ServerCommandSource> {
    val data: LiteralArgumentBuilder<ServerCommandSource> = literal("reload")
        .requires(requirePermission("reload", 4))
        .executesSuspend(DiscordBridge, this)

    override suspend fun run(context: CommandContext<ServerCommandSource>): Int {
        sendFeedback(context, "Reloading config file, please wait...")
        val changes = config.reload()

        if (changes.webhookUrl) {
            sendFeedback(context, "Webhook URL has been changed, making changes...")
            config.parseWebhookUrl()
        }

        if (changes.discordToken) {
            sendFeedback(context, "Discord token has been changed, making changes...")
            discord.shutdown()
            discord.init(context.source.server, true)
        }

        if (changes.presence && !changes.discordToken) {
            sendFeedback(context, "Presence has been changed, updating...")
            discord.setPresence(config.startedActivity)
        }

        sendFeedback(context, "The config has reloaded and made the necessary changes")
        return 1
    }

    private fun sendFeedback(context: CommandContext<ServerCommandSource>, message: String) {
        context.source.sendFeedback({ Text.of(message) }, true)
    }
}