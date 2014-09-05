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

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;

import com.robotium.solo.Solo;

import org.akvo.mobile.caddisfly.activity.MainActivity;

public class RobotiumTestSingle extends ActivityInstrumentationTestCase2<MainActivity> {

    private Solo solo;

    public RobotiumTestSingle() {
        super(MainActivity.class);
    }

    protected void setUp() throws Exception {
        super.setUp();
        Context context = this.getInstrumentation().getTargetContext();
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        sharedPreferences.edit().clear().commit();
        solo = new Solo(getInstrumentation(), getActivity());
    }

    private void openDrawer() {
        solo.drag(0, 400, 500, 500, 5);
    }

    private void closeDrawer() {
        solo.drag(400, 0, 500, 500, 5);
    }

    private int getButtonColor(int index) {

        if (index > 0) {
            boolean found = solo.searchButton("", index + 1, true);
            if (!found) {
                index -= 1;
            }
        }
        return ((ColorDrawable) solo.getButton(index).getBackground()).getColor();

    }

    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }
}
