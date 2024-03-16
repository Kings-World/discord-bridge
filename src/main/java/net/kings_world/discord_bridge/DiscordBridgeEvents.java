package net.kings_world.discord_bridge;

import dev.kord.core.entity.Message;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class DiscordBridgeEvents {
    public static final Event<PlayerJoin> PLAYER_JOIN = EventFactory.createArrayBacked(PlayerJoin.class, callbacks -> player -> {
        for (PlayerJoin callback : callbacks) {
            callback.onPlayerJoin(player);
        }
    });

    public static final Event<PlayerLeave> PLAYER_LEAVE = EventFactory.createArrayBacked(PlayerLeave.class, callbacks -> (player, reason) -> {
        for (PlayerLeave callback : callbacks) {
            callback.onPlayerLeave(player, reason);
        }
    });

    public static final Event<PlayerDeath> PLAYER_DEATH = EventFactory.createArrayBacked(PlayerDeath.class, callbacks -> (entity, reason) -> {
        for (PlayerDeath callback : callbacks) {
            callback.onPlayerDeath(entity, reason);
        }
    });

    public static final Event<PlayerAdvancement> PLAYER_ADVANCEMENT = EventFactory.createArrayBacked(PlayerAdvancement.class, callbacks -> (player, advancement) -> {
        for (PlayerAdvancement callback : callbacks) {
            callback.onPlayerAdvancement(player, advancement);
        }
    });

    public static final Event<DiscordMessage> DISCORD_MESSAGE = EventFactory.createArrayBacked(DiscordMessage.class, callbacks -> message -> {
        for (DiscordMessage callback : callbacks) {
            callback.onDiscordMessage(message);
        }
    });

    @FunctionalInterface
    public interface PlayerJoin {
        void onPlayerJoin(ServerPlayerEntity player);
    }

    @FunctionalInterface
    public interface PlayerLeave {
        void onPlayerLeave(ServerPlayerEntity player, Text reason);
    }

    @FunctionalInterface
    public interface PlayerDeath {
        void onPlayerDeath(LivingEntity entity, Text reason);
    }

    @FunctionalInterface
    public interface PlayerAdvancement {
        void onPlayerAdvancement(ServerPlayerEntity player, AdvancementDisplay advancement);
    }

    @FunctionalInterface
    public interface DiscordMessage {
        void onDiscordMessage(Message message);
    }
}
