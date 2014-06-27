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

package com.ternup.caddisfly.app;

import com.ternup.caddisfly.R;
import com.ternup.caddisfly.util.PreferencesUtils;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.preference.PreferenceManager;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;

public class MainApp extends Application {

    //Global color lists
    public final ArrayList<Integer> presetColorList = new ArrayList<Integer>();

    public final ArrayList<Double> rangeIntervals = new ArrayList<Double>();

    public int rangeIncrementStep = 5;

    public int rangeStartIncrement = 0;

    public int CurrentTheme = R.style.AppTheme_Light;

    public int currentTestType = Globals.FLUORIDE_INDEX;

    public ArrayList<Integer> colorList = new ArrayList<Integer>();

    public Address address = new Address(Locale.getDefault());

    public Location location = new Location(LocationManager.GPS_PROVIDER);

    public double rangeIncrementValue = 0.1;

    public DecimalFormat doubleFormat = new DecimalFormat("0.0");

    public MainApp() {
    }

    public static String getVersion(Context context) {
        try {
            String version = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).versionName;
            String[] words = version.split("\\s");
            String versionString = "";
            for (String word : words) {
                try {
                    Double versionNumber = Double.parseDouble(word);
                    versionString += String.format("%.2f", versionNumber);
                } catch (NumberFormatException e) {
                    int id = context.getResources()
                            .getIdentifier(word, "string", context.getPackageName());
                    if (id > 0) {
                        versionString += context.getString(id);
                    } else {
                        versionString += word;
                    }
                }
                versionString += " ";
            }
            return versionString.trim();

        } catch (PackageManager.NameNotFoundException e) {
            return "";
        }

    }

    /**
     * Factory preset values for Fluoride
     */
    public void setFluorideSwatches() {
        presetColorList.clear();
        rangeIntervals.clear();

        rangeIncrementStep = 5;
        rangeIncrementValue = 0.1;
        rangeStartIncrement = 0;
        doubleFormat = new DecimalFormat("0.0");

        currentTestType = Globals.FLUORIDE_INDEX;

        for (double i = 0.0; i < 3.5; i += 0.5) {
            rangeIntervals.add(i);
        }

        for (double i = 0; i < 31; i++) {
            presetColorList.add(Color.rgb(0, 0, 0));
        }

        colorList = new ArrayList<Integer>(presetColorList);
        loadCalibratedSwatches(Globals.FLUORIDE_INDEX);
    }

    /**
     * Factory preset values for Fluoride
     */
    public void setFluoride2Swatches() {
        presetColorList.clear();
        rangeIntervals.clear();

        rangeIncrementStep = 30;
        rangeIncrementValue = 0.1;
        rangeStartIncrement = 0;
        doubleFormat = new DecimalFormat("0.0");

        currentTestType = Globals.FLUORIDE_2_INDEX;

        for (double i = 0.0; i < 3.1; i += (rangeIncrementStep * rangeIncrementValue)) {
            rangeIntervals.add(i);
        }

        for (double i = 0; i < 31; i++) {
            presetColorList.add(Color.rgb(0, 0, 0));
        }

        colorList = new ArrayList<Integer>(presetColorList);
        loadCalibratedSwatches(Globals.FLUORIDE_2_INDEX);
    }

    /**
     * Factory preset values for pH Test
     */
    public void setPhSwatches() {
        presetColorList.clear();
        rangeIntervals.clear();

        rangeIncrementStep = 1;
        rangeIncrementValue = 1;
        rangeStartIncrement = 1;
        doubleFormat = new DecimalFormat("0");

        currentTestType = Globals.PH_INDEX;

        for (int i = 1; i < 15; i++) {
            rangeIntervals.add((double) i);
        }

        for (double i = 0; i < 14; i++) {
            presetColorList.add(Color.rgb(0, 0, 0));
        }

        colorList = new ArrayList<Integer>(presetColorList);
        loadCalibratedSwatches(Globals.PH_INDEX);
    }

    /**
     * Load any user calibrated swatches which overrides factory preset swatches
     *
     * @param testType The type of test
     */
    void loadCalibratedSwatches(int testType) {
        MainApp context = ((MainApp) this.getApplicationContext());
        assert context != null;
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);

        for (int i = 0; i < colorList.size(); i++) {
            int value = sharedPreferences
                    .getInt(String.format("%d-%d", testType, i),
                            -1);

            if (value != -1) {
                int r = Color.red(value);
                int g = Color.green(value);
                int b = Color.blue(value);

                // eliminate white and black colors
                if (r == 255 && g == 255 && b == 255) {
                    PreferencesUtils.setInt(this, String.format("%d-a-%d", testType, i), -1);
                    value = -1;
                }

                if (r == 0 && g == 0 && b == 0) {
                    PreferencesUtils.setInt(this, String.format("%d-a-%d", testType, i), -1);
                    value = -1;
                }

                if (value != -1) {
                    colorList.set(i, value);
                }
            }
        }
    }

    public void saveCalibratedSwatches(int testType, ArrayList<Integer> colorList) {
        MainApp context = ((MainApp) this.getApplicationContext());
        assert context != null;

        for (int i = 0; i < colorList.size(); i++) {

            PreferencesUtils
                    .setInt(context.getApplicationContext(),
                            String.format("%d-%d", testType, i),
                            colorList.get(i));
            PreferencesUtils
                    .setInt(context.getApplicationContext(),
                            String.format("%d-a-%d", testType, i), 100);
        }
    }

    public int getCalibrationErrorCount(int testType) {
        MainApp mainApp = this;
        int minAccuracy = PreferencesUtils
                .getInt(mainApp, R.string.minPhotoQualityKey, Globals.MINIMUM_PHOTO_QUALITY);

        int count = 0;
        for (int i = 0; i < mainApp.rangeIntervals.size(); i++) {
            final int index = i * mainApp.rangeIncrementStep;
            int accuracy = Math.max(-1, PreferencesUtils.getInt(mainApp, String
                    .format("%d-a-%d", testType, index), -1));
            if (accuracy < minAccuracy) {
                count++;
            }
        }
        return count;
    }

}
