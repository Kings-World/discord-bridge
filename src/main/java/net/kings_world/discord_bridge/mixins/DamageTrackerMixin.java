package net.kings_world.discord_bridge.mixins;

import net.kings_world.discord_bridge.DiscordBridgeEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageTracker;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DamageTracker.class)
public class DamageTrackerMixin {
    @Shadow
    @Final
    private LivingEntity entity;

    @Inject(at = @At(value = "RETURN"), method = "getDeathMessage")
    private void onGetDeathMessage(CallbackInfoReturnable<Text> cir) {
        DiscordBridgeEvents.PLAYER_DEATH.invoker().onPlayerDeath(entity, cir.getReturnValue());
    }
}