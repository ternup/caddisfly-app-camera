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

/*
    This file is part of Caddisfly

    Caddisfly is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Caddisfly is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Caddisfly.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.ternup.caddisfly.test;

import com.robotium.solo.Solo;
import com.ternup.caddisfly.R;

import org.akvo.mobile.caddisfly.activity.MainActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;

public class RobotiumTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private Solo solo;

    public RobotiumTest() {
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

/*
    public void testAStartSurvey() {

        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);

        assertTrue(solo.searchText("7"));

        solo.clickOnButton(solo.getString(R.string.startSurvey));

        solo.waitForDialogToOpen();

        solo.clickOnButton(solo.getString(R.string.cancel));

        solo.waitForDialogToClose();

        solo.clickOnButton(solo.getString(R.string.startSurvey));

        solo.waitForDialogToOpen();

        solo.clickOnButton(solo.getString(R.string.ok));

        solo.waitForDialogToClose();

        assertTrue(solo.waitForFragmentByTag(String.valueOf(Globals.CALIBRATE_SCREEN_INDEX)));

        calibrate(3);

        solo.goBack();

        assertTrue(solo.waitForFragmentByTag(String.valueOf(Globals.CALIBRATE_SCREEN_INDEX)));

        solo.goBack();

        assertTrue(solo.waitForFragmentByTag(String.valueOf(Globals.HOME_SCREEN_INDEX)));

        assertTrue(solo.searchText("6"));

    }


    public void testNavigationDrawer() {
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);

        solo.clickOnButton(solo.getString(R.string.calibrate));
        assertTrue(solo.waitForFragmentByTag(String.valueOf(Globals.CALIBRATE_SCREEN_INDEX)));
        solo.goBack();

        solo.clickOnButton(solo.getString(R.string.help));
        assertTrue(solo.waitForFragmentByTag(String.valueOf(Globals.HELP_SCREEN_INDEX)));
        solo.goBack();

        //assertTrue(solo.waitForFragmentByTag(String.valueOf(Globals.HOME_SCREEN_INDEX)));
        //solo.goBack();

    }

    public void testDashboard() {

        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);

        solo.clickOnButton(solo.getString(R.string.help));

        assertTrue(solo.waitForFragmentByTag(String.valueOf(Globals.HELP_SCREEN_INDEX)));

        solo.goBack();

        solo.clickOnButton(solo.getString(R.string.calibrate));

        assertTrue(solo.waitForFragmentByTag(String.valueOf(Globals.CALIBRATE_SCREEN_INDEX)));

        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);

        solo.goBack();
    }

*/
    public void testZCalibrate() {
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);

        solo.clickOnButton(solo.getString(R.string.calibrate));

        calibrate(1);
        solo.goBack();
        calibrate(2);
        solo.goBack();
        calibrate(3);
        solo.goBack();
        calibrate(4);
        solo.goBack();
        calibrate(5);
        solo.goBack();
        calibrate(6);
        solo.goBack();
        calibrate(7);
        solo.goBack();

    }


    private void calibrate(int index) {

        solo.clickInList(index);

        int colorBefore = getButtonColor(0);

        solo.clickOnText(solo.getString(R.string.calibrate), 3);

        solo.waitForDialogToOpen();

        solo.clickOnButton(solo.getString(R.string.ok));

        solo.waitForDialogToOpen();

        solo.waitForDialogToClose(30000);

        int colorAfter = getButtonColor(0);

        assertTrue("Calibrate error", colorAfter != colorBefore);

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
