package io.ganzuul.correctgamingposture.keybindings;

import dev.architectury.event.events.client.ClientTickEvent;
import io.ganzuul.correctgamingposture.CorrectGamingPosture;
import io.ganzuul.correctgamingposture.config.Config;

public class KeybindHelper {
  static boolean keybindingsLoaded = false;

  public static void load() {
    try {
      // Try new Keybindings
      Class<?> keybindingsClass = Class.forName("io.ganzuul.correctgamingposture.keybindings.KeyBindings");
      keybindingsClass.getMethod("init").invoke(null);

      ClientTickEvent.CLIENT_POST.register(
          mc -> {
            if (Config.enableHeadTrackingToggleShortcut && KeyBindings.HEADTRACKING_TOGGLE_KEY.consumeClick()) {
              toggleHeadTracking();
            }
          });

      keybindingsLoaded = true; // success

    } catch (Throwable t) {
      // only log if we actually need the fallback
      CorrectGamingPosture.LOGGER.info("New Keybindings failed to load, trying fallback...");
    }

    // only attempt fallback if first attempt failed
    if (!keybindingsLoaded) {
      try {
        Class<?> compatClass = Class.forName("io.ganzuul.correctgamingposture.keybindings.KeyBindingsCompat");
        compatClass.getMethod("init").invoke(null);

        // Object enabledKeyCompat = compatClass.getField("ENABLED_KEY").get(null);
        // var consumeClickCompat = enabledKeyCompat.getClass().getMethod("consumeClick");

        ClientTickEvent.CLIENT_POST.register(
            mc -> {
              if (Config.enableHeadTrackingToggleShortcut
                  && KeyBindingsCompat.HEADTRACKING_TOGGLE_KEY.consumeClick()) {
                toggleHeadTracking();
              }
            });

        CorrectGamingPosture.LOGGER.info("Fallback KeybindingsCompat loaded successfully.");

      } catch (Throwable fallbackError) {
        CorrectGamingPosture.LOGGER.error("No compatible keybindings class could be loaded!", fallbackError);
      }
    }
  }

  private static void toggleHeadTracking() {
    Config.enableHeadTracking = !Config.enableHeadTracking;
    if (Config.enableHeadTracking) {
      CorrectGamingPosture.LOGGER.info("[Head Tracking] 👁️ Enabled via shortcut");
      io.ganzuul.correctgamingposture.opentrack.OpentrackReceiver.start();
    } else {
      CorrectGamingPosture.LOGGER.info("[Head Tracking] 👁️ Disabled via shortcut");
      io.ganzuul.correctgamingposture.opentrack.OpentrackReceiver.stop();
    }
    Config.save();
  }
}
