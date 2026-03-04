package io.ganzuul.correctgamingposture.keybindings;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyMapping.Category;
import net.minecraft.resources.Identifier;

public class KeyBindings {
  public static Category CATEGORY = KeyMapping.Category.register(Identifier.parse("correct_gaming_posture.main"));

  public static final KeyMapping HEADTRACKING_TOGGLE_KEY = new KeyMapping(
      "key.correct_gaming_posture.headtracking_toggle",
      InputConstants.KEY_H,
      CATEGORY);

  public static void init() {
    KeyMappingRegistry.register(HEADTRACKING_TOGGLE_KEY);
  }
}
