package net.kings_world.discord_bridge.minecraft

import com.mojang.brigadier.arguments.StringArgumentType.*
import com.mojang.brigadier.context.CommandContext
import kotlinx.coroutines.*
import me.lucko.fabric.api.permissions.v0.Permissions
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.loader.api.FabricLoader
import net.kings_world.discord_bridge.DiscordBridge
import net.kings_world.discord_bridge.config.Config
import net.kings_world.discord_bridge.discord.Discord
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import java.util.function.Predicate

object MinecraftCommands {
    private fun reload(ctx: CommandContext<ServerCommandSource>, config: Config, discord: Discord): Int {
        ctx.source.sendFeedback({ Text.of("Reloading config file, please wait...") }, true)
        val changes = config.reload()

        if (changes.webhookUrl) {
            DiscordBridge.logger.info("Webhook URL has been changed, making changes...")
            config.parseWebhookUrl()
        }

        if (changes.discordToken) {
            DiscordBridge.logger.info("Discord token has been changed, making changes...")
            DiscordBridge.scope.launch {
                discord.shutdown()
                discord.init(ctx.source.server, true)
            }
        }

        if (changes.presence && !changes.discordToken) {
            DiscordBridge.logger.info("Presence has been changed, updating...")
            DiscordBridge.scope.launch { discord.setPresence(config.startedActivity) }
        }

        DiscordBridge.logger.info("The config has reloaded and made the necessary changes")
        return 1
    }

    private fun send(ctx: CommandContext<ServerCommandSource>, discord: Discord): Int {
        ctx.source.sendFeedback({ Text.of("Sending your message to Discord") }, true)
        DiscordBridge.scope.launch { discord.sendMessage(getString(ctx, "message")) }
        return 1
    }

    private fun version(ctx: CommandContext<ServerCommandSource>): Int {
        val mod = FabricLoader.getInstance().getModContainer(DiscordBridge.MOD_ID)
        if (mod.isEmpty) {
            ctx.source.sendError(Text.of("DiscordBridge is not installed on the server"))
            return 0
        }

        val metadata = mod.get().metadata
        ctx.source.sendFeedback({ Text.of("${metadata.name} v${metadata.version}") }, true)
        return 1
    }

    private fun requirePermission(permission: String, defaultLevel: Int): Predicate<ServerCommandSource> {
        return when (isFabricPermissionsAPILoaded()) {
            true -> Permissions.require("${DiscordBridge.MOD_ID}.$permission", defaultLevel)
            false -> Predicate { source -> source.hasPermissionLevel(defaultLevel) }
        }
    }

    private fun isFabricPermissionsAPILoaded(): Boolean {
        return FabricLoader.getInstance().isModLoaded("fabric-permissions-api-v0")
    }

    fun registerCommands(config: Config, discord: Discord) {
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(
                literal(DiscordBridge.MOD_ID)
                    .then(
                        literal("reload")
                            .requires(requirePermission("reload", 4))
                            .executes { reload(it, config, discord) }
                    )
                    .then(
                        literal("send")
                            .requires(requirePermission("send", 4))
                            .then(argument("message", greedyString()).executes { send(it, discord) })
                    )
                    .then(
                        literal("version")
                            .requires(requirePermission("version", 0))
                            .executes { version(it) }
                    )
            )
        }
    }
}