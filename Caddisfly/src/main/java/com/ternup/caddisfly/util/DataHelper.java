/*
 * Copyright (C) TernUp Research Labs
 *
 * This file is part of Caddisfly
 *
 * Caddisfly is free software: you can redistribute it and modify it under the terms of
 * the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 * either version 3 of the License or any later version.
 *
 * Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License included below for more details.
 *
 * The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package com.ternup.caddisfly.util;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import com.ternup.caddisfly.R;
import com.ternup.caddisfly.app.Globals;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class DataHelper {

    public static String getTestTitle(Context context, int testType) {
        switch (testType) {
            case Globals.FLUORIDE_INDEX:
                return context.getString(R.string.fluoride);
            case Globals.FLUORIDE_2_INDEX:
                return context.getString(R.string.fluoride);
            case Globals.PH_INDEX:
                return context.getString(R.string.pH);
            case Globals.BACTERIA_INDEX:
                return context.getString(R.string.bacteria);
            default:
                return context.getString(R.string.fluoride);
        }
    }

    public static int getTestTypeFromCode(String code) {

        if (("FLUOR").equals(code)) {
            return Globals.FLUORIDE_INDEX;
        } else if (("ALKAL").equals(code)) {
            return Globals.FLUORIDE_INDEX;
        } else if (("COLIF").equals(code)) {
            return Globals.BACTERIA_INDEX;
        } else if (("TURBI").equals(code)) {
            return Globals.FLUORIDE_INDEX;
        } else if (("NITRA").equals(code)) {
            return Globals.FLUORIDE_INDEX;
        } else if (("IRONA").equals(code)) {
            return Globals.FLUORIDE_INDEX;
        } else if (("ARSEN").equals(code)) {
            return Globals.FLUORIDE_INDEX;
        }

        return -1;
    }

    public static String getSwatchError(Context context, int errorCode) {
        switch (errorCode) {
            case Globals.ERROR_NOT_YET_CALIBRATED:
                return context.getString(R.string.notCalibrated);
            case Globals.ERROR_DUPLICATE_SWATCH:
                return context.getString(R.string.duplicateSwatch);
            case Globals.ERROR_SWATCH_OUT_OF_PLACE:
                return context.getString(R.string.outOfSequence);
            case Globals.ERROR_OUT_OF_RANGE:
                return context.getString(R.string.outOfRange);
            case Globals.ERROR_COLOR_IS_GRAY:
                return context.getString(R.string.outOfRange);
            case Globals.ERROR_LOW_QUALITY:
                return context.getString(R.string.photoQualityError);
            default:
                return context.getString(R.string.error);
        }
    }

    public static void saveResultToPreferences(Context context, int testType, long id) {
        int samplingCount = PreferencesUtils
                .getInt(context, R.string.samplingCountKey, Globals.SAMPLING_COUNT_DEFAULT);
        for (int i = 0; i < samplingCount; i++) {
            String key = String.format(context.getString(R.string.samplingIndexKey), i);
            double tempResult = PreferencesUtils.getDouble(context, key);
            PreferencesUtils.setDouble(context,
                    String.format(context.getString(R.string.resultValueKey), testType, id, i),
                    tempResult);

            key = String.format(context.getString(R.string.samplingColorIndexKey), i);
            int tempColor = PreferencesUtils.getInt(context, key, -1);
            PreferencesUtils.setInt(context,
                    String.format(context.getString(R.string.resultColorKey), testType, id, i),
                    tempColor);

            key = String.format(context.getString(R.string.samplingQualityIndexKey), i);
            int tempQuality = PreferencesUtils.getInt(context, key, -1);
            PreferencesUtils.setInt(context,
                    String.format(context.getString(R.string.resultQualityKey), testType, id, i),
                    tempQuality);
        }

    }

    public static void saveResult(Context context, int testType, int id, int position, double resultValue, int resultColor, int quality) {

        PreferencesUtils.setDouble(context,
                String.format(context.getString(R.string.resultValueKey), testType, id, position),
                resultValue);

        PreferencesUtils.setInt(context,
                String.format(context.getString(R.string.resultColorKey), testType, id, position),
                resultColor);

        PreferencesUtils.setInt(context,
                String.format(context.getString(R.string.resultQualityKey), testType, id, position),
                quality);
    }

    public static double getAverageResult(Context context, Bundle bundle) {

        double result = 0;

        int samplingCount = PreferencesUtils
                .getInt(context, R.string.samplingCountKey, Globals.SAMPLING_COUNT_DEFAULT);
        int counter = 0;
        double commonResult = 0;
        double[] results = new double[samplingCount];
        int[] colors = new int[samplingCount];
        for (int i = 0; i < samplingCount; i++) {
            String key = String.format(context.getString(R.string.samplingIndexKey), i);
            results[i] = PreferencesUtils.getDouble(context, key);
            key = String.format(context.getString(R.string.samplingColorIndexKey), i);
            colors[i] = PreferencesUtils.getInt(context, key, -1);
            commonResult = ColorUtils.mostFrequent(results);
        }

        int red = 0;
        int green = 0;
        int blue = 0;
        for (int i = 0; i < results.length; i++) {
            if (results[i] >= 0 && colors[i] != -1) {
                if (Math.abs(results[i] - commonResult) < 0.5) {
                    counter++;
                    result += results[i];
                    red += Color.red(colors[i]);
                    green += Color.green(colors[i]);
                    blue += Color.blue(colors[i]);
                }
            }
        }

        if (counter > 0) {
            result = round(result / counter, 2);
            bundle.putDouble(Globals.RESULT_VALUE_KEY, result);
            bundle.putInt(Globals.RESULT_COLOR_KEY,
                    Color.rgb(red / counter, green / counter, blue / counter));
        } else {
            result = -1;
        }

        return result;
    }

    //Ref: http://stackoverflow.com/questions/2808535/round-a-double-to-2-decimal-places
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static void saveTempResult(Context context, double resultValue, int resultColor, int quality) {

        int samplingCount = PreferencesUtils.getInt(context, R.string.currentSamplingCountKey, 0);
        String key = String.format(context.getString(R.string.samplingIndexKey), samplingCount);
        PreferencesUtils.setDouble(context, key, resultValue);
        key = String.format(context.getString(R.string.samplingColorIndexKey), samplingCount);
        PreferencesUtils.setInt(context, key, resultColor);
        key = String.format(context.getString(R.string.samplingQualityIndexKey), samplingCount);
        PreferencesUtils.setInt(context, key, quality);
    }


}
