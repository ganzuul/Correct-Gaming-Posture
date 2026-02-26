package wtf.blexyel.simpleCameraTweaks;

import dev.architectury.event.events.client.ClientTickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wtf.blexyel.simpleCameraTweaks.config.Config;
import wtf.blexyel.simpleCameraTweaks.keybindings.KeybindHelper;
import wtf.blexyel.simpleCameraTweaks.util.FreelookUtils;

public final class SimpleCameraTweaks {

  public static final String MOD_ID = "correct_gaming_posture";

  public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

  public static void init() {
    Config.load();
    KeybindHelper.load();

    // Initialize head tracking if enabled
    if (Config.enableHeadTracking) {
      LOGGER.info("[Head Tracking] 👁️ Head tracking enabled via config");
      wtf.blexyel.simpleCameraTweaks.opentrack.OpentrackReceiver.start();
    } else {
      LOGGER.info("[Head Tracking] 👁️ Head tracking disabled (enable in config)");
    }

    ClientTickEvent.CLIENT_POST.register(
        (client) -> {
          FreelookUtils.tick();
        });
  }
}
