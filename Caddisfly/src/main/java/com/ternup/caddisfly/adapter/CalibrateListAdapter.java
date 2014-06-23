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

package com.ternup.caddisfly.adapter;

import com.ternup.caddisfly.R;
import com.ternup.caddisfly.app.Globals;
import com.ternup.caddisfly.app.MainApp;
import com.ternup.caddisfly.util.PreferencesUtils;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class CalibrateListAdapter extends ArrayAdapter<Double> {

    private final Activity activity;

    private int mTestType = Globals.FLUORIDE_INDEX;

    public CalibrateListAdapter(Activity activity, Double[] rangeArray) {
        super(activity, R.layout.row_calibrate, rangeArray);
        this.activity = activity;
    }

    public void setTestType(int testType) {
        mTestType = testType;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        LayoutInflater inflater = activity.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.row_calibrate, parent, false);

        final MainApp mainApp = ((MainApp) activity.getApplicationContext());

        if (mainApp != null && rowView != null) {
            ArrayList<Integer> colorRange = mainApp.colorList;
            TextView ppmText = (TextView) rowView.findViewById(R.id.ppmText);
            TextView rgbText = (TextView) rowView.findViewById(R.id.rgbText);
            ImageView errorImage = (ImageView) rowView.findViewById(R.id.error);
            Button button = (Button) rowView.findViewById(R.id.button);

            final int index = position * mainApp.rangeIncrementStep;

            if (index < colorRange.size()) {
                // paint the button with the color
                button.setBackgroundColor(colorRange.get(index));

                // display ppm value
                ppmText.setText(mainApp.doubleFormat
                        .format((position + mainApp.rangeStartIncrement) * (
                                mainApp.rangeIncrementValue
                                        * mainApp.rangeIncrementStep)));

                // display color name
                // colorNameText.setText(colorNames.get(currentPosition));
                // display rgb value
                int color = colorRange.get(index);
                rgbText.setText(String.format("%s: %s  %s  %s", mainApp.getString(R.string.rgb),
                        String.format("%d", Color.red(color)),
                        String.format("%d", Color.green(color)),
                        String.format("%d", Color.blue(color))
                ));

                int accuracy = Math.max(-1, PreferencesUtils.getInt(mainApp,
                        String.format("%s-a-%d", mTestType, index), -1));

                int minAccuracy = PreferencesUtils.getInt(mainApp, R.string.minPhotoQualityKey, 0);

                if (accuracy < minAccuracy) {
                    errorImage.setVisibility(View.VISIBLE);
                } else {
                    errorImage.setVisibility(View.GONE);
                }
            }
        }
        return rowView;
    }
}
