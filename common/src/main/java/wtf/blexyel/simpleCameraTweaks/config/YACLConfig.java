package wtf.blexyel.simpleCameraTweaks.config;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class YACLConfig {
  public static Screen create(Screen parent) {
    return YetAnotherConfigLib.createBuilder()
        .title(Component.literal("Simple Camera Tweaks Config"))
        .category(
            ConfigCategory.createBuilder()
                .name(Component.literal("General"))
                .option(
                    Option.<Boolean>createBuilder()
                        .name(Component.literal("Always show offhand"))
                        .description(
                            OptionDescription.of(
                                Component.literal("Makes offhand visible at all times in first person")))
                        .binding(
                            Config.offhand, () -> Config.offhand, newVal -> Config.offhand = newVal)
                        .controller(TickBoxControllerBuilder::create)
                        .build())
                .option(
                    Option.<Boolean>createBuilder()
                        .name(Component.literal("Smooth zoom (Not implemented)"))
                        .description(
                            OptionDescription.of(
                                Component.literal("Smoothly zooms in and out (Not implemented, due to the lack of my math skills)")))
                        .binding(
                            Config.smooth, () -> Config.smooth, newVal -> Config.smooth = newVal)
                        .controller(TickBoxControllerBuilder::create)
                        .available(false)
                        .build())
                .build())
        .category(
            ConfigCategory.createBuilder()
                .name(Component.literal("Head Tracking"))
                .option(
                    Option.<Boolean>createBuilder()
                        .name(Component.literal("Enable Head Tracking"))
                        .description(
                            OptionDescription.of(
                                Component.literal("Track head position via OpenTrack UDP listener (port 4242)")))
                        .binding(
                            Config.enableHeadTracking, () -> Config.enableHeadTracking, newVal -> {
                              Config.enableHeadTracking = newVal;
                              if (newVal) {
                                wtf.blexyel.simpleCameraTweaks.SimpleCameraTweaks.LOGGER.info("[Head Tracking] 👁️ Enabled!");
                                wtf.blexyel.simpleCameraTweaks.opentrack.OpentrackReceiver.start();
                              } else {
                                wtf.blexyel.simpleCameraTweaks.SimpleCameraTweaks.LOGGER.info("[Head Tracking] 👁️ Disabled");
                                wtf.blexyel.simpleCameraTweaks.opentrack.OpentrackReceiver.stop();
                              }
                            })
                        .controller(TickBoxControllerBuilder::create)
                        .build())
                .option(
                    Option.<Float>createBuilder()
                        .name(Component.literal("Display Distance (m)"))
                        .description(
                            OptionDescription.of(
                                Component.literal("Distance from viewer to display screen in meters. Used for head-coupled perspective parallax calculation.")))
                        .binding(
                            (float) Config.displayDistance, () -> (float) Config.displayDistance, newVal -> Config.displayDistance = (double) newVal)
                        .controller(opt -> FloatSliderControllerBuilder.create(opt).range(0.1f, 2.0f).step(0.05f))
                        .build())
                .option(
                    Option.<Float>createBuilder()
                        .name(Component.literal("Head Tracking Smoothing"))
                        .description(
                            OptionDescription.of(
                                Component.literal("Smoothing factor for head tracking data (0 = no smoothing, 1 = maximum smoothing)")))
                        .binding(
                            Config.headTrackingSmoothing, () -> Config.headTrackingSmoothing, newVal -> Config.headTrackingSmoothing = newVal)
                        .controller(opt -> FloatSliderControllerBuilder.create(opt).range(0f, 1.0f).step(0.05f))
                        .build())
                .build())
        .save(Config::save)
        .build()
        .generateScreen(parent);
  }
}