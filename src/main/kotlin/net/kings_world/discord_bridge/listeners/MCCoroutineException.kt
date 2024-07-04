package net.kings_world.discord_bridge.listeners

import com.github.shynixn.mccoroutine.fabric.MCCoroutineExceptionEvent
import net.kings_world.discord_bridge.DiscordBridge
import net.kings_world.discord_bridge.DiscordBridge.logger

object MCCoroutineException : MCCoroutineExceptionEvent {
    override fun onMCCoroutineException(throwable: Throwable, entryPoint: Any): Boolean {
        if (entryPoint != DiscordBridge) return false

        logger.error("A coroutine threw an error in the $entryPoint entrypoint", throwable)
        return true
    }
}