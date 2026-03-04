package io.ganzuul.correctgamingposture.util;

public class OneEuroFilter {
  private static final float MIN_DT_SECONDS = 1.0f / 240.0f;

  private final LowPassFilter valueFilter = new LowPassFilter();
  private final LowPassFilter derivativeFilter = new LowPassFilter();

  private boolean initialized = false;
  private float previousValue = 0.0f;

  public float filter(float value, float dtSeconds, float minCutoff, float beta, float derivativeCutoff) {
    float safeDt = Math.max(MIN_DT_SECONDS, dtSeconds);

    if (!initialized) {
      initialized = true;
      previousValue = value;
      valueFilter.reset(value);
      derivativeFilter.reset(0.0f);
      return value;
    }

    float derivative = (value - previousValue) / safeDt;
    previousValue = value;

    float derivativeAlpha = smoothingAlpha(safeDt, Math.max(0.01f, derivativeCutoff));
    float smoothedDerivative = derivativeFilter.filter(derivative, derivativeAlpha);

    float dynamicCutoff = Math.max(0.01f, minCutoff) + Math.max(0.0f, beta) * Math.abs(smoothedDerivative);
    float valueAlpha = smoothingAlpha(safeDt, dynamicCutoff);
    return valueFilter.filter(value, valueAlpha);
  }

  public void reset() {
    initialized = false;
    previousValue = 0.0f;
    valueFilter.reset(0.0f);
    derivativeFilter.reset(0.0f);
  }

  private static float smoothingAlpha(float dtSeconds, float cutoffHz) {
    float tau = 1.0f / ((float) (2.0 * Math.PI) * cutoffHz);
    return 1.0f / (1.0f + tau / dtSeconds);
  }

  private static final class LowPassFilter {
    private float value = 0.0f;
    private boolean initialized = false;

    public float filter(float input, float alpha) {
      if (!initialized) {
        initialized = true;
        value = input;
        return input;
      }
      value += Math.max(0.0f, Math.min(1.0f, alpha)) * (input - value);
      return value;
    }

    public void reset(float initialValue) {
      value = initialValue;
      initialized = false;
    }
  }
}
