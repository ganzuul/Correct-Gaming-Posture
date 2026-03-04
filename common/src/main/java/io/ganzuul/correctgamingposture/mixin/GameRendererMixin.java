package io.ganzuul.correctgamingposture.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import io.ganzuul.correctgamingposture.config.Config;
import io.ganzuul.correctgamingposture.util.HeadTrackingState;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
  @Shadow @Final Minecraft minecraft;

  @Inject(method = "pick", at = @At("TAIL"))
  private void onPick(float tickDelta, CallbackInfo ci) {
    if (minecraft.player == null || minecraft.level == null || minecraft.gameMode == null) {
      return;
    }

    if (!Config.enableHeadTracking || !Config.isAlignedTargeting()) {
      return;
    }

    if (!minecraft.options.getCameraType().isFirstPerson()) {
      return;
    }

    Entity viewEntity = minecraft.getCameraEntity();
    if (viewEntity == null) {
      return;
    }

    Vec3 baseEyePos = viewEntity.getEyePosition(tickDelta);
    Vec3 origin = baseEyePos.add(HeadTrackingState.asVec3());
    Vec3 direction = viewEntity.getViewVector(tickDelta);
    double reach =
        Math.max(minecraft.player.blockInteractionRange(), minecraft.player.entityInteractionRange());
    Vec3 end = origin.add(direction.scale(reach));

    BlockHitResult blockHit =
        minecraft.level.clip(
            new ClipContext(origin, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, viewEntity));

    minecraft.hitResult = blockHit;
    if (blockHit.getType() == HitResult.Type.MISS) {
      minecraft.hitResult =
          BlockHitResult.miss(
              end,
              net.minecraft.core.Direction.getApproximateNearest(direction),
              net.minecraft.core.BlockPos.containing(end));
    }
  }
}
