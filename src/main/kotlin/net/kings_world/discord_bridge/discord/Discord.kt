package net.kings_world.discord_bridge.discord

import dev.kord.common.entity.PresenceStatus
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.executeIgnored
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
import net.kings_world.discord_bridge.DiscordBridge.config

object Discord {
    lateinit var kord: Kord

    private suspend fun init() {
        if (config.discordToken.isBlank()) {
            logger.warn("Discord token is not set! The bot will not be initialized.")
            return
        }

        logger.info("Initializing the Discord bot")
        kord = Kord(config.discordToken)
    }

    private fun registerEvents() {
        if (::kord.isInitialized.not()) {
            logger.warn("Discord bot is not initialized! Events will not be registered.")
            return
        }

        logger.info("Registering Discord events")

        kord.on<ReadyEvent> {
            logger.info("Logged into Discord as ${self.effectiveName}")
            DiscordCommands.registerCommands()
        }

        kord.on<MessageCreateEvent> {
            if (message.author?.isBot == true) return@on
            if (message.channelId.toString() != config.channelId) return@on
            if (config.webhookId != null && message.webhookId == Snowflake(config.webhookId!!)) return@on
            DiscordBridgeEvents.DISCORD_MESSAGE.invoker().onDiscordMessage(message)
        }

        DiscordCommands.registerInteractionEvents()
    }

    private suspend fun login(activity: Config.Activity) {
        if (::kord.isInitialized.not()) {
            logger.warn("Discord bot is not initialized! The bot will not be started.")
            return
        }

        logger.info("Logging into Discord")

        kord.login {
            intents {
                @OptIn(PrivilegedIntent::class)
                +Intent.MessageContent
                +Intent.GuildMessages
            }
            presence {
                status = PresenceStatus.from(activity.status.lowercase())
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

    suspend fun prepare(activity: Config.Activity) {
        init()
        registerEvents()
        login(activity)
    }

    suspend fun shutdown() {
        if (::kord.isInitialized.not()) {
            logger.warn("Discord bot is not initialized! The bot will not be shutdown.")
            return
        }

        logger.info("Shutting down the Discord bot")
        kord.shutdown()
    }

    suspend fun sendWebhook(builder: WebhookMessageCreateBuilder.() -> Unit) {
        if (::kord.isInitialized.not()) {
            logger.warn("Discord bot is not initialized! The webhook will not be sent.")
            return
        }

        if (config.webhookId == null || config.webhookToken == null) {
            logger.warn("Webhook ID or token is not set! The webhook will not be sent.")
            return
        }

        val webhook = kord.getWebhookOrNull(Snowflake(config.webhookId!!))
        if (webhook == null) {
            logger.error("Webhook with ID ${config.webhookId} not found! The webhook will not be sent.")
            return
        }

        webhook.executeIgnored(config.webhookToken!!, null, builder)
    }

    suspend fun sendMessage(message: String) {
        if (::kord.isInitialized.not()) {
            logger.warn("Discord bot is not initialized! The message will not be sent.")
            return
        }

        if (message.isBlank()) {
            logger.warn("The message is empty! The message will not be sent.")
            return
        }

        val channel = kord.getChannelOf<TextChannel>(Snowflake(config.channelId))
        if (channel == null) {
            logger.error("Channel with ID ${config.channelId} not found! The message will not be sent.")
            return
        }

        channel.createMessage { content = message; allowedMentions {  } }
    }

    suspend fun sendConfigMessage(message: Config.Message) {
        if (!message.enabled) return
        sendMessage(message.content)
    }

    suspend fun setPresence(activity: Config.Activity)  {
        if (::kord.isInitialized.not()) {
            logger.warn("Discord bot is not initialized! The presence will not be set.")
            return
        }

        kord.editPresence {
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