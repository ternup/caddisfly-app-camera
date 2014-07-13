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
import android.view.View;
import android.widget.ImageButton;

import com.robotium.solo.Solo;
import com.ternup.caddisfly.R;
import com.ternup.caddisfly.activity.MainActivity;
import com.ternup.caddisfly.app.Globals;

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


    public void testZCalibrate() {
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        openCalibrate(2);
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

        solo.waitForDialogToClose(40000);

        int colorAfter = getButtonColor(0);

        assertTrue("Calibrate error", colorAfter != colorBefore);

    }

    public void testEditSourceLocation() {
        openHome();

        assertTrue(solo.waitForFragmentByTag(String.valueOf(Globals.HOME_SCREEN_INDEX)));

        solo.clickOnButton(solo.getString(R.string.newLocation));

        //solo.waitForDialogToOpen();

        solo.clickOnText(solo.getString(R.string.address).toUpperCase());

        solo.typeText(0, "This is place name");

        solo.pressSoftKeyboardNextButton();

        solo.clickOnText(solo.getString(R.string.other).toUpperCase());

        solo.waitForText(solo.getString(R.string.selectSourceType));

        solo.clickOnText(solo.getString(R.string.selectSourceType));

        solo.clickOnText("Open Well");

        solo.waitForDialogToClose();

        solo.clickOnActionBarItem(2);
        //solo.clickOnText(solo.getString(R.string.done).toUpperCase());

        solo.waitForDialogToClose();

    }

    public void testNavigationDrawer() {
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);

        openHome();

        openSettings();

        solo.clickOnText("Interval");

        solo.waitForDialogToOpen();

        solo.clickOnButton(solo.getString(R.string.cancel));

        openHelp();

        openAbout();

        openHome();

        solo.goBack();
        assertTrue(solo.waitForFragmentByTag(String.valueOf(Globals.ABOUT_SCREEN_INDEX)));

        solo.goBack();
        assertTrue(solo.waitForFragmentByTag(String.valueOf(Globals.HELP_SCREEN_INDEX)));

        solo.goBack();
        assertTrue(solo.waitForFragmentByTag(String.valueOf(Globals.SETTINGS_SCREEN_INDEX)));

        solo.goBack();
        assertTrue(solo.waitForFragmentByTag(String.valueOf(Globals.HOME_SCREEN_INDEX)));

    }

    public void testDashboard() {

        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);

        openHome();

        openDrawer();
        solo.waitForView(R.id.navigation_drawer);
        solo.sleep(1000);
        solo.searchText(solo.getString(R.string.about));

        closeDrawer();
        solo.waitForView(R.id.navigation_drawer);
        solo.sleep(1000);

        solo.clickOnButton(solo.getString(R.string.help));

        assertTrue(solo.waitForFragmentByTag(String.valueOf(Globals.HELP_SCREEN_INDEX)));

        openHome();

        solo.clickOnButton(solo.getString(R.string.calibrate));

        assertTrue(solo.waitForFragmentByTag(String.valueOf(Globals.CALIBRATE_SCREEN_INDEX)));

        openHome();

        solo.clickOnButton(solo.getString(R.string.locations));

        //assertTrue(solo.waitForFragmentByTag(String.valueOf(Globals.HISTORY_SCREEN_INDEX)));

        openHome();

        solo.clickOnButton(solo.getString(R.string.locations));

        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);

        solo.goBack();

        assertTrue(solo.waitForFragmentByTag(String.valueOf(Globals.HOME_SCREEN_INDEX)));

    }


    public void testLocations() {

        solo.drag(200, 0, 400, 400, 1);

        openHome();

        solo.waitForText(solo.getString(R.string.noData));

//        solo.clickLongInList(1);
//
//        solo.clickInList(1);
//
//        solo.clickInList(3);
//
//        solo.clickInList(4);
//
//        solo.clickInList(5);
//
//        solo.goBack();
//
//        solo.clickInList(1);
//
//        solo.clickOnButton(solo.getString(R.string.details));

        solo.goBack();

    }

    public void testCalibrate() {
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);

        openHome();

        openSettings();

//        assertTrue(solo.isCheckBoxChecked(solo.getString(R.string.shakeDevice)));

        solo.clickOnText(solo.getString(R.string.shakeDevice));

        assertFalse(solo.isCheckBoxChecked(solo.getString(R.string.shakeDevice)));

        openHome();

        openCalibrate(2);

        solo.clickInList(2);

        int colorBefore = getButtonColor(0);

        solo.clickOnButton(solo.getString(R.string.edit));

        solo.waitForDialogToOpen();

        solo.clickOnButton(solo.getString(R.string.cancel));

        solo.clickOnButton(solo.getString(R.string.edit));

        solo.waitForDialogToOpen();

        solo.clickOnButton(solo.getString(R.string.ok));

        solo.waitForText(solo.getString(R.string.invalidColor));

        solo.enterText(0, "344255023");

        solo.waitForText(solo.getString(R.string.invalidColor));

        solo.clickOnButton(solo.getString(R.string.cancel));

        solo.clickOnButton(solo.getString(R.string.edit));

        solo.waitForDialogToOpen();

        solo.enterText(0, "144255023");

        solo.clickOnButton(solo.getString(R.string.ok));

        solo.clickOnButton(solo.getString(R.string.edit));

        solo.waitForDialogToOpen();

        solo.enterText(0, "255-122-53");

        solo.clickOnButton(solo.getString(R.string.ok));

        //solo.waitForText(solo.getString(R.string.calibrate));

        solo.clickOnText(solo.getString(R.string.calibrate), 2);

        solo.waitForDialogToOpen();

        solo.clickOnButton(solo.getString(R.string.ok));

        solo.waitForDialogToOpen(40000);

