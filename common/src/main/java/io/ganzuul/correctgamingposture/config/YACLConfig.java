package io.ganzuul.correctgamingposture.config;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import io.ganzuul.correctgamingposture.CorrectGamingPosture;
import io.ganzuul.correctgamingposture.opentrack.OpentrackReceiver;

public class YACLConfig {
  public static Screen create(Screen parent) {
    return YetAnotherConfigLib.createBuilder()
        .title(Component.literal("Correct Gaming Posture Config"))
        .category(
            ConfigCategory.createBuilder()
                .name(Component.literal("Keyboard Shortcuts"))
                .option(
                    Option.<Boolean>createBuilder()
                        .name(Component.literal("Enable Headtracking Toggle Shortcut"))
                        .description(
                            OptionDescription.of(
                                Component.literal(
                                    "When enabled, the Toggle Headtracking keybind can turn tracking on/off globally.")))
                        .binding(
                            Config.enableHeadTrackingToggleShortcut,
                            () -> Config.enableHeadTrackingToggleShortcut,
                            newVal -> Config.enableHeadTrackingToggleShortcut = newVal)
                        .controller(TickBoxControllerBuilder::create)
                        .build())
                .build())
        .category(
            ConfigCategory.createBuilder()
                .name(Component.literal("Head Tracking"))
                .option(
                    Option.<Boolean>createBuilder()
                        .name(Component.literal("Tracker Connected"))
                        .description(
                            OptionDescription.of(
                                Component.literal("Live status of OpenTrack UDP data reception.")))
                        .binding(
                            OpentrackReceiver.isConnected(),
                            OpentrackReceiver::isConnected,
                            newVal -> {})
                        .controller(TickBoxControllerBuilder::create)
                        .available(false)
                        .build())
                .option(
                    Option.<Boolean>createBuilder()
                        .name(Component.literal("Enable Head Tracking"))
                        .description(
                            OptionDescription.of(
                                Component.literal("Track head position via OpenTrack UDP listener (port 4242)")))
                        .binding(
                            Config.enableHeadTracking,
                            () -> Config.enableHeadTracking,
                            newVal -> {
                              Config.enableHeadTracking = newVal;
                              if (newVal) {
                                CorrectGamingPosture.LOGGER.info("[Head Tracking] 👁️ Enabled!");
                                OpentrackReceiver.start();
                              } else {
                                CorrectGamingPosture.LOGGER.info("[Head Tracking] 👁️ Disabled");
                                OpentrackReceiver.stop();
                              }
                            })
                        .controller(TickBoxControllerBuilder::create)
                        .build())
                .option(
                    Option.<Float>createBuilder()
                        .name(Component.literal("Display Distance (m)"))
                        .description(
                            OptionDescription.of(
                                Component.literal(
                                    "Distance from viewer to display screen in meters. Used for head-coupled perspective parallax calculation.")))
                        .binding(
                            (float) Config.displayDistance,
                            () -> (float) Config.displayDistance,
                            newVal -> Config.displayDistance = (double) newVal)
                        .controller(
                            opt ->
                                FloatSliderControllerBuilder.create(opt)
                                    .range(0.01f, 2.0f)
                                    .step(0.01f))
                        .build())
                .build())
        .category(
            ConfigCategory.createBuilder()
                .name(Component.literal("Smoothing"))
                .option(
                    Option.<Boolean>createBuilder()
                        .name(Component.literal("Enable 1€ Filter"))
                        .description(
                            OptionDescription.of(
                                Component.literal(
                                    "Uses the One Euro filter for low-latency smoothing. Disable for raw tracking data and minimum compute.")))
                        .binding(
                            Config.isOneEuroSmoothing(),
                            Config::isOneEuroSmoothing,
                            newVal ->
                                Config.headTrackingSmoothingMode =
                                    newVal
                                        ? Config.SMOOTHING_MODE_ONE_EURO
                                        : Config.SMOOTHING_MODE_OFF)
                        .controller(TickBoxControllerBuilder::create)
                        .build())
                .option(
                    Option.<Float>createBuilder()
                        .name(Component.literal("1€ Min Cutoff"))
                        .description(
                            OptionDescription.of(
                                Component.literal(
                                    "Base smoothing frequency. Higher values respond faster and smooth less.")))
                        .binding(
                            Config.oneEuroMinCutoff,
                            () -> Config.oneEuroMinCutoff,
                            newVal -> Config.oneEuroMinCutoff = newVal)
                        .controller(
                            opt ->
                                FloatSliderControllerBuilder.create(opt)
                                    .range(0.01f, 8.0f)
                                    .step(0.01f))
                        .build())
                .option(
                    Option.<Float>createBuilder()
                        .name(Component.literal("1€ Beta"))
                        .description(
                            OptionDescription.of(
                                Component.literal(
                                    "Velocity sensitivity. Higher values reduce lag during faster head movement.")))
                        .binding(
                            Config.oneEuroBeta,
                            () -> Config.oneEuroBeta,
                            newVal -> Config.oneEuroBeta = newVal)
                        .controller(
                            opt ->
                                FloatSliderControllerBuilder.create(opt)
                                    .range(0.0f, 1.0f)
                                    .step(0.005f))
                        .build())
                .option(
                    Option.<Float>createBuilder()
                        .name(Component.literal("1€ Derivative Cutoff"))
                        .description(
                            OptionDescription.of(
                                Component.literal(
                                    "Smoothing frequency for motion derivative estimation.")))
                        .binding(
                            Config.oneEuroDerivativeCutoff,
                            () -> Config.oneEuroDerivativeCutoff,
                            newVal -> Config.oneEuroDerivativeCutoff = newVal)
                        .controller(
                            opt ->
                                FloatSliderControllerBuilder.create(opt)
                                    .range(0.01f, 8.0f)
                                    .step(0.01f))
                        .build())
                .build())
        .category(
            ConfigCategory.createBuilder()
                .name(Component.literal("Translation"))
                .option(
                    Option.<Float>createBuilder()
                        .name(Component.literal("Horizontal Gain"))
                        .description(
                            OptionDescription.of(
                                Component.literal(
                                    "Scales left/right head translation contribution.")))
                        .binding(
                            Config.headTrackingGainX,
                            () -> Config.headTrackingGainX,
                            newVal -> Config.headTrackingGainX = newVal)
                        .controller(
                            opt ->
                                FloatSliderControllerBuilder.create(opt)
                                    .range(0f, 3.0f)
                                    .step(0.05f))
                        .build())
                .option(
                    Option.<Float>createBuilder()
                        .name(Component.literal("Vertical Gain"))
                        .description(
                            OptionDescription.of(
                                Component.literal("Scales up/down head translation contribution.")))
                        .binding(
                            Config.headTrackingGainY,
                            () -> Config.headTrackingGainY,
                            newVal -> Config.headTrackingGainY = newVal)
                        .controller(
                            opt ->
                                FloatSliderControllerBuilder.create(opt)
                                    .range(0f, 3.0f)
                                    .step(0.05f))
                        .build())
                .option(
                    Option.<Float>createBuilder()
                        .name(Component.literal("Depth Gain"))
                        .description(
                            OptionDescription.of(
                                Component.literal(
                                    "Scales forward/back head translation contribution.")))
                        .binding(
                            Config.headTrackingGainZ,
                            () -> Config.headTrackingGainZ,
                            newVal -> Config.headTrackingGainZ = newVal)
                        .controller(
                            opt ->
                                FloatSliderControllerBuilder.create(opt)
                                    .range(0f, 3.0f)
                                    .step(0.05f))
                        .build())
                .option(
                    Option.<Float>createBuilder()
                        .name(Component.literal("Max Horizontal Offset"))
                        .description(
                            OptionDescription.of(
                                Component.literal(
                                    "Maximum left/right camera translation from head tracking in blocks.")))
                        .binding(
                            Config.headTrackingMaxHorizontal,
                            () -> Config.headTrackingMaxHorizontal,
                            newVal -> Config.headTrackingMaxHorizontal = newVal)
                        .controller(
                            opt ->
                                FloatSliderControllerBuilder.create(opt)
                                    .range(0.1f, 3.0f)
                                    .step(0.05f))
                        .build())
                .option(
                    Option.<Float>createBuilder()
                        .name(Component.literal("Max Vertical Offset"))
                        .description(
                            OptionDescription.of(
                                Component.literal(
                                    "Maximum up/down camera translation from head tracking in blocks.")))
                        .binding(
                            Config.headTrackingMaxVertical,
                            () -> Config.headTrackingMaxVertical,
                            newVal -> Config.headTrackingMaxVertical = newVal)
                        .controller(
                            opt ->
                                FloatSliderControllerBuilder.create(opt)
                                    .range(0.1f, 3.0f)
                                    .step(0.05f))
                        .build())
                .option(
                    Option.<Float>createBuilder()
                        .name(Component.literal("Max Depth Offset"))
                        .description(
                            OptionDescription.of(
                                Component.literal(
                                    "Maximum forward/back camera translation from head tracking in blocks.")))
                        .binding(
                            Config.headTrackingMaxDepth,
                            () -> Config.headTrackingMaxDepth,
                            newVal -> Config.headTrackingMaxDepth = newVal)
                        .controller(
                            opt ->
                                FloatSliderControllerBuilder.create(opt)
                                    .range(0.1f, 3.0f)
                                    .step(0.05f))
                        .build())
                .build())
        .category(
            ConfigCategory.createBuilder()
                .name(Component.literal("Targeting"))
                .option(
                    Option.<Boolean>createBuilder()
                        .name(Component.literal("Aligned Targeting"))
                        .description(
                            OptionDescription.of(
                                Component.literal(
                                    "Aligns block targeting with head-tracked camera translation.")))
                        .binding(
                            Config.isAlignedTargeting(),
                            Config::isAlignedTargeting,
                            newVal ->
                                Config.targetingMode =
                                    newVal
                                        ? Config.TARGETING_MODE_ALIGNED
                                        : Config.TARGETING_MODE_VANILLA)
                        .controller(TickBoxControllerBuilder::create)
                        .build())
                .option(
                    Option.<Boolean>createBuilder()
                        .name(Component.literal("Predictive Assist (Experimental)"))
                        .description(
                            OptionDescription.of(
                                Component.literal(
                                    "Predictively biases targeting during rapid camera movement. Off by default.")))
                        .binding(
                            Config.enablePredictiveTargetingAssist,
                            () -> Config.enablePredictiveTargetingAssist,
                            newVal -> Config.enablePredictiveTargetingAssist = newVal)
                        .controller(TickBoxControllerBuilder::create)
                        .build())
                .option(
                    Option.<Float>createBuilder()
                        .name(Component.literal("Predictive Assist Time (s)"))
                        .description(
                            OptionDescription.of(
                                Component.literal(
                                    "Prediction horizon in seconds for experimental assist.")))
                        .binding(
                            Config.predictiveTargetingAssistSeconds,
                            () -> Config.predictiveTargetingAssistSeconds,
                            newVal -> Config.predictiveTargetingAssistSeconds = newVal)
                        .controller(
                            opt ->
                                FloatSliderControllerBuilder.create(opt)
                                    .range(0.0f, 0.5f)
                                    .step(0.01f))
                        .build())
                .build())
        .save(Config::save)
        .build()
        .generateScreen(parent);
  }
}
