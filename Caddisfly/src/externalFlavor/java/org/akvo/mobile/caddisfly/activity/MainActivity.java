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

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;

import com.ternup.caddisfly.R;
import com.ternup.caddisfly.activity.MainActivityBase;
import com.ternup.caddisfly.activity.ProgressActivity;
import com.ternup.caddisfly.app.Globals;
import com.ternup.caddisfly.app.MainApp;
import com.ternup.caddisfly.fragment.AboutFragment;
import com.ternup.caddisfly.fragment.HelpFragment;
import com.ternup.caddisfly.fragment.SettingsFragment;
import com.ternup.caddisfly.util.AlertUtils;
import com.ternup.caddisfly.util.FileUtils;
import com.ternup.caddisfly.util.PreferencesHelper;
import com.ternup.caddisfly.util.PreferencesUtils;

import org.akvo.mobile.caddisfly.fragment.CalibrateFragment;
import org.akvo.mobile.caddisfly.fragment.CalibrateMessageFragment;
import org.akvo.mobile.caddisfly.fragment.StartFragment;


public class MainActivity extends MainActivityBase
        implements SettingsFragment.OnCalibrateListener, SettingsFragment.OnAboutListener,
        SettingsFragment.OnCheckUpdateListener, StartFragment.OnStartTestListener,
        StartFragment.OnStartSurveyListener, StartFragment.OnHelpListener,
        CalibrateMessageFragment.ResultDialogListener {

    private final int mTestType = Globals.FLUORIDE_INDEX;

    private CalibrateFragment mCalibrateFragment = null;

    private SettingsFragment mSettingsFragment = null;

    private HelpFragment helpFragment = null;

    private boolean mShouldFinish = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTheme(R.style.Flow_Theme);

        MainApp mainApp = (MainApp) this.getApplicationContext();
        mainApp.CurrentTheme = R.style.Flow_Theme;
        setContentView(R.layout.activity_main);

        Intent LaunchIntent = getPackageManager()
                .getLaunchIntentForPackage(Globals.CADDISFLY_PACKAGE_NAME);
        if (LaunchIntent != null) {
            if (PreferencesUtils.getBoolean(this, R.string.sevenStepCalibrationKey, false)) {
                mainApp.setFluoride2Swatches();
            } else {
                mainApp.setFluorideSwatches();
            }
        }

        if (savedInstanceState == null) {
            displayView(Globals.HOME_SCREEN_INDEX, false);
        }

        FileUtils.trimFolders(this);

        //TODO: temporary, to be removed
        int sampleLength = PreferencesUtils.getInt(this, R.string.photoSampleDimensionKey,
                Globals.SAMPLE_CROP_LENGTH_DEFAULT);
        if (sampleLength > Globals.SAMPLE_CROP_LENGTH_DEFAULT) {
            PreferencesUtils.setInt(this, R.string.photoSampleDimensionKey, Globals.SAMPLE_CROP_LENGTH_DEFAULT);
        }

        if (mainApp.getCalibrationErrorCount(mTestType) > 0) {
            //showWelcome();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
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

        //showCheckUpdateOption = false;
        switch (position) {
            case Globals.HOME_SCREEN_INDEX:
                fragment = StartFragment.newInstance(external, mTestType);
                break;
            case Globals.SETTINGS_SCREEN_INDEX:
                //showCheckUpdateOption = true;
                if (mSettingsFragment == null) {
                    mSettingsFragment = new SettingsFragment();
                }
                fragment = mSettingsFragment;
                break;
            case Globals.CALIBRATE_SCREEN_INDEX:
                if (mCalibrateFragment == null) {
                    mCalibrateFragment = new CalibrateFragment();
                }
                fragment = mCalibrateFragment;
                break;
            default:
                return;
        }
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.container, fragment, String.valueOf(position));
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        if (addToBackStack) {
            ft.addToBackStack(null);
        }
        ft.commit();

        invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (getCurrentFragmentIndex() == Globals.HOME_SCREEN_INDEX) {
            getMenuInflater().inflate(R.menu.home, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                displayView(Globals.SETTINGS_SCREEN_INDEX, true);
                //checkUpdate(false);
/*
                AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
                builderSingle.setIcon(R.drawable.ic_launcher);
                final Context context = this;
                //builderSingle.setTitle(R.string.selectTestType);

                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                        android.R.layout.select_dialog_singlechoice);
                arrayAdapter.addAll(getResources().getStringArray(R.array.languages));

                builderSingle.setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }
                );
                builderSingle.setAdapter(arrayAdapter,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String languageCode = getResources().getStringArray(R.array.language_codes)[which];
                                PreferencesUtils.setString(context, R.string.currentLocale, languageCode);
                                recreate();
                            }
                        }
                );
                builderSingle.show();
*/

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCalibrate() {
        displayView(Globals.CALIBRATE_SCREEN_INDEX, true);
    }

    @Override
    public void onHelp() {
        displayView(Globals.HELP_SCREEN_INDEX, true);
    }

    public void showWelcome() {
        CalibrateMessageFragment calibrateMessageFragment = CalibrateMessageFragment.newInstance();
        final FragmentTransaction ft = getFragmentManager().beginTransaction();

        Fragment prev = getFragmentManager().findFragmentByTag("calibrateMessageFragment");
        if (prev != null) {
            ft.remove(prev);
        }
        calibrateMessageFragment.show(ft, "calibrateMessageFragment");
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
            //finish();

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
            //showCheckUpdateOption = getCurrentFragmentIndex() == Globals.SETTINGS_SCREEN_INDEX;
            invalidateOptionsMenu();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFinishDialog() {
        displayView(Globals.CALIBRATE_SCREEN_INDEX, true);
    }

    @Override
    public void onAbout() {
        AboutFragment aboutFragment = AboutFragment.newInstance();
        final FragmentTransaction ft = getFragmentManager().beginTransaction();

        Fragment prev = getFragmentManager().findFragmentByTag("aboutDialog");
        if (prev != null) {
            ft.remove(prev);
        }
        aboutFragment.show(ft, "aboutDialog");

    }

    @Override
    public void onCheckUpdate() {
        checkUpdate(false);
    }
}
