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

package com.ternup.caddisfly.fragment;


import com.ternup.caddisfly.R;
import com.ternup.caddisfly.app.MainApp;
import com.ternup.caddisfly.util.PreferencesUtils;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class CalibrateItemFragment extends CalibrateItemFragmentBase {
    private TextView mRgbText;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View header = getActivity().getLayoutInflater()
                .inflate(R.layout.fragment_calibrate_item, null, false);

        mRgbText = (TextView) header.findViewById(R.id.rgbText);
    }

    @Override
    void displayInfo(boolean animate) {
        super.displayInfo(animate);

        final MainApp mainApp = ((MainApp) getActivity().getApplicationContext());

        final int position = getArguments().getInt(getString(R.string.swatchIndex));
        final int index = position * mainApp.rangeIncrementStep;

        int color = PreferencesUtils.getInt(mainApp,
                String.format("%s-%s", String.valueOf(mTestType), String.valueOf(index)),
                -1);

        mRgbText.setText(String.format("%s: %s  %s  %s", mainApp.getString(R.string.rgb),
                String.format("%d", Color.red(color)),
                String.format("%d", Color.green(color)),
                String.format("%d", Color.blue(color))
        ));

/*
        if (accuracy == -1) {
            mColorButton.setText(getActivity().getString(R.string.notCalibrated));
            color = Color.WHITE;
            mRgbText.setVisibility(View.GONE);
        } else {
            mRgbText.setVisibility(View.VISIBLE);
        }
*/

    }
}
