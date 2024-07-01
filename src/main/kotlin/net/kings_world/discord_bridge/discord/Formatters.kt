package net.kings_world.discord_bridge.discord

import dev.kord.common.entity.optional.coerceToMissing
import dev.kord.core.entity.Message
import dev.kord.core.entity.effectiveName

// message formatters converted from typescript
// https://github.com/Kings-World/fabric-microservice/blob/main/discord-bot/src/lib/format.ts

object Formatters {
    @JvmStatic
    suspend fun formatMessage(message: Message): String {
        var formattedMessage = message.content
        formattedMessage = formatCustomEmojis(formattedMessage)
        formattedMessage = formatMentions(formattedMessage, message)
        formattedMessage = formatMarkdown(formattedMessage)

        return listOf(formattedMessage, formatAttachments(message), formatStickers(message))
            .filter { it.isNotEmpty() }
            .joinToString(" ")
    }

    @JvmStatic
    suspend fun formatAuthor(message: Message): String {
        if (message.webhookId != null) return message.interaction?.getUserOrNull()?.effectiveName ?: message.data.author.username
        return message.getAuthorAsMemberOrNull()?.effectiveName ?: message.author?.effectiveName ?: "unknown author"
    }

    @JvmStatic
    suspend fun formatReference(message: Message): String {
        val reference = message.referencedMessage ?: return "unknown reference"
        return formatAuthor(reference)
    }

    @JvmStatic
    fun formatAttachments(message: Message): String {
        if (message.attachments.isEmpty()) return ""
        return message.attachments.joinToString(" ") {
            "attachment://${it.filename.noSpaces}"
        }
    }

    @JvmStatic
    fun formatStickers(message: Message): String {
        if (message.stickers.isEmpty()) return ""
        return message.stickers.joinToString(" ") {
            "sticker://${it.name.noSpaces}"
        }
    }

    @JvmStatic
    fun formatCustomEmojis(content: String): String {
        return content.replace(Regex("<a?:([^:]+):(\\d{17,20})>")) {
            ":${it.groupValues[1]}:"
        }
    }

    @JvmStatic
    suspend fun formatMentions(content: String, message: Message): String {
        var formattedMessage = content

        message.mentionedUsers.collect { user ->
            val member = message.getGuildOrNull()?.getMemberOrNull(user.id)
            formattedMessage = formattedMessage.replace(Regex("<@!?${user.id}>"), "@${member?.effectiveName ?: user.effectiveName}")
        }

        message.mentionedRoles.collect { role ->
            formattedMessage = formattedMessage.replace(Regex("<@&${role.id}>"), "@${role.name}")
        }

        message.mentionedChannels.collect { channel ->
            formattedMessage = formattedMessage.replace(Regex("<#${channel.id}>"), "#${channel.data.name.coerceToMissing().value ?: "unknown"}")
        }

        return formattedMessage
    }

    @JvmStatic
    fun formatMarkdown(content: String): String {
        // credits to https://github.com/cindyaddoil/Regex-Tuesday-Challenge#challenge-4
        return content
            .replace(Regex("""(^|[^*])\*{3}([^*]+.?[^*])\*{3}(?=[^*]|$)""")) {
                "${it.groupValues[1]}§l§o${it.groupValues[2]}§r§7" // bold and italic
            }
            .replace(Regex("""(^|[^*])\*{2}([^*]+.?[^*])\*{2}(?=[^*]|$)""")) {
                "${it.groupValues[1]}§l${it.groupValues[2]}§r§7" // bold
            }
            .replace(Regex("""(^|[^*])\*([^*]+.?[^*])\*(?=[^*]|$)""")) {
                "${it.groupValues[1]}§o${it.groupValues[2]}§r§7" // italic
            }
            .replace(Regex("""(^|[^_])_{2}([^_].+?[^_])_{2}(?=[^_]|$)""")) {
                "${it.groupValues[1]}§n${it.groupValues[2]}§r§7" // underline
            }
            .replace(Regex("""(^|[^~])~{2}([^~].+?[^~])~{2}(?=[^~]|$)""")) {
                "${it.groupValues[1]}§m${it.groupValues[2]}§r§7"// strikethrough
            }
    }

    @JvmStatic
    private val String.noSpaces: String
        get() = this.replace(Regex("\\s+"), "_")
}