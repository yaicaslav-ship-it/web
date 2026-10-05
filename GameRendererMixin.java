package com.example.jimhelper.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.RaycastContext;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Shadow @Final private MinecraftClient client;

    @Inject(method = "updateCrosshairTarget", at = @At("TAIL"))
    private void fixCrosshairTargetForWeb(float tickDelta, CallbackInfo ci) {
        if (this.client.player == null || this.client.world == null) return;

        // Проверяем, держит ли игрок паутину в основной или левой руке
        boolean holdingWeb = this.client.player.getMainHandStack().isOf(Items.COBWEB)
                          || this.client.player.getOffHandStack().isOf(Items.COBWEB);

        if (!holdingWeb) return;

        // Если прицел захватил сущность (игрока, моба, стойку)
        if (this.client.crosshairTarget instanceof EntityHitResult) {
            Entity camera = this.client.getCameraEntity();
            if (camera == null) camera = this.client.player;

            // Дальность взаимодействия с блоками в 1.21.4
            double blockRange = this.client.player.getBlockInteractionRange();

            // Трассируем луч строго по блокам от позиции глаз камеры
            var eyePos = camera.getCameraPosVec(tickDelta);
            var rotVec = camera.getRotationVec(tickDelta);
            var endPos = eyePos.add(rotVec.x * blockRange, rotVec.y * blockRange, rotVec.z * blockRange);

            BlockHitResult blockHit = this.client.world.raycast(new RaycastContext(
                eyePos,
                endPos,
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE,
                camera
            ));

            // Перенаправляем прицел на блок позади сущности
            this.client.crosshairTarget = blockHit;
            // Обнуляем захваченную сущность в клиенте
            this.client.targetedEntity = null;
        }
    }
}
