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

import com.ternup.caddisfly.app.Globals;
import com.ternup.caddisfly.model.ColorInfo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.SparseIntArray;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Set of utility functions for color calculations and analysis
 */
public class ColorUtils {

    private static final int GRAY_TOLERANCE = 10;

    private static final double MAX_COLOR_DISTANCE = 50.0;

    public static Bundle getPpmValue(byte[] data, ArrayList<Integer> colorRange,
            double rangeStepUnit, int rangeStartUnit) {
        ColorInfo photoColor = getColorFromByteArray(data);
        return analyzeColor(photoColor, colorRange, rangeStepUnit, rangeStartUnit);
    }

    public static Bundle getPpmValue(String filePath, ArrayList<Integer> colorRange,
            double rangeStepUnit, int rangeStartUnit) {
        ColorInfo photoColor = getColorFromImage(filePath);
        return analyzeColor(photoColor, colorRange, rangeStepUnit, rangeStartUnit);
    }


    private static byte[] resizeImage(byte[] input) {
        Bitmap original = BitmapFactory.decodeByteArray(input, 0, input.length);
        Bitmap resized = Bitmap.createScaledBitmap(original, Globals.IMAGE_SAMPLE_LENGTH,
                Globals.IMAGE_SAMPLE_LENGTH, true);

        ByteArrayOutputStream blob = new ByteArrayOutputStream();
        resized.compress(Bitmap.CompressFormat.JPEG, 100, blob);

        return blob.toByteArray();
    }

    static ColorInfo getColorFromByteArray(byte[] data) {
        byte[] imageData = resizeImage(data);
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
        return getColorFromBitmap(bitmap);

    }

    static ColorInfo getColorFromBitmap(Bitmap bitmap) {
        int highestCount = 0;
        int highCount = 0;

        int commonColor = -1;
        int count = 0;
        int totalCounter = 0;

        try {

            SparseIntArray m = new SparseIntArray();

            int counter;
            for (int i = 0; i < Math.min(bitmap.getWidth(), Globals.IMAGE_SAMPLE_LENGTH); i++) {

                for (int j = 0; j < Math.min(bitmap.getHeight(), Globals.IMAGE_SAMPLE_LENGTH);
                        j++) {

                    totalCounter++;

                    int rgb = bitmap.getPixel(i, j);
                    int[] rgbArr = ColorUtils.getRGB(rgb);

                    if (ColorUtils.isNotGray(rgbArr)) {
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
            count = m.size();

            for (int i = 0; i < count; i++) {
                double distance = getDistance(commonColor, m.keyAt(i));

                if (distance < 10) {
                    highCount += m.valueAt(i);
                }
            }

            m.clear();

        } catch (Exception e) {
            e.printStackTrace();
        }

        double colorPercentage = ((double) highCount / totalCounter) * 100d;

        return new ColorInfo(commonColor, count, (int) colorPercentage);

    }

    /**
     * Analyzes an image and attempts to get the dominant color
     *
     * @param filename The path to image file that is to be analyzed
     * @return The dominant color
     */
    static ColorInfo getColorFromImage(String filename) {

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, options);

        int height = options.outHeight;
        int width = options.outWidth;
        int leftMargin = (width - Globals.IMAGE_SAMPLE_LENGTH) / 2;
        int topMargin = (height - Globals.IMAGE_SAMPLE_LENGTH) / 2;
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
        Rect re = new Rect(leftMargin, topMargin, leftMargin + Globals.IMAGE_SAMPLE_LENGTH,
                topMargin + Globals.IMAGE_SAMPLE_LENGTH);
        Bitmap bitmap = decoder.decodeRegion(re, options1);

        return getColorFromBitmap(bitmap);
    }

    /**
     * Analyzes the color and returns a bundle with various result values
     *
     * @param photoColor The color to compare
     * @param colorRange The range of colors to compare against
     * @return A bundle with the results
     */
    static Bundle analyzeColor(ColorInfo photoColor, ArrayList<Integer> colorRange,
            double rangeStepUnit, int rangeStartUnit) {

        Bundle bundle = new Bundle();
        bundle.putInt("resultColor", photoColor.getColor()); //NON-NLS

        double value = getNearestColorFromSwatchRange(photoColor.getColor(), colorRange,
                rangeStepUnit);

        if (value < 0) {
            bundle.putDouble("resultValue", -1); //NON-NLS

        } else {
            value = value + rangeStartUnit;
            bundle.putDouble("resultValue", (double) Math.round(value * 100) / 100); //NON-NLS
            int color = colorRange.get((int) Math.round(value / rangeStepUnit));

            bundle.putInt("standardColor", color); //NON-NLS

            bundle.putString("standardColorRgb",
                    String.format("%s  %s  %s", Integer.toString(Color.red(color)),
                            Integer.toString(Color.green(color)),
                            Integer.toString(Color.blue(color)))
            );
        }

        bundle.putString("color",
                String.format("%s  %s  %s", Integer.toString(Color.red(photoColor.getColor())),
                        Integer.toString(Color.green(photoColor.getColor())),
                        Integer.toString(Color.blue(photoColor.getColor())))
        );

        int colorAccuracy = Math.max(0, 100 - ((photoColor.getCount() / 5)));

        bundle.putInt("accuracy", Math.min(colorAccuracy, photoColor.getDominantCount()));

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
    private static double getNearestColorFromSwatchRange(int color, List<Integer> colorRange,
            double rangeStepUnit) {
        double distance = MAX_COLOR_DISTANCE;
        double nearest = -1;

        double red, green, blue;
        for (int i = 0; i < colorRange.size(); i++) {
            int tempColor = colorRange.get(i);

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


}
