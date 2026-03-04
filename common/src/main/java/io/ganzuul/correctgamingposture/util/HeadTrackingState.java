package io.ganzuul.correctgamingposture.util;

import net.minecraft.world.phys.Vec3;

public final class HeadTrackingState {
  private static volatile float offsetX = 0.0f;
  private static volatile float offsetY = 0.0f;
  private static volatile float offsetZ = 0.0f;

  private HeadTrackingState() {}

  public static void setOffsets(float x, float y, float z) {
    offsetX = x;
    offsetY = y;
    offsetZ = z;
  }

  public static Vec3 asVec3() {
    return new Vec3(offsetX, offsetY, offsetZ);
  }

  public static void clear() {
    setOffsets(0.0f, 0.0f, 0.0f);
  }
}
