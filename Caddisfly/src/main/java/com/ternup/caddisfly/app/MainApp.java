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

import android.app.Application;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Camera;
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

    public Camera camera;

    public int CurrentTheme = R.style.AppTheme_Light;

    public ArrayList<Integer> colorList = new ArrayList<Integer>();

    public Address address = new Address(Locale.getDefault());

    public Location location = new Location(LocationManager.GPS_PROVIDER);

    public double rangeIncrementValue = 0.1;

    public DecimalFormat doubleFormat = new DecimalFormat("0.0");

    public MainApp() {

        //Factory preset color range (0.0 to 3.0 with increments of 0.1)
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

        for (double i = 0.0; i < 3.5; i += 0.5) {
            rangeIntervals.add(i);
        }

        //TODO: Move preset colors to raw file
        presetColorList.add(Color.rgb(207, 90, 179));
        presetColorList.add(Color.rgb(209, 98, 180));
        presetColorList.add(Color.rgb(212, 107, 181));
        presetColorList.add(Color.rgb(215, 115, 182));
        presetColorList.add(Color.rgb(218, 124, 183));
        presetColorList.add(Color.rgb(221, 133, 184));
        presetColorList.add(Color.rgb(217, 134, 179));
        presetColorList.add(Color.rgb(213, 135, 175));
        presetColorList.add(Color.rgb(210, 136, 171));
        presetColorList.add(Color.rgb(206, 137, 167));
        presetColorList.add(Color.rgb(203, 139, 163));
        presetColorList.add(Color.rgb(205, 143, 165));
        presetColorList.add(Color.rgb(208, 148, 168));
        presetColorList.add(Color.rgb(211, 153, 171));
        presetColorList.add(Color.rgb(214, 158, 174));
        presetColorList.add(Color.rgb(217, 163, 177));
        presetColorList.add(Color.rgb(215, 161, 174));
        presetColorList.add(Color.rgb(213, 160, 171));
        presetColorList.add(Color.rgb(212, 158, 168));
        presetColorList.add(Color.rgb(210, 157, 165));
        presetColorList.add(Color.rgb(209, 156, 162));
        presetColorList.add(Color.rgb(203, 154, 159));
        presetColorList.add(Color.rgb(198, 153, 157));
        presetColorList.add(Color.rgb(192, 152, 155));
        presetColorList.add(Color.rgb(187, 151, 153));
        presetColorList.add(Color.rgb(182, 150, 151));
        presetColorList.add(Color.rgb(184, 153, 153));
        presetColorList.add(Color.rgb(187, 157, 155));
        presetColorList.add(Color.rgb(190, 161, 157));
        presetColorList.add(Color.rgb(193, 165, 159));
        presetColorList.add(Color.rgb(196, 169, 162));

        colorList = new ArrayList<Integer>(presetColorList);
        loadCalibratedSwatches(Globals.FLUORIDE_INDEX);
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

        for (int i = 1; i < 15; i++) {
            rangeIntervals.add((double) i);
        }

        //TODO: Move preset colors to raw file
        presetColorList.add(Color.rgb(235, 30, 37));
        presetColorList.add(Color.rgb(238, 120, 33));
        presetColorList.add(Color.rgb(244, 156, 30));
        presetColorList.add(Color.rgb(242, 187, 24));

        presetColorList.add(Color.rgb(247, 220, 9));
        presetColorList.add(Color.rgb(246, 237, 18));

        presetColorList.add(Color.rgb(202, 216, 43));
        presetColorList.add(Color.rgb(171, 198, 69));

        presetColorList.add(Color.rgb(122, 174, 162));
        presetColorList.add(Color.rgb(68, 149, 204));
        presetColorList.add(Color.rgb(49, 122, 190));
        presetColorList.add(Color.rgb(62, 109, 181));

        presetColorList.add(Color.rgb(94, 79, 162));
        presetColorList.add(Color.rgb(97, 64, 151));
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
                    .getInt(String.format("%s-%s", String.valueOf(testType), String.valueOf(i)),
                            -1);
            if (value != -1) {
                colorList.set(i, value);
            }
        }
    }

}
