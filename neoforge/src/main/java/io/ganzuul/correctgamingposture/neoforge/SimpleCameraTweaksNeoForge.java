package io.ganzuul.correctgamingposture.neoforge;

import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import io.ganzuul.correctgamingposture.CorrectGamingPosture;
import net.neoforged.fml.common.Mod;
import io.ganzuul.correctgamingposture.config.YACLConfig;

@Mod(CorrectGamingPosture.MOD_ID)
public final class SimpleCameraTweaksNeoForge {

  public SimpleCameraTweaksNeoForge() {
    ModLoadingContext.get()
        .registerExtensionPoint(
            IConfigScreenFactory.class, () -> (client, parent) -> YACLConfig.create(parent));
    // Run our common setup.
    CorrectGamingPosture.init();
  }
}
