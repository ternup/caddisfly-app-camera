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

import com.ternup.caddisfly.R;
import com.ternup.caddisfly.app.Globals;
import com.ternup.caddisfly.model.ColorInfo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseIntArray;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Set of utility functions for color calculations and analysis
 */
public class ColorUtils {

    private static final int GRAY_TOLERANCE = 10;

    private static final double MAX_COLOR_DISTANCE = 50.0;

    private static final double LM_RED_COEFFICIENT = 0.2126;

    private static final double LM_GREEN_COEFFICIENT = 0.7152;

    private static final double LM_BLUE_COEFFICIENT = 0.0722;

    public static Bundle getPpmValue(byte[] data, ArrayList<ColorInfo> colorRange,
                                     double rangeStepUnit, int rangeStartUnit, int length) {
        ColorInfo photoColor = getColorFromByteArray(data, length);
        return analyzeColor(photoColor, colorRange, rangeStepUnit, rangeStartUnit);
    }
/*
    public static Bundle getPpmValue(String filePath, ArrayList<ColorInfo> colorRange,
                                     double rangeStepUnit, int rangeStartUnit, int length) {
        ColorInfo photoColor = getColorFromImage(filePath, length);
        return analyzeColor(photoColor, colorRange, rangeStepUnit, rangeStartUnit);
    }


    private static byte[] resizeImage(byte[] input, int length) {
        Bitmap original = BitmapFactory.decodeByteArray(input, 0, input.length);
        Bitmap resized = Bitmap.createScaledBitmap(original, length, length, true);

        ByteArrayOutputStream blob = new ByteArrayOutputStream();
        resized.compress(Bitmap.CompressFormat.JPEG, 100, blob);

        return blob.toByteArray();
    }
*/

    static ColorInfo getColorFromByteArray(byte[] data, int length) {
        //byte[] imageData = resizeImage(data, length);
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        return getColorFromBitmap(bitmap, length);
    }

