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

package com.ternup.caddisfly.test;

import android.content.Intent;
import android.test.ActivityUnitTestCase;
import android.widget.Button;

import com.ternup.caddisfly.R;

import org.akvo.mobile.caddisfly.activity.MainActivity;

public class MainActivityTest extends ActivityUnitTestCase<MainActivity> {

    private MainActivity activity;

    public MainActivityTest() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Intent intent = new Intent(getInstrumentation().getTargetContext(),
                MainActivity.class);
        startActivity(intent, null, null);
        activity = getActivity();
    }

    public void testLayout() {
        int buttonId = R.id.startSurveyButton;
        assertNotNull(activity.findViewById(buttonId));
        Button startButton = (Button) activity.findViewById(buttonId);
        assertEquals("Button text not correct", getActivity().getString(R.string.fluoride),
                startButton.getText());
    }

    public void testStartButtonClick() {
        Button startButton = (Button) activity.findViewById(R.id.startButton);
        assertNotNull("Button is null", startButton);

/*        startButton.performClick();

        Button startButton = (Button) activity.findViewById(R.id.startButton);

        startButton.performClick();

        Button chartButton = (Button) activity.findViewById(R.id.chartButton);
        assertNotNull("Button is null", chartButton);
        assertEquals("Button text not correct", "Chart", chartButton.getText());

        Button calibrateButton = (Button) activity.findViewById(R.id.calibrateButton);

        assertNotNull("Button is null", calibrateButton);
        assertEquals("Button text not correct", "Start Test", calibrateButton.getText());
*/

    }
}
