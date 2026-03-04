package io.ganzuul.correctgamingposture.mixin;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import io.ganzuul.correctgamingposture.config.Config;
import io.ganzuul.correctgamingposture.opentrack.OpentrackReceiver;
import io.ganzuul.correctgamingposture.util.HeadTrackingState;
import io.ganzuul.correctgamingposture.util.OneEuroFilter;

@Mixin(Camera.class)
public abstract class CameraMixin {
  @Shadow
  protected abstract void move(float distanceOffset, float verticalOffset, float horizontalOffset);

  @Unique private long lastFilterTimeNanos = 0L;
  @Unique private static final float MIN_DISPLAY_DISTANCE_METERS = 0.01f;
  @Unique private static final float MAX_PREDICTIVE_SECONDS = 0.5f;
  @Unique private final OneEuroFilter trackFilterX = new OneEuroFilter();
  @Unique private final OneEuroFilter trackFilterY = new OneEuroFilter();
  @Unique private final OneEuroFilter trackFilterZ = new OneEuroFilter();

  @Inject(method = "setup", at = @At("TAIL"))
  private void applyHeadTrackingTranslation(
      net.minecraft.world.level.Level level,
      Entity focusedEntity,
      boolean thirdPerson,
      boolean inverseView,
      float tickDelta,
      CallbackInfo ci) {
    if (!(focusedEntity instanceof Player) || !Config.enableHeadTracking) {
      resetHeadTrackingState();
      return;
    }

    if (thirdPerson) {
      HeadTrackingState.clear();
      return;
    }

    float dtSeconds = getFrameDeltaSeconds();

    float rawTrackX = (float) OpentrackReceiver.x;
    float rawTrackY = (float) OpentrackReceiver.y;
    float rawTrackZ = (float) OpentrackReceiver.z;

    float filteredTrackX;
    float filteredTrackY;
    float filteredTrackZ;
    if (Config.isOneEuroSmoothing()) {
      filteredTrackX =
          trackFilterX.filter(
              rawTrackX,
              dtSeconds,
              Config.oneEuroMinCutoff,
              Config.oneEuroBeta,
              Config.oneEuroDerivativeCutoff);
      filteredTrackY =
          trackFilterY.filter(
              rawTrackY,
              dtSeconds,
              Config.oneEuroMinCutoff,
              Config.oneEuroBeta,
              Config.oneEuroDerivativeCutoff);
      filteredTrackZ =
          trackFilterZ.filter(
              rawTrackZ,
              dtSeconds,
              Config.oneEuroMinCutoff,
              Config.oneEuroBeta,
              Config.oneEuroDerivativeCutoff);
    } else {
      filteredTrackX = rawTrackX;
      filteredTrackY = rawTrackY;
      filteredTrackZ = rawTrackZ;
      resetFiltersOnly();
    }

    float displayDistance = (float) Math.max(MIN_DISPLAY_DISTANCE_METERS, Config.displayDistance);
    float scale = 0.35f / displayDistance;

    float horizontalLimit = Math.max(0.01f, Config.headTrackingMaxHorizontal);
    float verticalLimit = Math.max(0.01f, Config.headTrackingMaxVertical);
    float depthLimit = Math.max(0.01f, Config.headTrackingMaxDepth);

    float horizontalOffset = clamp(-filteredTrackX * scale * Config.headTrackingGainX, -horizontalLimit, horizontalLimit);
    float verticalOffset = clamp(filteredTrackY * scale * Config.headTrackingGainY, -verticalLimit, verticalLimit);
    float distanceOffset = clamp(-filteredTrackZ * scale * Config.headTrackingGainZ, -depthLimit, depthLimit);

    if (Config.enablePredictiveTargetingAssist && Config.isAlignedTargeting()) {
      float predictiveSeconds = clamp(Config.predictiveTargetingAssistSeconds, 0.0f, MAX_PREDICTIVE_SECONDS);
      float predictiveScale = predictiveSeconds / Math.max(1.0f / 240.0f, dtSeconds);
      horizontalOffset += (horizontalOffset - (float) HeadTrackingState.asVec3().x) * predictiveScale;
      verticalOffset += (verticalOffset - (float) HeadTrackingState.asVec3().y) * predictiveScale;
      distanceOffset += (distanceOffset - (float) HeadTrackingState.asVec3().z) * predictiveScale;
    }

    HeadTrackingState.setOffsets(horizontalOffset, verticalOffset, distanceOffset);

    this.move(distanceOffset, verticalOffset, horizontalOffset);
  }

  @Unique
  private float getFrameDeltaSeconds() {
    long now = System.nanoTime();
    if (lastFilterTimeNanos == 0L) {
      lastFilterTimeNanos = now;
      return 1.0f / 60.0f;
    }
    float delta = (now - lastFilterTimeNanos) / 1_000_000_000.0f;
    lastFilterTimeNanos = now;
    return clamp(delta, 1.0f / 240.0f, 0.2f);
  }

  @Unique
  private void resetFiltersOnly() {
    trackFilterX.reset();
    trackFilterY.reset();
    trackFilterZ.reset();
    lastFilterTimeNanos = 0L;
  }

  @Unique
  private void resetHeadTrackingState() {
    resetFiltersOnly();
    HeadTrackingState.clear();
  }

  @Unique
  private static float clamp(float value, float min, float max) {
    return Math.max(min, Math.min(max, value));
  }
}