//        solo.clickOnButton(solo.getString(R.string.analyze));

        solo.waitForDialogToClose(40000);

        int colorAfter = getButtonColor(0);

        assertTrue("Calibrate error", colorAfter != colorBefore);

        solo.goBack();

        solo.clickOnText(solo.getString(R.string.swatches));

    }


    public void testSettings() {

        openHome();

        openSettings();

        assertTrue(solo.waitForFragmentByTag(String.valueOf(Globals.SETTINGS_SCREEN_INDEX)));

        assertTrue(solo.searchText("1 minutes"));

        solo.clickOnText(solo.getString(R.string.analysisCount));

        solo.waitForDialogToOpen();

        solo.clickOnButton(solo.getString(R.string.cancel));

        assertTrue(solo.searchText("1 minutes"));

        solo.clickOnText(solo.getString(R.string.interval));

        solo.waitForDialogToOpen();

        // change value in number picker
        for (View v : solo.getViews()) {
            if (v instanceof ImageButton) {
                solo.sleep(1000);
                solo.clickOnView(v);
                solo.sleep(1000);
                break;
            }
        }

        solo.clickOnButton(solo.getString(R.string.ok));

        //        assertTrue(solo.searchText("15 minutes"));

        assertTrue(solo.searchText("3"));

        solo.clickOnText(solo.getString(R.string.analysisCount));

        solo.waitForDialogToOpen();

        solo.clickOnButton(solo.getString(R.string.cancel));

        assertTrue(solo.searchText("3"));

        solo.clickOnText(solo.getString(R.string.analysisCount));

        solo.waitForDialogToOpen();

        // change value in number picker
        for (View v : solo.getViews()) {
            if (v instanceof ImageButton) {
                solo.sleep(1000);
                solo.clickOnView(v);
                solo.sleep(1000);
                break;
            }
        }

        solo.clickOnButton(solo.getString(R.string.ok));

        //      assertTrue(solo.searchText("4"));
    }

    public void testAbout() {

        openAbout();

        solo.waitForText(solo.getString(R.string.waterQualitySystem));

    }


    public void testTranslate() {

        openHome();

        openSettings();

        solo.clickOnText(solo.getString(R.string.language));

        solo.waitForDialogToOpen();

        solo.clickOnText("Esperanto");

        solo.waitForDialogToClose();

        openHome();

        solo.clickOnButton(solo.getString(R.string.calibrate));

        solo.goBack();

        solo.goBack();

        solo.clickOnText(solo.getString(R.string.language));

        solo.waitForDialogToOpen();

        solo.clickOnText("English");

        solo.waitForDialogToClose();

        openHome();

        solo.clickOnButton(solo.getString(R.string.locations));

        solo.goBack();

    }

    public void testSwatches() {

        openHome();

        openCalibrate(2);

        solo.clickOnText(solo.getString(R.string.swatches));

        solo.scrollListToBottom(0);

        solo.waitForText("3.0");

        solo.scrollListToTop(0);

        solo.waitForText("0.0");
    }


    private void openHome() {
        openHome(false);
    }

    private void openHome(boolean isRtl) {
        solo.sleep(500);
        openDrawer(isRtl);
        solo.waitForView(R.id.navigation_drawer);
        solo.sleep(1000);
        solo.clickOnText(solo.getString(R.string.home));
        assertTrue(solo.waitForFragmentByTag(String.valueOf(Globals.HOME_SCREEN_INDEX)));

        solo.sleep(500);
    }

    private void openCalibrate(int index) {
        openDrawer();
        solo.waitForView(R.id.navigation_drawer);
        solo.sleep(1000);
        solo.clickOnText(solo.getString(R.string.calibrate), index);
        assertTrue(solo.waitForFragmentByTag(String.valueOf(Globals.CALIBRATE_SCREEN_INDEX)));
        solo.sleep(500);
    }

   /* private void openSwatches() {
        solo.sleep(500);
        openDrawer();
        solo.waitForView(R.id.navigation_drawer);
        solo.sleep(1000);
        solo.clickOnText(solo.getString(R.string.swatches));
        assertTrue(solo.waitForFragmentByTag(String.valueOf(Globals.SWATCHES_SCREEN_INDEX)));
    }*/

    private void openSettings() {
        solo.sleep(1500);
        openDrawer();
        solo.waitForView(R.id.navigation_drawer);
        solo.sleep(1000);
        solo.clickOnText(solo.getString(R.string.settings));
        assertTrue(solo.waitForFragmentByTag(String.valueOf(Globals.SETTINGS_SCREEN_INDEX)));
    }

    private void openHelp() {
        openDrawer();
        solo.waitForView(R.id.navigation_drawer);
        solo.sleep(1000);
        solo.clickOnText(solo.getString(R.string.help));
        assertTrue(solo.waitForFragmentByTag(String.valueOf(Globals.HELP_SCREEN_INDEX)));
    }

    private void openAbout() {
        openDrawer();
        solo.waitForView(R.id.navigation_drawer);
        solo.sleep(1000);
        solo.clickOnText(solo.getString(R.string.about));
        assertTrue(solo.waitForFragmentByTag(String.valueOf(Globals.ABOUT_SCREEN_INDEX)));
    }

    private void openDrawer() {
        openDrawer(false);
    }

    private void openDrawer(boolean isRtl) {
        if (isRtl) {
            solo.drag(1000, 400, 500, 500, 5);
        } else {
            solo.drag(0, 400, 500, 500, 5);
        }
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
