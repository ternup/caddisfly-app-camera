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

import java.util.ArrayList;
import java.util.Locale;

public class MainApp extends Application {

    //Global color lists
    private final ArrayList<Integer> presetColorList = new ArrayList<Integer>();

    public Camera camera;

    //private final ArrayList<Double> rangeIntervals = new ArrayList<Double>();

    public int CurrentTheme = R.style.AppTheme_Light;

    public ArrayList<Integer> colorList = new ArrayList<Integer>();

    public Address address = new Address(Locale.getDefault());

    public Location location = new Location(LocationManager.GPS_PROVIDER);

    public MainApp() {

        //Factory preset color range (0.0 to 3.0 with increments of 0.1)
/*
        rangeIntervals.add(0.0);
        rangeIntervals.add(0.5);
        rangeIntervals.add(1.0);
        rangeIntervals.add(1.5);
        rangeIntervals.add(2.0);
        rangeIntervals.add(2.5);
        rangeIntervals.add(3.0);
*/
    }

    /**
     * Factory preset values for Fluoride
     */
    public void setFluorideSwatches() {
        presetColorList.clear();
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
        //TODO: Move preset colors to raw file
        presetColorList.add(Color.rgb(255, 246, 246));
        presetColorList.add(Color.rgb(255, 238, 238));
        presetColorList.add(Color.rgb(255, 230, 230));
        presetColorList.add(Color.rgb(255, 222, 222));
        presetColorList.add(Color.rgb(255, 213, 213));
        presetColorList.add(Color.rgb(255, 205, 205));
        presetColorList.add(Color.rgb(255, 197, 197));
        presetColorList.add(Color.rgb(255, 189, 189));
        presetColorList.add(Color.rgb(255, 180, 180));
        presetColorList.add(Color.rgb(255, 172, 172));
        presetColorList.add(Color.rgb(255, 164, 164));
        presetColorList.add(Color.rgb(255, 156, 156));
        presetColorList.add(Color.rgb(255, 148, 148));
        presetColorList.add(Color.rgb(255, 139, 139));
        presetColorList.add(Color.rgb(255, 131, 131));
        presetColorList.add(Color.rgb(255, 123, 123));
        presetColorList.add(Color.rgb(255, 115, 115));
        presetColorList.add(Color.rgb(255, 106, 106));
        presetColorList.add(Color.rgb(255, 98, 98));
        presetColorList.add(Color.rgb(255, 90, 90));
        presetColorList.add(Color.rgb(255, 82, 82));
        presetColorList.add(Color.rgb(255, 74, 74));
        presetColorList.add(Color.rgb(255, 65, 65));
        presetColorList.add(Color.rgb(255, 57, 57));
        presetColorList.add(Color.rgb(255, 49, 49));
        presetColorList.add(Color.rgb(255, 41, 41));
        presetColorList.add(Color.rgb(255, 32, 32));
        presetColorList.add(Color.rgb(255, 24, 24));
        presetColorList.add(Color.rgb(255, 16, 16));
        presetColorList.add(Color.rgb(255, 8, 8));
        presetColorList.add(Color.rgb(255, 0, 0));
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

        for (int i = 0; i <= 30; i++) {
            int value = sharedPreferences
                    .getInt(String.valueOf(testType) + "-" + String.valueOf(i), -1);
            if (value != -1) {
                colorList.set(i, value);
            }
        }
    }

}
