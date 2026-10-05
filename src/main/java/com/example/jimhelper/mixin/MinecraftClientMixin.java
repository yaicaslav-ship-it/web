package com.example.jimhelper.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow public ClientPlayerEntity player;
    @Shadow public HitResult crosshairTarget;

    @Inject(method = "doItemUse", at = @At("HEAD"))
    private void onDoItemUse(CallbackInfo ci) {
        if (this.player == null) return;

        boolean holdingWeb = this.player.getMainHandStack().isOf(Items.COBWEB)
                          || this.player.getOffHandStack().isOf(Items.COBWEB);

        // Если держим паутину и цель - сущность, принудительно сбрасываем привязку
        if (holdingWeb && this.crosshairTarget != null && this.crosshairTarget.getType() == HitResult.Type.ENTITY) {
            ((MinecraftClient) (Object) this).targetedEntity = null;
        }
    }
}
