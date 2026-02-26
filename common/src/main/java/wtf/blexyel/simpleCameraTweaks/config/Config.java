package wtf.blexyel.simpleCameraTweaks.config;

import com.google.gson.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import wtf.blexyel.simpleCameraTweaks.SimpleCameraTweaks;

public class Config {
  public static boolean smooth = false;
  public static boolean offhand = false;

  // Head Tracking Configuration
  public static boolean enableHeadTracking = false;
  public static double displayDistance = 0.3; // meters
  public static float headTrackingSmoothing = 0.8f; // 0-1, higher = smoother
  public static float headTrackingGainX = 1.0f;
  public static float headTrackingGainY = 1.0f;
  public static float headTrackingGainZ = 1.0f;
  public static float headTrackingMaxHorizontal = 1.25f;
  public static float headTrackingMaxVertical = 0.9f;
  public static float headTrackingMaxDepth = 1.25f;
  public static double thirdPersonPivotStartDistance = 0.75; // blocks
  public static double thirdPersonPivotFullDistance = 4.0; // blocks
  public static float thirdPersonPivotStrength = 1.0f; // multiplier
  public static float thirdPersonPivotAngleScale = 0.25f; // radians per block of offset

  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final File CONFIG_FILE = new File("config/correct_gaming_posture.json");
  private static final File LEGACY_CONFIG_FILE = new File("config/simple_camera_tweaks.json");

  public static void load() {
    File sourceFile = CONFIG_FILE;
    boolean migratedFromLegacy = false;

    if (!CONFIG_FILE.exists() && LEGACY_CONFIG_FILE.exists()) {
      sourceFile = LEGACY_CONFIG_FILE;
      migratedFromLegacy = true;
      SimpleCameraTweaks.LOGGER.info("Found legacy config at {}. Migrating to {}.", LEGACY_CONFIG_FILE.getPath(),
          CONFIG_FILE.getPath());
    }

    if (!sourceFile.exists()) {
      save(); // create default config file if missing
      return;
    }

    try (FileReader reader = new FileReader(sourceFile)) {
      JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
      loadFromJson(json);

      if (migratedFromLegacy) {
        save();
      }

    } catch (IOException | JsonParseException e) {
      e.printStackTrace();
    }
  }

  private static void loadFromJson(JsonObject json) {
    // Migrate "enabled" -> "offhand" if present
    if (json.has("enabled") && !json.has("offhand")) {
      offhand = json.get("enabled").getAsBoolean();
      SimpleCameraTweaks.LOGGER.info("Migrated 'enabled' to 'offhand'");
    } else if (json.has("offhand")) {
      offhand = json.get("offhand").getAsBoolean();
    }

    if (json.has("smooth")) {
      smooth = json.get("smooth").getAsBoolean();
    }

    // Load head tracking config
    if (json.has("enableHeadTracking")) {
      enableHeadTracking = json.get("enableHeadTracking").getAsBoolean();
    }
    if (json.has("displayDistance")) {
      displayDistance = json.get("displayDistance").getAsDouble();
    }
    if (json.has("headTrackingSmoothing")) {
      headTrackingSmoothing = json.get("headTrackingSmoothing").getAsFloat();
    }
    if (json.has("headTrackingGainX")) {
      headTrackingGainX = json.get("headTrackingGainX").getAsFloat();
    }
    if (json.has("headTrackingGainY")) {
      headTrackingGainY = json.get("headTrackingGainY").getAsFloat();
    }
    if (json.has("headTrackingGainZ")) {
      headTrackingGainZ = json.get("headTrackingGainZ").getAsFloat();
    }
    if (json.has("headTrackingMaxHorizontal")) {
      headTrackingMaxHorizontal = json.get("headTrackingMaxHorizontal").getAsFloat();
    }
    if (json.has("headTrackingMaxVertical")) {
      headTrackingMaxVertical = json.get("headTrackingMaxVertical").getAsFloat();
    }
    if (json.has("headTrackingMaxDepth")) {
      headTrackingMaxDepth = json.get("headTrackingMaxDepth").getAsFloat();
    }
    if (json.has("thirdPersonPivotStartDistance")) {
      thirdPersonPivotStartDistance = json.get("thirdPersonPivotStartDistance").getAsDouble();
    }
    if (json.has("thirdPersonPivotFullDistance")) {
      thirdPersonPivotFullDistance = json.get("thirdPersonPivotFullDistance").getAsDouble();
    }
    if (json.has("thirdPersonPivotStrength")) {
      thirdPersonPivotStrength = json.get("thirdPersonPivotStrength").getAsFloat();
    }
    if (json.has("thirdPersonPivotAngleScale")) {
      thirdPersonPivotAngleScale = json.get("thirdPersonPivotAngleScale").getAsFloat();
    }
  }

  public static void save() {
    try {
      CONFIG_FILE.getParentFile().mkdirs();

      JsonObject json = new JsonObject();
      json.addProperty("smooth", smooth);
      json.addProperty("offhand", offhand);
      json.addProperty("enableHeadTracking", enableHeadTracking);
      json.addProperty("displayDistance", displayDistance);
      json.addProperty("headTrackingSmoothing", headTrackingSmoothing);
      json.addProperty("headTrackingGainX", headTrackingGainX);
      json.addProperty("headTrackingGainY", headTrackingGainY);
      json.addProperty("headTrackingGainZ", headTrackingGainZ);
      json.addProperty("headTrackingMaxHorizontal", headTrackingMaxHorizontal);
      json.addProperty("headTrackingMaxVertical", headTrackingMaxVertical);
      json.addProperty("headTrackingMaxDepth", headTrackingMaxDepth);
      json.addProperty("thirdPersonPivotStartDistance", thirdPersonPivotStartDistance);
      json.addProperty("thirdPersonPivotFullDistance", thirdPersonPivotFullDistance);
      json.addProperty("thirdPersonPivotStrength", thirdPersonPivotStrength);
      json.addProperty("thirdPersonPivotAngleScale", thirdPersonPivotAngleScale);

      try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
        GSON.toJson(json, writer);
      }
      // Main.LOGGER.info("Config saved");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}