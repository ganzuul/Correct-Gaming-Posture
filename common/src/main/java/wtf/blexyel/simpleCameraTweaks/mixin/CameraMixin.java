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
  @Unique private static final float MAX_HEAD_OFFSET = 0.35f;

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

    float displayDistance = (float) Math.max(0.1d, Config.displayDistance);
    float scale = 0.35f / displayDistance;

    float horizontalOffset = clamp(-smoothedTrackX * scale, -MAX_HEAD_OFFSET, MAX_HEAD_OFFSET);
    float verticalOffset = clamp(smoothedTrackY * scale, -MAX_HEAD_OFFSET, MAX_HEAD_OFFSET);
    float distanceOffset = clamp(-smoothedTrackZ * scale, -MAX_HEAD_OFFSET, MAX_HEAD_OFFSET);

    this.move(distanceOffset, verticalOffset, horizontalOffset);
  }

  @Unique
  private static float clamp(float value, float min, float max) {
    return Math.max(min, Math.min(max, value));
  }
}
