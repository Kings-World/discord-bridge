package net.kings_world.discord_bridge

import com.github.shynixn.mccoroutine.fabric.*
import kotlinx.coroutines.*
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
import java.util.concurrent.Executor

object DiscordBridge : DedicatedServerModInitializer {
    const val MOD_ID = "discord-bridge"
    val logger: Logger = LoggerFactory.getLogger(MOD_ID)
    val configFolder: Path = FabricLoader.getInstance().configDir.resolve(MOD_ID)

    // maybe use koin for these?
    lateinit var server: MinecraftServer
    lateinit var config: Config

    override fun onInitializeServer() {
        // server
        ServerLifecycleEvents.SERVER_STARTING.register { server ->
            // Connect Native Minecraft Scheduler and MCCoroutine
            mcCoroutineConfiguration.minecraftExecutor = Executor { r -> server.submitAndJoin(r) }
            launch { ServerStarting.run(server) }
        }
        ServerLifecycleEvents.SERVER_STARTED.register { server ->
            launch { ServerStarted.run(server) }
        }
        ServerLifecycleEvents.SERVER_STOPPING.register {
            // TODO: figure out how to properly shutdown
            // i'm currently trying to figure it out but having no luck at doing such
            // my goal is to stop the server hanging on the "Thread Query Listener stopped" message
            // i know its this mod because when i remove it, it stops hanging
            runBlocking {
                try {
                    logger.info("Sending stopped message")
                    Discord.sendConfigMessage(config.messages.stopped)

                    logger.info("Shutting down Kord and clearing resources")
                    Discord.shutdown()
                } catch (e: Exception) {
                    logger.error("Error during shutdown", e)
                } finally {
                    logger.info("Disposing plugin session")
                    mcCoroutineConfiguration.disposePluginSession()
                }
            }
        }

        // messages
        ServerMessageEvents.CHAT_MESSAGE.register { message, sender, _ -> launch { ChatMessage.run(message, sender) }  }
        DiscordBridgeEvents.DISCORD_MESSAGE.register { message -> launch { DiscordMessage.run(message) } }

        // players
        DiscordBridgeEvents.PLAYER_JOIN.register { player -> launch { PlayerJoin.run(player) } }
        DiscordBridgeEvents.PLAYER_LEAVE.register { player, reason -> launch { PlayerLeave.run(player, reason) } }
        DiscordBridgeEvents.PLAYER_DEATH.register { player, message -> launch { PlayerDeath.run(player, message) } }
        DiscordBridgeEvents.PLAYER_ADVANCEMENT.register { player, advancement -> launch { PlayerAdvancement.run(player, advancement) } }

        // mc coroutine
        MCCoroutineExceptionEvent.EVENT.register(MCCoroutineException)
    }
}