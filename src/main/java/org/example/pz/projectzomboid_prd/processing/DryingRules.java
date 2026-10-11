package org.example.pz.projectzomboid_prd.processing;

/**
 * How fast a harvest dries on a drying rack, from the temperature and humidity where the rack stands (the same
 * values the thermo-hygrometer shows). Dry, warm air dries fastest; muggy air barely dries and, if it lasts,
 * the harvest goes moldy.
 */
public final class DryingRules {
    /** One Minecraft day. */
    public static final int DAY = 24000;
    /** From this relative humidity (%) on the harvest stays damp. */
    public static final double DAMP_HUMIDITY = 85;
    /** Damp ticks in a row before the harvest is spoiled. */
    public static final int MOLD_TICKS = DAY;
    /** Humidity at which drying stops completely. */
    private static final double NO_DRYING_HUMIDITY = 90;

    private DryingRules() {
    }

    /** Drying speed relative to normal (1.0 = the recipe's days). 0 when it rains on the rack. */
    public static float rate(double temperature, double humidity, boolean rain) {
        if (rain) {
            return 0;
        }
        double air = Math.clamp((NO_DRYING_HUMIDITY - humidity) / 40.0, 0.0, 1.5);
        double warmth;
        if (temperature < 5) {
            warmth = 0.4;
        } else if (temperature < 15) {
            warmth = 0.7;
        } else if (temperature <= 30) {
            warmth = 1.0;
        } else {
            warmth = 1.3;
        }
        return (float) (air * warmth);
    }

    public static boolean damp(double humidity, boolean rain) {
        return rain || humidity >= DAMP_HUMIDITY;
    }

    /** State of a batch on a rack; {@code progress} goes from 0 to 1. */
    public record Batch(float progress, int dampTicks) {
        public static final Batch FRESH = new Batch(0, 0);

        public boolean done() {
            return progress >= 1;
        }

        public boolean spoiled() {
            return dampTicks >= MOLD_TICKS;
        }

        /** Advances the batch by {@code ticks} under the given conditions; {@code days} is the recipe's drying time. */
        public Batch advance(float days, double temperature, double humidity, boolean rain, int ticks) {
            int damp = damp(humidity, rain) ? dampTicks + ticks : Math.max(0, dampTicks - ticks);
            float dried = rate(temperature, humidity, rain) * ticks / (days * DAY);
            return new Batch(Math.min(1, progress + dried), damp);
        }
    }
}
