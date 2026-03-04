package io.ganzuul.correctgamingposture.keybindings;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import java.lang.reflect.Constructor;
import net.minecraft.client.KeyMapping;

public class KeyBindingsCompat {
  public static KeyMapping HEADTRACKING_TOGGLE_KEY;
  public static String CATEGORY = "key.category.minecraft.correct_gaming_posture.main";

  static {
    try {
      Constructor<KeyMapping> constructor = KeyMapping.class.getConstructor(String.class,
          InputConstants.Type.class, int.class, String.class);

        HEADTRACKING_TOGGLE_KEY = constructor.newInstance("key.correct_gaming_posture.headtracking_toggle",
          InputConstants.Type.KEYSYM,
          InputConstants.KEY_H,
          CATEGORY);

    } catch (Exception e) {
      // nothin
    }
  }
  public static void init() {
    try {
      KeyMappingRegistry.register(HEADTRACKING_TOGGLE_KEY);
    } catch (Exception e) {
      // nothin
    }
  }
}
