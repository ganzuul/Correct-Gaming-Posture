package io.ganzuul.correctgamingposture.fabric;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.ganzuul.correctgamingposture.config.YACLConfig;

public class ModMenu implements ModMenuApi {
  @Override
  public ConfigScreenFactory<?> getModConfigScreenFactory() {
    return YACLConfig::create;
  }
}