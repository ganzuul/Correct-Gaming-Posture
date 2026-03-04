package io.ganzuul.correctgamingposture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.ganzuul.correctgamingposture.config.Config;
import io.ganzuul.correctgamingposture.keybindings.KeybindHelper;

public final class CorrectGamingPosture {

  public static final String MOD_ID = "correct_gaming_posture";

  public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

  public static void init() {
    Config.load();
    KeybindHelper.load();

    if (Config.enableHeadTracking) {
      LOGGER.info("[Head Tracking] 👁️ Head tracking enabled via config");
      io.ganzuul.correctgamingposture.opentrack.OpentrackReceiver.start();
    } else {
      LOGGER.info("[Head Tracking] 👁️ Head tracking disabled (enable in config)");
    }
  }
}
