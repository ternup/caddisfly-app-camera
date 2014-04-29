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

package com.ternup.caddisfly.activity;

import com.ternup.caddisfly.R;
import com.ternup.caddisfly.app.Globals;
import com.ternup.caddisfly.app.MainApp;
import com.ternup.caddisfly.fragment.AboutFragment;
import com.ternup.caddisfly.fragment.CalibrateFragment;
import com.ternup.caddisfly.fragment.HelpFragment;
import com.ternup.caddisfly.fragment.HomeFragment;
import com.ternup.caddisfly.fragment.LocationListFragment;
import com.ternup.caddisfly.fragment.NavigationDrawerFragment;
import com.ternup.caddisfly.fragment.SettingsFragment;
import com.ternup.caddisfly.util.DateUtils;
import com.ternup.caddisfly.util.PreferencesUtils;
import com.ternup.caddisfly.util.UpdateCheckTask;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Calendar;
import java.util.Locale;

/**
 * The Main Activity of the App which includes navigation drawer menu
 */
public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private HomeFragment homeFragment = null;

    private LocationListFragment mLocationListFragment = null;

    private CalibrateFragment calibrateFragment = null;

    private HelpFragment helpFragment = null;

    private AboutFragment aboutFragment = null;

    private SettingsFragment settingsFragment = null;

    private long updateLastCheck;

    private boolean isSettingsShowing = false;

    private boolean isAboutShowing = false;

    private CharSequence mTitle;

    private NavigationDrawerFragment mNavigationDrawerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadSavedPreferences();

        assert getApplicationContext() != null;
        this.setTheme(((MainApp) getApplicationContext()).CurrentTheme);

        setContentView(R.layout.activity_main);

        mTitle = getTitle();

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        // Set default values for preference settings
        PreferenceManager.setDefaultValues(this, R.xml.fragmented_preferences, false);

        if (savedInstanceState == null) {
            displayView(0, false);
        }

        // last update check date
        Calendar lastCheckDate = Calendar.getInstance();
        lastCheckDate.setTimeInMillis(updateLastCheck);

        Calendar currentDate = Calendar.getInstance();

        if (DateUtils.getDaysDifference(lastCheckDate, currentDate) > 0) {
            checkUpdate(true);
        }

        // enable ActionBar app icon to behave as action to toggle nav drawer
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);
        }

        int index = getCurrentFragmentIndex();
        isSettingsShowing = index == Globals.SETTINGS_SCREEN_INDEX;
        isAboutShowing = index == Globals.ABOUT_SCREEN_INDEX;
    }

    /**
     * @param background true: check for update silently, false: show messages to user
     */
    void checkUpdate(boolean background) {
        UpdateCheckTask updateCheckTask = new UpdateCheckTask(this, background);
        updateCheckTask.execute();
    }

    /**
     *
     */
    void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(mTitle);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        restoreActionBar();

        // Show the 'Check Update' button for Settings and About screens
        if (isSettingsShowing || isAboutShowing) {
            menu.add(Menu.NONE, R.id.menu_update, Menu.NONE, R.string.checkUpdate)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
        return true;
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        displayView(position, true);
    }

    /**
     * Display view fragment for selected nav drawer menu option
     */
    public void displayView(int position, boolean addToBackStack) {

        int index = getCurrentFragmentIndex();

        if (index == position) {
            // requested fragment is already showing
            return;
        }

        Fragment fragment = null;

        isSettingsShowing = false;
        isAboutShowing = false;
        switch (position) {
            case Globals.HOME_SCREEN_INDEX:
                if (homeFragment == null) {
                    homeFragment = new HomeFragment();
                }
                fragment = homeFragment;
                break;
            case Globals.LOCATION_LIST_SCREEN_INDEX:
                if (mLocationListFragment == null) {
                    mLocationListFragment = new LocationListFragment();
                }
                fragment = mLocationListFragment;
                break;
            case Globals.CALIBRATE_SCREEN_INDEX:
                if (calibrateFragment == null) {
                    calibrateFragment = new CalibrateFragment();
                }
                fragment = calibrateFragment;
                break;
            case Globals.SETTINGS_SCREEN_INDEX:
                isSettingsShowing = true;
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
                isAboutShowing = true;
                if (aboutFragment == null) {
                    aboutFragment = new AboutFragment();
                }
                fragment = aboutFragment;
                break;
            default:
                toggleTheme();
                return;
        }
        invalidateOptionsMenu();
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
        mNavigationDrawerFragment.checkItem(position);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        ActionBar actionBar = getActionBar();
        assert actionBar != null;
        actionBar.setTitle(title);
    }

    @Override
    protected void onStart() {
        super.onStart();
        assert getApplicationContext() != null;
        //final String folderName = PreferencesUtils.getString(this, R.string.runningTestFolder, "");
        //final int testType = PreferencesUtils.getInt(this, R.string.testType, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        int index = getCurrentFragmentIndex();
        if (index > -1) {
            mNavigationDrawerFragment.checkItem(index);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_update:
                checkUpdate(false);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Toggle the theme between light and dark styles
     */
    private void toggleTheme() {
        assert getApplicationContext() != null;
        MainApp mainApp = (MainApp) getApplicationContext();

        mainApp.CurrentTheme = mainApp.CurrentTheme == R.style.AppTheme_Light
                ? R.style.AppTheme_Dark : R.style.AppTheme_Light;

        PreferencesUtils.setInt(this, R.string.currentTheme, mainApp.CurrentTheme);

        this.recreate();
    }

    /**
     * Load user preferences
     */
    private void loadSavedPreferences() {
        assert getApplicationContext() != null;

        updateLastCheck = PreferencesUtils.getLong(this, R.string.lastUpdateCheck);

        MainApp context = ((MainApp) this.getApplicationContext());

        // Default app to Fluoride swatches
        context.setFluorideSwatches();

        // Set the locale according to preference
        Locale myLocale = new Locale(
                PreferencesUtils.getString(this, R.string.currentLocale, Globals.DEFAULT_LOCALE));
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);

        // Set theme according to preference
        context.CurrentTheme = PreferencesUtils.getInt(this, R.string.currentTheme,
                R.style.AppTheme_Light);
    }

    @Override
    public void onBackPressed() {
        try {
            int index = getCurrentFragmentIndex();

            if (index == Globals.LOCATION_LIST_SCREEN_INDEX) {
                Fragment fragment = getFragmentManager().findFragmentById(R.id.container);
                if (((LocationListFragment) fragment).backPressHandled()) {
                    return;
                }
            }
            // or just go back to main activity
            super.onBackPressed();

            index = getCurrentFragmentIndex();

            mNavigationDrawerFragment.checkItem(index);

            isSettingsShowing = index == Globals.SETTINGS_SCREEN_INDEX;
            isAboutShowing = index == Globals.ABOUT_SCREEN_INDEX;
            invalidateOptionsMenu();

        } catch (Exception e) {
            e.printStackTrace();
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
    public boolean onKeyDown(int keycode, KeyEvent e) {
        switch (keycode) {
            case KeyEvent.KEYCODE_MENU:
                mNavigationDrawerFragment.toggleDrawer();
                return true;
        }

        return super.onKeyDown(keycode, e);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        assert getApplicationContext() != null;
        outState.putInt(getString(R.string.currentTheme),
                ((MainApp) getApplicationContext()).CurrentTheme);
    }
}