    static ColorInfo getColorFromBitmap(Bitmap bitmap, int sampleLength) {
        int highestCount = 0;
        int goodPixelCount = 0;

        int commonColor = -1;
        int totalPixels = 0;
        int counter;
        double quality = 0;
        int colorsFound = 0;

        try {

            SparseIntArray m = new SparseIntArray();

            for (int i = 0; i < Math.min(bitmap.getWidth(), sampleLength); i++) {

                for (int j = 0; j < Math.min(bitmap.getHeight(), sampleLength); j++) {

                    int rgb = bitmap.getPixel(i, j);
                    int[] rgbArr = ColorUtils.getRGB(rgb);

                    if (ColorUtils.isNotGray(rgbArr)) {
                        totalPixels++;

                        counter = m.get(rgb);
                        counter++;
                        m.put(rgb, counter);

                        if (counter > highestCount) {
                            commonColor = rgb;
                            highestCount = counter;
                        }
                    }
                }
            }

            bitmap.recycle();

            colorsFound = m.size();
            int goodColors = 0;

            for (int i = 0; i < colorsFound; i++) {
                double distance = getDistance(commonColor, m.keyAt(i));

                if (distance < 10) {
                    goodColors++;
                    goodPixelCount += m.valueAt(i);
                }
            }

            m.clear();
            double quality1 = ((double) goodPixelCount / totalPixels) * 100d;
            double quality2 = ((double) (colorsFound - goodColors) / colorsFound) * 100d;
            quality = Math.min(quality1, (100 - quality2));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ColorInfo(commonColor, colorsFound, goodPixelCount, (int) quality);

    }

    /**
     * Analyzes an image and attempts to get the dominant color
     *
     * @param filename The path to image file that is to be analyzed
     * @return The dominant color
     */
/*
    static ColorInfo getColorFromImage(String filename, int length) {

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, options);

        int height = options.outHeight;
        int width = options.outWidth;
        int leftMargin = (width - length) / 2;
        int topMargin = (height - length) / 2;
        if (leftMargin < 0) {
            leftMargin = 0;
        }
        if (topMargin < 0) {
            topMargin = 0;
        }

        BitmapRegionDecoder decoder = null;
        try {
            decoder = BitmapRegionDecoder.newInstance(filename, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        BitmapFactory.Options options1 = new BitmapFactory.Options();
        //options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options1.inPreferQualityOverSpeed = true;
        options1.inPurgeable = true;
        Rect re = new Rect(leftMargin, topMargin, leftMargin + length,
                topMargin + length);
        Bitmap bitmap = decoder.decodeRegion(re, options1);

        return getColorFromBitmap(bitmap, length);
    }
*/

    /**
     * Analyzes the color and returns a bundle with various result values
     *
     * @param photoColor The color to compare
     * @param colorRange The range of colors to compare against
     * @return A bundle with the results
     */
    static Bundle analyzeColor(ColorInfo photoColor, ArrayList<ColorInfo> colorRange,
                               double rangeStepUnit, int rangeStartUnit) {

        Bundle bundle = new Bundle();
        bundle.putInt(Globals.RESULT_COLOR_KEY, photoColor.getColor()); //NON-NLS

        double value = getNearestColorFromSwatchRange(photoColor.getColor(), colorRange,
                rangeStepUnit);

        if (value < 0) {
            bundle.putDouble(Globals.RESULT_VALUE_KEY, -1); //NON-NLS

        } else {
            value = value + rangeStartUnit;
            bundle.putDouble(Globals.RESULT_VALUE_KEY,
                    (double) Math.round(value * 100) / 100); //NON-NLS
            int color = colorRange.get((int) Math.round(value / rangeStepUnit)).getColor();

            bundle.putInt("standardColor", color); //NON-NLS

            bundle.putString("standardColorRgb",
                    String.format("%d  %d  %d", Color.red(color),
                            Color.green(color),
                            Color.blue(color))
            );
        }

        bundle.putString("color",
                String.format("%d  %d  %d", Color.red(photoColor.getColor()),
                        Color.green(photoColor.getColor()),
                        Color.blue(photoColor.getColor()))
        );

        bundle.putInt(Globals.QUALITY_KEY, photoColor.getQuality());

        return bundle;
    }

    private static double getDistance(int color, int tempColor) {
        double red, green, blue;

        red = Math.pow(Color.red(tempColor) - Color.red(color), 2.0);
        green = Math.pow(Color.green(tempColor) - Color.green(color), 2.0);
        blue = Math.pow(Color.blue(tempColor) - Color.blue(color), 2.0);

        return Math.sqrt(blue + green + red);
    }

    /**
     * Compares the color to all colors in the color range and finds the nearest matching color
     *
     * @param color      The color to compare
     * @param colorRange The range of colors from which to return the nearest color
     * @return A parts per million (ppm) value (color index multiplied by a step unit)
     */
    private static double getNearestColorFromSwatchRange(int color, ArrayList<ColorInfo> colorRange,
                                                         double rangeStepUnit) {
        double distance = MAX_COLOR_DISTANCE;
        double nearest = -1;

        double red, green, blue;
        for (int i = 0; i < colorRange.size(); i++) {
            int tempColor = colorRange.get(i).getColor();

            // compute the Euclidean distance between the two colors
            red = Math.pow(Color.red(tempColor) - Color.red(color), 2.0);
            green = Math.pow(Color.green(tempColor) - Color.green(color), 2.0);
            blue = Math.pow(Color.blue(tempColor) - Color.blue(color), 2.0);

            double temp = Math.sqrt(blue + green + red);

            if (temp == 0.0) {
                nearest = i * rangeStepUnit;
                break;
            } else if (temp < distance) {
                distance = temp;
                nearest = i * rangeStepUnit;
            }
        }

        return nearest;
    }

    public static int getGradientColor(int startColor, int endColor, int incrementStep, int i) {
        int r = interpolate(Color.red(startColor), Color.red(endColor), incrementStep, i),
                g = interpolate(Color.green(startColor), Color.green(endColor), incrementStep, i),
                b = interpolate(Color.blue(startColor), Color.blue(endColor), incrementStep, i);

        return Color.rgb(r, g, b);
    }

    public static int interpolate(int start, int end, int steps, int count) {
        float result = (float) start
                + ((((float) end - (float) start) / steps) * count);
        return (int) result;
    }

    public static String getColorRgbString(int color) {
        return String.format("%d  %d  %d",
                Color.red(color), Color.green(color), Color.blue(color));
    }

    public static int[] getRGB(int pixel) {

        int red = (pixel >> 16) & 0xff;
        int green = (pixel >> 8) & 0xff;
        int blue = (pixel) & 0xff;

        return new int[]{red, green, blue};
    }

    public static boolean isNotGray(int[] rgb) {
        return Math.abs(rgb[0] - rgb[1]) > GRAY_TOLERANCE
                || Math.abs(rgb[0] - rgb[2]) > GRAY_TOLERANCE;
    }

    //Reference: https://gist.github.com/alexfu/64dc37b3343b9dead0c4
/*
    public static int calculateRelativeLuminance(int color) {
        int red = (int) (Color.red(color) * LM_RED_COEFFICIENT);
        int green = (int) (Color.green(color) * LM_GREEN_COEFFICIENT);
        int blue = (int) (Color.blue(color) * LM_BLUE_COEFFICIENT);
        return red + green + blue;
    }
*/

    public static double mostFrequent(double[] ary) {
        Map<Double, Integer> m = new HashMap<Double, Integer>();

        for (double a : ary) {
            if (a >= 0) {
                Integer freq = m.get(a);
                m.put(a, (freq == null) ? 1 : freq + 1);
            }
        }

        int max = -1;
        double mostFrequent = -1;

        for (Map.Entry<Double, Integer> e : m.entrySet()) {
            if (e.getValue() > max) {
                mostFrequent = e.getKey();
                max = e.getValue();
            }
        }

        return mostFrequent;
    }

    public static void validateGradient(ArrayList<ColorInfo> colorList, int size, int increment, int minQuality) {

        int index1, index2;
        double previousDistance = 0;
        int previousIndex = 0;
        boolean errorFound = false;
        boolean notCalibrated = false;

        for (int i = 0; i < size - 1; i++) {
            int index = i * increment;
            if (colorList.get(index).getErrorCode() == Globals.ERROR_NOT_YET_CALIBRATED) {
                notCalibrated = true;
                break;
            }
        }

        if (!notCalibrated) {

            for (int i = 0; i < size - 1; i++) {
                index1 = i * increment;
                int color1 = colorList.get(index1).getColor();
                index2 = (i + 1) * increment;
                int color2 = colorList.get(index2).getColor();
                double distance = getDistance(color1, color2);
                //Log.i("ColorInfo", String.valueOf(distance));
                //Invalid if color is too distant from previous color in list
                if (distance > 60) {
                    //Only one color needs to be set as invalid
                    if (colorList.get(index1).getErrorCode() == 0) {
                        errorFound = true;

                        if (i < size - 2) {
                            int index3 = (i + 2) * increment;
                            int color3 = colorList.get(index3).getColor();
                            distance = getDistance(color2, color3);
                            if (distance < 61) {
                                colorList.get(index1).setErrorCode(Globals.ERROR_OUT_OF_RANGE);
                            } else {
                                colorList.get(index2).setErrorCode(Globals.ERROR_OUT_OF_RANGE);
                            }
                        } else {
                            colorList.get(index2).setErrorCode(Globals.ERROR_OUT_OF_RANGE);
                        }
                    }
                }
            }

            if (!errorFound) {
                for (int i = 0; i < size; i++) {
                    index1 = i * increment;
                    int color1 = colorList.get(index1).getColor();
                    for (int j = 0; j < size; j++) {
                        index2 = j * increment;
                        int color2 = colorList.get(index2).getColor();
                        if (color1 != color2) {
                            double distance = getDistance(color1, color2);

                            //Invalid if color gradient is in reverse
                            if (i == 0 && previousDistance > distance) {
                                colorList.get(previousIndex).setErrorCode(Globals.ERROR_SWATCH_OUT_OF_PLACE);
                            }

                            previousIndex = index2;
                            previousDistance = distance;
                            //Log.i("ColorInfo", distance + "  =   "  + getColorRgbString(color1) + "  -  " + getColorRgbString(color2));

                            //Invalid if the color is too close to any other color in the list
                            if (distance < 10) {
                                colorList.get(index1).setErrorCode(Globals.ERROR_DUPLICATE_SWATCH);
                            }
                        }
                    }
                }
            }

            for (int i = 0; i < size - 1; i++) {
                int index = i * increment;
                if (colorList.get(index).getQuality() < minQuality) {
                    colorList.get(index).setErrorCode(Globals.ERROR_LOW_QUALITY);
                }
            }
        }
    }
}
