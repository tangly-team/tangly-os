package net.tangly.commons.utilities;

import net.tangly.commons.lang.Interval;

import java.util.Arrays;

public class MathUtilities {
    public static final double Z_VALUE_PERCENTILE_99 = 2.576;
    public static final double Z_VALUE_PERCENTILE_95 = 1.96;
    public static final double Z_VALUE_PERCENTILE_90 = 1.647;
    public static final double Z_VALUE_PERCENTILE_85 = 1.44;
    public static final double Z_VALUE_PERCENTILE_80 = 1.28;
    public static final double Z_VALUE_PERCENTILE_75 = 1.04;

    public static double average(int[] values) {
        return Arrays.stream(values).average().getAsDouble();
    }

    public static double mean(int[] values) {
        Arrays.sort(values);
        double median;
        if (values.length % 2 == 0) {
            median = ((double) (values[values.length / 2] + values[values.length / 2 - 1])) / 2;
        } else {
            median = (double) values[values.length / 2];
        }
        return median;
    }

    public static Interval<Integer> confidence(int[] values, double confidence) throws ArrayIndexOutOfBoundsException {
        int lower = (int) Math.round(((double) (values.length) / 2) - (confidence * Math.pow(values.length * 0.25, 0.5)));
        int upper = (int) Math.round(((double) (values.length) / 2) + (confidence * Math.pow(values.length * 0.25, 0.5)));
        return new Interval(values[lower], values[upper]);
    }
}
