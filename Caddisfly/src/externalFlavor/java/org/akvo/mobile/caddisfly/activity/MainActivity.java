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
import com.ternup.caddisfly.activity.MainActivityBase;
import com.ternup.caddisfly.activity.ProgressActivity;
import com.ternup.caddisfly.app.Globals;
import com.ternup.caddisfly.app.MainApp;
import com.ternup.caddisfly.fragment.HelpFragment;
import com.ternup.caddisfly.util.AlertUtils;
import com.ternup.caddisfly.util.FileUtils;
import com.ternup.caddisfly.util.PreferencesHelper;

import org.akvo.mobile.caddisfly.fragment.CalibrateFragment;
import org.akvo.mobile.caddisfly.fragment.StartFragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;


public class MainActivity extends MainActivityBase
        implements StartFragment.OnCalibrateListener, StartFragment.OnStartTestListener,
        StartFragment.OnStartSurveyListener, StartFragment.OnHelpListener {

    private final int mTestType = Globals.FLUORIDE_INDEX;

    private CalibrateFragment mCalibrateFragment = null;

    private HelpFragment helpFragment = null;

    private boolean mShouldFinish = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTheme(R.style.Flow_Theme);

        MainApp mainApp = (MainApp) this.getApplicationContext();
        mainApp.CurrentTheme = R.style.Flow_Theme;
        setContentView(R.layout.activity_main);

        displayView(Globals.HOME_SCREEN_INDEX, false);

        FileUtils.trimFolders(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        showCheckUpdateOption = false;
        mShouldFinish = false;
    }

    void displayView(int position, boolean addToBackStack) {

        int index = getCurrentFragmentIndex();

        if (index == position) {
            // requested fragment is already showing
            return;
        }

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        Boolean external = false;

        if (Globals.ACTION_WATER_TEST.equals(action) && type != null) {
            if ("text/plain".equals(type)) { //NON-NLS
                external = true;
                //mQuestionId = getIntent().getStringExtra("questionId");
                //questionTitle = getIntent().getStringExtra("questionTitle");

                //TODO: switch to correct test type
                //String code = questionTitle.substring(Math.max(0, questionTitle.length() - 5));
                //mTestType = DataHelper.getTestTypeFromCode(code);
            }
        }

        Fragment fragment;

        showCheckUpdateOption = false;
        switch (position) {
            case Globals.HOME_SCREEN_INDEX:
                fragment = StartFragment.newInstance(external, mTestType);
                break;
            case Globals.CALIBRATE_SCREEN_INDEX:
                if (mCalibrateFragment == null) {
                    mCalibrateFragment = new CalibrateFragment();
                }
                fragment = mCalibrateFragment;
                break;
            case Globals.HELP_SCREEN_INDEX:
                showCheckUpdateOption = true;
                if (helpFragment == null) {
                    helpFragment = new HelpFragment();
                }
                fragment = helpFragment;
                break;
            default:
                return;
        }
        invalidateOptionsMenu();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.container, fragment, String.valueOf(position));
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        if (addToBackStack) {
            ft.addToBackStack(null);
        }
        ft.commit();
    }


    @Override
    public void onCalibrate() {
        displayView(Globals.CALIBRATE_SCREEN_INDEX, true);
    }

    @Override
    public void onHelp() {
        displayView(Globals.HELP_SCREEN_INDEX, true);
    }

    @Override
    public void onStartSurvey() {

        Context context = this;

        MainApp mainApp = (MainApp) this.getApplicationContext();

        if (mainApp.getCalibrationErrorCount(mTestType) > 0) {
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

        Intent LaunchIntent = getPackageManager()
                .getLaunchIntentForPackage(Globals.FLOW_SURVEY_PACKAGE_NAME);
        if (LaunchIntent == null) {
            AlertUtils.showMessage(this, R.string.error, R.string.installAkvoFlow);
        } else {
            startActivity(LaunchIntent);
            mShouldFinish = true;
            (new Handler()).postDelayed(new Runnable() {
                public void run() {
                    if (mShouldFinish) {
                        finish();
                    }
                }
            }, 6000);
        }
    }

    @Override
    public void onStartTest() {

        Context context = this;

        MainApp mainApp = (MainApp) context.getApplicationContext();

        // TODO: change according to test type
        mainApp.setFluorideSwatches();

        if (mainApp.getCalibrationErrorCount(mTestType) > 0) {
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

        final Intent intent = new Intent(getIntent());
        intent.setClass(context, ProgressActivity.class);
        intent.putExtra(PreferencesHelper.CURRENT_LOCATION_ID_KEY, (long) 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        try {
            int index = getCurrentFragmentIndex();
            showCheckUpdateOption = index == Globals.HELP_SCREEN_INDEX;
            invalidateOptionsMenu();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
