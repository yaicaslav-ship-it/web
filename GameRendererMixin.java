package com.example.jimhelper.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.item.Items;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Shadow @Final private MinecraftClient client;

    @Inject(method = "updateCrosshairTarget", at = @At("RETURN"))
    private void onUpdateCrosshairTarget(float tickDelta, CallbackInfo ci) {
        if (this.client.player == null || this.client.world == null) return;

        boolean holdingWeb = this.client.player.getMainHandStack().isOf(Items.COBWEB)
                          || this.client.player.getOffHandStack().isOf(Items.COBWEB);

        if (holdingWeb) {
            if (this.client.crosshairTarget != null && this.client.crosshairTarget.getType() == HitResult.Type.ENTITY) {
                double interactionRange = this.client.player.getBlockInteractionRange();
                this.client.crosshairTarget = this.client.player.raycast(interactionRange, tickDelta, false);
            }
        }
    }
}
