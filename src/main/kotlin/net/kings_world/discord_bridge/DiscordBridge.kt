package net.kings_world.discord_bridge

import dev.kord.rest.builder.message.allowedMentions
import kotlinx.coroutines.*
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents
import net.fabricmc.loader.api.FabricLoader
import net.kings_world.discord_bridge.config.Config
import net.kings_world.discord_bridge.discord.Discord
import net.kings_world.discord_bridge.discord.Formatters
import net.kings_world.discord_bridge.minecraft.MinecraftCommands
import net.minecraft.server.MinecraftServer
import net.minecraft.text.Text
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Path

object DiscordBridge : ModInitializer {
    const val MOD_ID = "discord-bridge"
    val logger: Logger = LoggerFactory.getLogger(MOD_ID)
    val configFolder: Path = FabricLoader.getInstance().configDir.resolve(MOD_ID)
    // val scope = CoroutineScope(Job() + Dispatchers.Default) // SupervisorJob
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private fun stringReplace(string: String, keys: Map<String, Any>): String {
        var result = string
        keys.forEach { (k, v) -> result = result.replace("{$k}", v.toString()) }
        return result
    }

    override fun onInitialize() {
        logger.info("_  _ _ _  _ ____ ____    _ _ _ ____ ____ _    ___  ")
        logger.info("|_/  | |\\ | | __ [__     | | | |  | |__/ |    |  \\ ")
        logger.info("| \\_ | | \\| |__] ___]    |_|_| |__| |  \\ |___ |__/ ")
        logger.info("                                                   ")

        // maybe use koin for these?
        var server: MinecraftServer? = null
        val config = Config("config.yml")
        val discord = Discord(config)

        ServerLifecycleEvents.SERVER_STARTING.register {
            server = it
            scope.launch { discord.init(it) }
        }

        ServerLifecycleEvents.SERVER_STARTED.register {
           scope.launch {
               if (config.messages.started.enabled) {
                   discord.sendMessage(config.messages.started.content)
               }
               discord.setPresence(config.startedActivity)
           }
        }

        ServerLifecycleEvents.SERVER_STOPPED.register {
            runBlocking {
                if (config.messages.stopped.enabled) {
                    discord.sendMessage(config.messages.stopped.content)
                }
                discord.shutdown()
            }
        }

        ServerMessageEvents.CHAT_MESSAGE.register { message, sender, _ ->
            if (!config.messages.chatMessage.enabled) return@register

            scope.launch {
                discord.sendWebhook {
                    username = sender.name.string
                    avatarUrl = config.avatarUrl.replace("{uuid}", sender.uuid.toString())
                    content = stringReplace(config.messages.chatMessage.content, mapOf(
                        "name" to sender.name.string,
                        "uuid" to sender.uuid.toString(),
                        "content" to message.content.string
                    ))
                    allowedMentions {  }
                }
            }
        }

        DiscordBridgeEvents.DISCORD_MESSAGE.register { message ->
            val template = if (message.referencedMessage != null)
                config.messages.discordMessage.reply else
                    config.messages.discordMessage.standard

            if (!template.enabled) return@register
            if (message.interaction != null) return@register

            scope.launch {
                server?.playerManager?.broadcast(
                    Text.of(stringReplace(template.content, mapOf(
                        "name" to Formatters.formatAuthor(message),
                        "content" to Formatters.formatMessage(message),
                        "reference_name" to Formatters.formatReference(message)
                    ))),
                    false
                )
            }
        }

        DiscordBridgeEvents.PLAYER_JOIN.register { player ->
            if (!config.messages.playerJoin.enabled) return@register
            scope.launch {
                discord.sendWebhook {
                    username = player.name.string
                    avatarUrl = config.avatarUrl.replace("{uuid}", player.uuid.toString())
                    content = stringReplace(config.messages.playerJoin.content, mapOf(
                        "name" to player.name.string,
                        "uuid" to player.uuid.toString()
                    ))
                }
            }
        }

        DiscordBridgeEvents.PLAYER_LEAVE.register { player, reason ->
            if (!config.messages.playerLeave.enabled) return@register
            scope.launch {
                discord.sendWebhook {
                    username = player.name.string
                    avatarUrl = config.avatarUrl.replace("{uuid}", player.uuid.toString())
                    content = stringReplace(config.messages.playerLeave.content, mapOf(
                        "name" to player.name.string,
                        "uuid" to player.uuid.toString(),
                        "reason" to reason.string
                    ))
                }
            }
        }

        DiscordBridgeEvents.PLAYER_DEATH.register { player, message ->
            if (!config.messages.playerDeath.enabled) return@register

            scope.launch {
                discord.sendWebhook {
                    username = player.name.string
                    avatarUrl = config.avatarUrl.replace("{uuid}", player.uuid.toString())
                    content = stringReplace(config.messages.playerDeath.content, mapOf(
                        "name" to player.name.string,
                        "uuid" to player.uuid.toString(),
                        "message" to message.string
                    ))
                }
            }
        }

        DiscordBridgeEvents.PLAYER_ADVANCEMENT.register { player, advancement ->
            if (advancement.title.string.isBlank()) return@register
            if (!config.messages.playerAdvancement.enabled) return@register

            scope.launch {
                discord.sendWebhook {
                    username = player.name.string
                    avatarUrl = config.avatarUrl.replace("{uuid}", player.uuid.toString())
                    content = stringReplace(config.messages.playerAdvancement.content, mapOf(
                        "name" to player.name.string,
                        "uuid" to player.uuid.toString(),
                        "title" to advancement.title.string,
                        "description" to advancement.description.string
                    ))
                }
            }
        }

        MinecraftCommands.registerCommands(config, discord)
    }
}