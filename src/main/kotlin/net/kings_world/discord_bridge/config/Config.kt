package net.kings_world.discord_bridge.config

import net.kings_world.discord_bridge.DiscordBridge.configFolder
import net.kings_world.discord_bridge.DiscordBridge.logger

class Config(file: String) {
    private val yaml = Yaml(configFolder.resolve(file).toFile(), file)
    private val config = yaml.config
    private val regex = Regex("(?:https?://)?(?:\\w+\\.)?discord(?:app)?\\.com/api(?:/v\\d+)?/webhooks/(\\d+)/([\\w-]+)(?:/(?:\\w+)?)?")

    var webhookId: String? = null
    var webhookToken: String? = null

    fun reload(): ConfigChanges {
        logger.info("Reloading config file")

        val oldDiscordToken = discordToken
        val oldWebhookUrl = webhookUrl

        yaml.load()
//        if (webhookUrl != oldWebhookUrl) parseWebhookUrl()

        return ConfigChanges(
            discordToken = discordToken != oldDiscordToken,
            webhookUrl = webhookUrl != oldWebhookUrl
        )
    }

    init {
        logger.info("Loading config file")
        yaml.load()
        parseWebhookUrl()
    }

    @Suppress("MemberVisibilityCanBePrivate")
    val webhookUrl: String
        get() = getString("webhook_url", "")

    val avatarUrl: String
        get() = getString("avatar_url", "https://crafthead.net/helm/{uuid}")

    val discordToken: String
        get() = getString("discord_token", "")

    val channelId: String
        get() = getString("channel_id", "")

    val startingActivity: Activity
        get() = Activity(
            status = getString("starting_activity.status", "IDLE"),
            type = getString("starting_activity.type", "NONE"),
            name = getString("starting_activity.name", "Minecraft")
        )

    val startedActivity: Activity
        get() = Activity(
            status = getString("started_activity.status", "ONLINE"),
            type = getString("started_activity.type", "PLAYING"),
            name = getString("started_activity.name", "Minecraft")
        )

    val messages: Messages
        get() = Messages(
            started = Message("server_started", ":white_check_mark: The server has started!"),
            stopped =  Message("server_stopped", ":octagonal_sign: The server has stopped!"),
            chatMessage = Message("chat_message", "{name}: {content}"),
            playerJoin = Message("player_join", ":arrow_right: {name} has joined!"),
            playerLeave = Message("player_leave", ":arrow_left: {name} has left!"),
            playerDeath = Message("player_death", ":skull: {message}"),
            playerAdvancement = Message("player_advancement", ":medal: {name} has completed the advancement **{title}**!"),
            discordMessage = DiscordMessage(
                standard = Message("discord_message.standard", "{name}: {content}"),
                reply = Message("discord_message.reply", "{name} -> {reference_name}: {content}")
            )
        )

    private fun getString(path: String, default: String): String {
        return config.getString(path, default) ?: default
    }

    fun parseWebhookUrl() {
        if (webhookUrl.isBlank()) return

        logger.info("Attempting to parse webhook URL")
        val matcher = regex.toPattern().matcher(webhookUrl)

        if (!matcher.matches()) {
            logger.warn("Failed to parse the webhook URL")
            // setting back to null so if a user runs the reload command, it will reset rather than keep the old id/token
            webhookId = null
            webhookToken = null
            return
        }

        webhookId = matcher.group(1)
        webhookToken = matcher.group(2)
        logger.info("Webhook URL has been parsed successfully")
    }

    data class Activity (
        val status: String,
        val type: String,
        val name: String
    )

    data class Messages (
        val started: Message,
        val stopped: Message,
        val chatMessage: Message,
        val playerJoin: Message,
        val playerLeave: Message,
        val playerDeath: Message,
        val playerAdvancement: Message,
        val discordMessage: DiscordMessage
    )

    data class DiscordMessage (
        val standard: Message,
        val reply: Message
    )

    data class ConfigChanges (
        val discordToken: Boolean,
        val webhookUrl: Boolean
    )

    inner class Message(id: String, def: String) {
        val content = getString("messages.$id.content", def)
        val enabled = config.getBoolean("messages.$id.enabled", true)
        override fun toString(): String {
            return content
        }
    }
}