package com.example.jimhelper.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.RaycastContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow public ClientPlayerEntity player;
    @Shadow public ClientWorld world;
    @Shadow public HitResult crosshairTarget;
    @Shadow public Entity targetedEntity;

    @Inject(method = "doItemUse", at = @At("HEAD"))
    private void redirectWebPlacement(CallbackInfo ci) {
        if (this.player == null || this.world == null) return;

        // Проверяем наличие паутины в руках
        boolean holdingWeb = this.player.getMainHandStack().isOf(Items.COBWEB)
                          || this.player.getOffHandStack().isOf(Items.COBWEB);

        if (!holdingWeb) return;

        // Если прицел захватил сущность (игрока, моба, стойку)
        if (this.crosshairTarget != null && this.crosshairTarget.getType() == HitResult.Type.ENTITY) {
            double range = this.player.getBlockInteractionRange();

            // Трассируем луч строго по блокам сквозь сущность
            var eyePos = this.player.getEyePos();
            var rotVec = this.player.getRotationVec(1.0F);
            var endPos = eyePos.add(rotVec.x * range, rotVec.y * range, rotVec.z * range);

            BlockHitResult blockHit = this.world.raycast(new RaycastContext(
                eyePos,
                endPos,
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE,
                this.player
            ));

            // Если за хитбоксом найден опорный блок — переключаем цель на него
            if (blockHit.getType() == HitResult.Type.BLOCK) {
                this.crosshairTarget = blockHit;
                this.targetedEntity = null;
            }
        }
    }
}
