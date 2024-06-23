package net.kings_world.discord_bridge.discord

import dev.kord.common.annotation.KordExperimental
import dev.kord.common.annotation.KordUnsafe
import dev.kord.common.entity.PresenceStatus
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.execute
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.entity.effectiveName
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import dev.kord.rest.builder.message.allowedMentions
import dev.kord.rest.builder.message.create.WebhookMessageCreateBuilder
import net.kings_world.discord_bridge.DiscordBridge.logger
import net.kings_world.discord_bridge.DiscordBridgeEvents
import net.kings_world.discord_bridge.config.Config
import net.minecraft.server.MinecraftServer

@OptIn(KordUnsafe::class, KordExperimental::class)
class Discord(private val config: Config) {
    private var bot: Kord? = null

    suspend fun init(server: MinecraftServer, reloaded: Boolean = false) {
        if (config.discordToken.isBlank()) return

        logger.info("Initializing the Discord bot")
        val kord = Kord(config.discordToken)
        bot = kord

        kord.on<ReadyEvent> {
            logger.info("Logged into Discord as ${self.effectiveName}")
            DiscordCommands.registerCommands(kord)
        }

        kord.on<MessageCreateEvent> {
            if (message.author?.isBot == true) return@on
            if (message.channelId.toString() != config.channelId) return@on
            if (config.webhookId != null && message.webhookId == Snowflake(config.webhookId!!)) return@on
            DiscordBridgeEvents.DISCORD_MESSAGE.invoker().onDiscordMessage(message)
        }

        DiscordCommands.registerEvents(kord, server)

        logger.info("Logging into Discord")
        val presence = if (reloaded) config.startedActivity else config.startingActivity

        kord.login {
            intents {
                @OptIn(PrivilegedIntent::class)
                +Intent.MessageContent
                +Intent.GuildMessages
            }
            presence {
                status = PresenceStatus.from(presence.status.lowercase())
                when (presence.type.lowercase()) {
                    "playing" -> playing(presence.name)
                    "listening" -> listening(presence.name)
                    "watching" -> watching(presence.name)
                    "competing" -> competing(presence.name)
                    "custom" -> this.state = presence.name
                }
            }
        }
    }

    suspend fun shutdown() {
        if (bot != null) logger.info("Closing connection to Discord")
        bot?.shutdown()
        bot = null
    }

    suspend fun sendWebhook(builder: WebhookMessageCreateBuilder.() -> Unit) {
        if (bot == null || config.webhookId == null || config.webhookToken == null) return
        bot!!.unsafe.webhook(Snowflake(config.webhookId!!)).execute(config.webhookToken!!, null, builder)
    }

    suspend fun sendMessage(message: String) {
        if (bot == null || message.isBlank()) return

        val channel = bot!!.getChannelOf<TextChannel>(Snowflake(config.channelId))
        if (channel == null) {
            logger.error("DiscordBridge(message): The provided channel ID is invalid!")
            return
        }

        channel.createMessage { content = message; allowedMentions {  } }
    }

    suspend fun setPresence(activity: Config.Activity) {
        bot?.editPresence {
            this.status = PresenceStatus.from(activity.status.lowercase())
            when (activity.type.lowercase()) {
                "playing" -> playing(activity.name)
                "listening" -> listening(activity.name)
                "watching" -> watching(activity.name)
                "competing" -> competing(activity.name)
                "custom" -> this.state = activity.name
            }
        }
    }
}