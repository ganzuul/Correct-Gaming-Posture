package io.ganzuul.correctgamingposture.config;

import com.google.gson.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import io.ganzuul.correctgamingposture.CorrectGamingPosture;

public class Config {
  public static boolean smooth = false;
  public static boolean enableHeadTrackingToggleShortcut = true;

  public static final String SMOOTHING_MODE_OFF = "OFF";
  public static final String SMOOTHING_MODE_ONE_EURO = "ONE_EURO";
  public static final String TARGETING_MODE_VANILLA = "VANILLA";
  public static final String TARGETING_MODE_ALIGNED = "ALIGNED";

  // Head Tracking Configuration
  public static boolean enableHeadTracking = false;
  public static double displayDistance = 0.3; // meters
  public static String headTrackingSmoothingMode = SMOOTHING_MODE_ONE_EURO;
  public static float oneEuroMinCutoff = 1.0f;
  public static float oneEuroBeta = 0.02f;
  public static float oneEuroDerivativeCutoff = 1.0f;
  public static float headTrackingGainX = 1.0f;
  public static float headTrackingGainY = 1.0f;
  public static float headTrackingGainZ = 1.0f;
  public static float headTrackingMaxHorizontal = 1.25f;
  public static float headTrackingMaxVertical = 0.9f;
  public static float headTrackingMaxDepth = 1.25f;
  public static String targetingMode = TARGETING_MODE_VANILLA;
  public static boolean enablePredictiveTargetingAssist = false;
  public static float predictiveTargetingAssistSeconds = 0.10f;

  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final File CONFIG_FILE = new File("config/correct_gaming_posture.json");
  private static final File LEGACY_CONFIG_FILE = new File("config/simple_camera_tweaks.json");

  public static void load() {
    File sourceFile = CONFIG_FILE;
    boolean migratedFromLegacy = false;

    if (!CONFIG_FILE.exists() && LEGACY_CONFIG_FILE.exists()) {
      sourceFile = LEGACY_CONFIG_FILE;
      migratedFromLegacy = true;
      CorrectGamingPosture.LOGGER.info("Found legacy config at {}. Migrating to {}.", LEGACY_CONFIG_FILE.getPath(),
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
    if (json.has("enableHeadTrackingToggleShortcut")) {
      enableHeadTrackingToggleShortcut = json.get("enableHeadTrackingToggleShortcut").getAsBoolean();
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
    if (json.has("headTrackingSmoothingMode")) {
      headTrackingSmoothingMode = parseSmoothingMode(json.get("headTrackingSmoothingMode").getAsString());
    }
    if (json.has("oneEuroMinCutoff")) {
      oneEuroMinCutoff = json.get("oneEuroMinCutoff").getAsFloat();
    }
    if (json.has("oneEuroBeta")) {
      oneEuroBeta = json.get("oneEuroBeta").getAsFloat();
    }
    if (json.has("oneEuroDerivativeCutoff")) {
      oneEuroDerivativeCutoff = json.get("oneEuroDerivativeCutoff").getAsFloat();
    }
    if (json.has("headTrackingSmoothing")) {
      float legacySmoothing = json.get("headTrackingSmoothing").getAsFloat();
      if (legacySmoothing <= 0.0f) {
        headTrackingSmoothingMode = SMOOTHING_MODE_OFF;
      } else {
        headTrackingSmoothingMode = SMOOTHING_MODE_ONE_EURO;
        oneEuroBeta = Math.max(0.0f, legacySmoothing * 0.08f);
      }
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
    if (json.has("targetingMode")) {
      targetingMode = parseTargetingMode(json.get("targetingMode").getAsString());
    }
    if (json.has("enablePredictiveTargetingAssist")) {
      enablePredictiveTargetingAssist = json.get("enablePredictiveTargetingAssist").getAsBoolean();
    }
    if (json.has("predictiveTargetingAssistSeconds")) {
      predictiveTargetingAssistSeconds = json.get("predictiveTargetingAssistSeconds").getAsFloat();
    }
  }

  public static void save() {
    try {
      CONFIG_FILE.getParentFile().mkdirs();

      JsonObject json = new JsonObject();
      json.addProperty("smooth", smooth);
      json.addProperty("enableHeadTrackingToggleShortcut", enableHeadTrackingToggleShortcut);
      json.addProperty("enableHeadTracking", enableHeadTracking);
      json.addProperty("displayDistance", displayDistance);
      json.addProperty("headTrackingSmoothingMode", headTrackingSmoothingMode);
      json.addProperty("oneEuroMinCutoff", oneEuroMinCutoff);
      json.addProperty("oneEuroBeta", oneEuroBeta);
      json.addProperty("oneEuroDerivativeCutoff", oneEuroDerivativeCutoff);
      json.addProperty("headTrackingGainX", headTrackingGainX);
      json.addProperty("headTrackingGainY", headTrackingGainY);
      json.addProperty("headTrackingGainZ", headTrackingGainZ);
      json.addProperty("headTrackingMaxHorizontal", headTrackingMaxHorizontal);
      json.addProperty("headTrackingMaxVertical", headTrackingMaxVertical);
      json.addProperty("headTrackingMaxDepth", headTrackingMaxDepth);
      json.addProperty("targetingMode", targetingMode);
      json.addProperty("enablePredictiveTargetingAssist", enablePredictiveTargetingAssist);
      json.addProperty("predictiveTargetingAssistSeconds", predictiveTargetingAssistSeconds);

      try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
        GSON.toJson(json, writer);
      }
      // Main.LOGGER.info("Config saved");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static boolean isOneEuroSmoothing() {
    return SMOOTHING_MODE_ONE_EURO.equals(headTrackingSmoothingMode);
  }

  public static boolean isAlignedTargeting() {
    return TARGETING_MODE_ALIGNED.equals(targetingMode);
  }

  private static String parseSmoothingMode(String mode) {
    if (mode == null) {
      return SMOOTHING_MODE_ONE_EURO;
    }
    String normalized = mode.trim().toUpperCase();
    if (SMOOTHING_MODE_OFF.equals(normalized)) {
      return SMOOTHING_MODE_OFF;
    }
    return SMOOTHING_MODE_ONE_EURO;
  }

  private static String parseTargetingMode(String mode) {
    if (mode == null) {
      return TARGETING_MODE_VANILLA;
    }
    String normalized = mode.trim().toUpperCase();
    if (TARGETING_MODE_ALIGNED.equals(normalized)) {
      return TARGETING_MODE_ALIGNED;
    }
    return TARGETING_MODE_VANILLA;
  }
}