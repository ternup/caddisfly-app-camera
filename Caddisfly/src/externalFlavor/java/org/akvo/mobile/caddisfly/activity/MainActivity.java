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

package org.akvo.mobile.caddisfly.activity;

import com.ternup.caddisfly.R;
import com.ternup.caddisfly.activity.ProgressActivity;
import com.ternup.caddisfly.app.Globals;
import com.ternup.caddisfly.app.MainApp;
import com.ternup.caddisfly.fragment.AboutFragment;
import com.ternup.caddisfly.fragment.HelpFragment;
import com.ternup.caddisfly.util.AlertUtils;
import com.ternup.caddisfly.util.PreferencesHelper;
import com.ternup.caddisfly.util.PreferencesUtils;

import org.akvo.mobile.caddisfly.fragment.CalibrateFragment;
import org.akvo.mobile.caddisfly.fragment.SettingsFragment;
import org.akvo.mobile.caddisfly.fragment.StartFragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;


public class MainActivity extends Activity
        implements StartFragment.OnCalibrateListener, StartFragment.OnStartTestListener,
        StartFragment.OnStartSurveyListener, StartFragment.OnHelpListener {

    private final int REQUEST_TEST = 1;

    private CalibrateFragment mCalibrateFragment = null;

    private HelpFragment helpFragment = null;

    private AboutFragment aboutFragment = null;

    private SettingsFragment settingsFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTheme(R.style.AppTheme_Dark);
        ((MainApp) this.getApplicationContext()).CurrentTheme = R.style.AppTheme_Dark;
        setContentView(R.layout.activity_main);

        //TODO: setup external app connection
        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        Boolean external = false;

        if (Globals.ACTION_WATER_TEST.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                external = true;
            }
        }

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, StartFragment.newInstance(external))
                    .commit();
        }
    }

    public void displayView(int position, boolean addToBackStack) {

        int index = getCurrentFragmentIndex();

        if (index == position) {
            // requested fragment is already showing
            return;
        }

        Fragment fragment;

        //isSettingsShowing = false;
        //isAboutShowing = false;
        switch (position) {
            case Globals.CALIBRATE_SCREEN_INDEX:
                if (mCalibrateFragment == null) {
                    mCalibrateFragment = new CalibrateFragment();
                }
                fragment = mCalibrateFragment;
                break;
            case Globals.SETTINGS_SCREEN_INDEX:
                //isSettingsShowing = true;
                if (settingsFragment == null) {
                    settingsFragment = new SettingsFragment();
                }
                fragment = settingsFragment;
                break;
            case Globals.HELP_SCREEN_INDEX:

                if (helpFragment == null) {
                    helpFragment = new HelpFragment();
                }
                fragment = helpFragment;
                break;
            case Globals.ABOUT_SCREEN_INDEX:
                //isAboutShowing = true;
                if (aboutFragment == null) {
                    aboutFragment = new AboutFragment();
                }
                fragment = aboutFragment;
                break;
            default:
                return;
        }
        if (fragment != null) {

            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.replace(R.id.container, fragment, String.valueOf(position));
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            if (addToBackStack) {
                ft.addToBackStack(null);
            }
            ft.commit();
        }
    }

    /**
     * @return index of fragment currently showing
     */
    private int getCurrentFragmentIndex() {
        Fragment fragment = getFragmentManager().findFragmentById(R.id.container);
        if (fragment != null) {
            String positionString = fragment.getTag();
            if (positionString != null) {
                try {
                    return Integer.parseInt(positionString);
                } catch (NumberFormatException e) {
                    return -1;
                }
            }
        }
        return -1;
    }

    @Override
    public void onCalibrate(int index) {
        displayView(index, true);
    }

    @Override
    public void onHelp(int index) {
        displayView(index, true);
    }

    @Override
    public void onStartSurvey() {
        Intent LaunchIntent = getPackageManager()
                .getLaunchIntentForPackage("com.gallatinsystems.survey.device");
        if (LaunchIntent == null) {
            AlertUtils.showMessage(this, R.string.error, R.string.installAkvoFlow);
        } else {
            startActivity(LaunchIntent);
            finish();
        }
    }

    @Override
    public void onStartTest() {

        Context context = this;
        int testType = 0;

        MainApp mainApp = (MainApp) this.getApplicationContext();
        mainApp.setFluorideSwatches();

        int minAccuracy = PreferencesUtils
                .getInt(context, R.string.minPhotoQualityKey, 0);

        for (int i = 0; i < mainApp.rangeIntervals.size(); i++) {
            final int index = i * mainApp.rangeIncrementStep;
            int accuracy = Math.max(-1, PreferencesUtils.getInt(context, String
                    .format("%s-a-%s", String.valueOf(testType),
                            String.valueOf(index)), -1));
            if (accuracy < minAccuracy) {
                AlertUtils.showAlert(context, R.string.error,
                        R.string.calibrate_error,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(
                                    DialogInterface dialogInterface,
                                    int i) {
                                displayView(Globals.CALIBRATE_SCREEN_INDEX, true);

                            }
                        }, null
                );
                return;
            }
        }

            final Intent intent = new Intent(getIntent());
            intent.setClass(this, ProgressActivity.class);
            //final Intent intent = new Intent(this, ProgressActivity.class);
            intent.putExtra("startTest", true);
            intent.putExtra(PreferencesHelper.CURRENT_TEST_TYPE_KEY, 0);
            intent.putExtra(PreferencesHelper.CURRENT_LOCATION_ID_KEY, -1);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        }

        @Override
        public void onActivityResult ( int requestCode, int resultCode, Intent data){
            super.onActivityResult(requestCode, resultCode, data);

            switch (requestCode) {
                case REQUEST_TEST:
                    if (resultCode == Activity.RESULT_OK) {
                        Intent intent = new Intent(data);
                        this.setResult(Activity.RESULT_OK, intent);
                        finish();
                    }
                    break;
            }
        }

    }
