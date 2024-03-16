package net.kings_world.discord_bridge.mixins;

import net.kings_world.discord_bridge.DiscordBridge;
import net.kings_world.discord_bridge.DiscordBridgeEvents;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerAdvancementTracker.class)
public abstract class PlayerAdvancementTrackerMixin {
    @Shadow
    private ServerPlayerEntity owner;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/text/Text;Z)V"), method = "grantCriterion")
    private void onAdvancementBroadcast(Advancement advancement, String criterionName, CallbackInfoReturnable<Boolean> cir) {
        AdvancementDisplay display = advancement.getDisplay();
        if (display == null) {
            DiscordBridge.INSTANCE.getLogger().warn("Advancement " + advancement.getId().toString() + " has no display!");
            return;
        }
        DiscordBridge.INSTANCE.getLogger().info(owner.getName().getString() + " unlocked " + display.getTitle().getString());
        DiscordBridgeEvents.PLAYER_ADVANCEMENT.invoker().onPlayerAdvancement(owner, display);
    }
}