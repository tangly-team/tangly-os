/*
 * Copyright 2006-2020 Marcel Baumann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 *  a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 *  under the License.
 */

package net.tangly.commons.utilities;

import java.util.Arrays;

import net.tangly.commons.lang.Interval;

public final class MathUtilities {
    public static final double Z_VALUE_PERCENTILE_99 = 2.576;
    public static final double Z_VALUE_PERCENTILE_95 = 1.96;
    public static final double Z_VALUE_PERCENTILE_90 = 1.647;
    public static final double Z_VALUE_PERCENTILE_85 = 1.44;
    public static final double Z_VALUE_PERCENTILE_80 = 1.28;
    public static final double Z_VALUE_PERCENTILE_75 = 1.04;

    /**
     * Private constructor for a utility class.
     */
    private MathUtilities() {
    }


    public static double average(int... values) {
        return Arrays.stream(values).average().getAsDouble();
    }

    public static double mean(int... values) {
        Arrays.sort(values);
        double median;
        if (values.length % 2 == 0) {
            median = ((double) (values[values.length / 2] + values[values.length / 2 - 1])) / 2;
        } else {
            median = values[values.length / 2];
        }
        return median;
    }

    public static Interval<Integer> confidence(int[] values, double confidence) {
        double correction = (confidence * Math.pow(values.length * 0.25, 0.5));
        int lower = (int) Math.round(((double) (values.length) / 2) - correction);
        int upper = (int) Math.round(((double) (values.length) / 2) + correction);
        return new Interval<>(values[lower], values[upper]);
    }
}
