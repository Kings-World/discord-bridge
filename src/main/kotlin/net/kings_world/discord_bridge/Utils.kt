package net.kings_world.discord_bridge

import me.lucko.fabric.api.permissions.v0.Permissions
import net.fabricmc.loader.api.FabricLoader
import net.kings_world.discord_bridge.DiscordBridge.MOD_ID
import net.minecraft.server.command.ServerCommandSource
import java.util.function.Predicate

object Utils {
    fun stringReplace(string: String, keys: Map<String, Any>): String {
        var result = string
        keys.forEach { (k, v) -> result = result.replace("{$k}", v.toString()) }
        return result
    }

    fun requirePermission(permission: String, defaultLevel: Int): Predicate<ServerCommandSource> {
        return when (isFabricPermissionsAPILoaded()) {
            true -> Permissions.require("$MOD_ID.$permission", defaultLevel)
            false -> Predicate { source -> source.hasPermissionLevel(defaultLevel) }
        }
    }

    private fun isFabricPermissionsAPILoaded(): Boolean {
        return FabricLoader.getInstance().isModLoaded("fabric-permissions-api-v0")
    }
}