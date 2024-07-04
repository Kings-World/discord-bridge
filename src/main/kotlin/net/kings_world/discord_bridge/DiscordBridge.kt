package net.kings_world.discord_bridge

import com.github.shynixn.mccoroutine.fabric.MCCoroutineExceptionEvent
import com.github.shynixn.mccoroutine.fabric.launch
import kotlinx.coroutines.runBlocking
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents
import net.fabricmc.loader.api.FabricLoader
import net.kings_world.discord_bridge.config.Config
import net.kings_world.discord_bridge.discord.Discord
import net.kings_world.discord_bridge.listeners.*
import net.minecraft.server.MinecraftServer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Path

object DiscordBridge : DedicatedServerModInitializer {
    const val MOD_ID = "discord-bridge"
    val logger: Logger = LoggerFactory.getLogger(MOD_ID)
    val configFolder: Path = FabricLoader.getInstance().configDir.resolve(MOD_ID)

    // maybe use koin for these?
    lateinit var server: MinecraftServer
    lateinit var config: Config
    lateinit var discord: Discord

    override fun onInitializeServer() {
        // server
        ServerLifecycleEvents.SERVER_STARTING.register { server -> ServerStarting.connect(server) }
        ServerLifecycleEvents.SERVER_STARTED.register { server -> launch { ServerStarted.run(server) } }
        ServerLifecycleEvents.SERVER_STOPPING.register { runBlocking { ServerStopping.run() } }
        ServerLifecycleEvents.SERVER_STOPPED.register { ServerStopped.run() }

        // messages
        ServerMessageEvents.CHAT_MESSAGE.register { message, sender, _ -> ChatMessage.run(message, sender)  }
        DiscordBridgeEvents.DISCORD_MESSAGE.register { message -> launch { DiscordMessage.run(message) } }

        // players
        DiscordBridgeEvents.PLAYER_JOIN.register { player -> PlayerJoin.run(player) }
        DiscordBridgeEvents.PLAYER_LEAVE.register { player, reason -> PlayerLeave.run(player, reason) }
        DiscordBridgeEvents.PLAYER_DEATH.register { player, message -> PlayerDeath.run(player, message) }
        DiscordBridgeEvents.PLAYER_ADVANCEMENT.register { player, advancement -> PlayerAdvancement.run(player, advancement) }

        // mc coroutine
        MCCoroutineExceptionEvent.EVENT.register(MCCoroutineException)
    }
}