package wtf.blexyel.simpleCameraTweaks.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wtf.blexyel.simpleCameraTweaks.config.Config;
import wtf.blexyel.simpleCameraTweaks.opentrack.OpentrackReceiver;
import wtf.blexyel.simpleCameraTweaks.util.Freelook;
import wtf.blexyel.simpleCameraTweaks.util.FreelookUtils;

@Mixin(Camera.class)
public abstract class CameraMixin {
  @Shadow
  protected abstract void setRotation(float yaw, float pitch);

  @Shadow
  protected abstract void move(float distanceOffset, float verticalOffset, float horizontalOffset);

  @Unique private boolean startFreelook = true;
  @Unique private float smoothedTrackX = 0.0f;
  @Unique private float smoothedTrackY = 0.0f;
  @Unique private float smoothedTrackZ = 0.0f;
  @Unique private float lastVanillaCameraDistance = 0.0f;
  @Unique private static final float MIN_DISPLAY_DISTANCE_METERS = 0.01f;

  @Inject(
      method = "setup",
      at =
      @At(
          value = "INVOKE",
          target = "Lnet/minecraft/client/Camera;move(FFF)V",
          shift = At.Shift.BEFORE))
  public void setup(
      net.minecraft.world.level.Level level,
      Entity focusedEntity,
      boolean thirdPerson,
      boolean inverseView,
      float tickDelta,
      CallbackInfo ci) {
    lastVanillaCameraDistance = 0.0f;
    if (!(focusedEntity instanceof Player)) return;

    if (FreelookUtils.active) {
      Freelook fl = (Freelook) focusedEntity;

      if (Minecraft.getInstance().player != null && startFreelook) {
        fl.setCameraX(Minecraft.getInstance().player.getYRot());
        fl.setCameraY(Minecraft.getInstance().player.getXRot());
        startFreelook = false;
      }

      this.setRotation(fl.getCameraX(), fl.getCameraY());
    }

  }

  @ModifyArg(
      method = "setup",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;move(FFF)V"),
      index = 0)
  private float captureVanillaCameraDistance(float distanceOffset) {
    lastVanillaCameraDistance = Math.abs(distanceOffset);
    return distanceOffset;
  }

  @Inject(method = "setup", at = @At("TAIL"))
  private void applyHeadTrackingTranslation(
      net.minecraft.world.level.Level level,
      Entity focusedEntity,
      boolean thirdPerson,
      boolean inverseView,
      float tickDelta,
      CallbackInfo ci) {
    if (!(focusedEntity instanceof Player) || !Config.enableHeadTracking) return;

    float smoothing = Math.max(0.0f, Math.min(1.0f, Config.headTrackingSmoothing));
    float blend = 1.0f - smoothing;

    smoothedTrackX = smoothedTrackX * smoothing + (float) OpentrackReceiver.x * blend;
    smoothedTrackY = smoothedTrackY * smoothing + (float) OpentrackReceiver.y * blend;
    smoothedTrackZ = smoothedTrackZ * smoothing + (float) OpentrackReceiver.z * blend;

    float displayDistance = (float) Math.max(MIN_DISPLAY_DISTANCE_METERS, Config.displayDistance);
    float scale = 0.35f / displayDistance;

    float horizontalLimit = Math.max(0.01f, Config.headTrackingMaxHorizontal);
    float verticalLimit = Math.max(0.01f, Config.headTrackingMaxVertical);
    float depthLimit = Math.max(0.01f, Config.headTrackingMaxDepth);

    float horizontalOffset = clamp(-smoothedTrackX * scale * Config.headTrackingGainX, -horizontalLimit, horizontalLimit);
    float verticalOffset = clamp(smoothedTrackY * scale * Config.headTrackingGainY, -verticalLimit, verticalLimit);
    float distanceOffset = clamp(-smoothedTrackZ * scale * Config.headTrackingGainZ, -depthLimit, depthLimit);

    float cameraDistanceFromHead = thirdPerson ? lastVanillaCameraDistance : 0.0f;

    float pivotMultiplier = getPivotMultiplier(thirdPerson, cameraDistanceFromHead);
    if (pivotMultiplier > 0.0f) {
      float pivotAngleScale = Math.max(0.0f, Config.thirdPersonPivotAngleScale);
      float yawAngle = horizontalOffset * pivotAngleScale;
      float pitchAngle = verticalOffset * pivotAngleScale;

      float pivotHorizontal = (float) (Math.sin(yawAngle) * cameraDistanceFromHead);
      float pivotVertical = (float) (Math.sin(pitchAngle) * cameraDistanceFromHead);
      float pivotDistance =
          (float) (((Math.cos(yawAngle) - 1.0) + (Math.cos(pitchAngle) - 1.0)) * cameraDistanceFromHead);

      horizontalOffset += pivotHorizontal * pivotMultiplier;
      verticalOffset += pivotVertical * pivotMultiplier;
      distanceOffset += pivotDistance * pivotMultiplier;
    }

    this.move(distanceOffset, verticalOffset, horizontalOffset);
  }

  @Unique
  private static float getPivotMultiplier(boolean thirdPerson, float cameraDistanceFromHead) {
    if (!thirdPerson) {
      return 0.0f;
    }

    float startDistance = Math.max(0.0f, (float) Config.thirdPersonPivotStartDistance);
    float fullDistance = Math.max(startDistance + 0.001f, (float) Config.thirdPersonPivotFullDistance);
    float normalized = (cameraDistanceFromHead - startDistance) / (fullDistance - startDistance);
    float strength = Math.max(0.0f, Config.thirdPersonPivotStrength);
    return clamp(normalized, 0.0f, 1.0f) * strength;
  }

  @Unique
  private static float clamp(float value, float min, float max) {
    return Math.max(min, Math.min(max, value));
  }
}
