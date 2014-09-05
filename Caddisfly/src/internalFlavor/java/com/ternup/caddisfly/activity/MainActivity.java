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

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.view.KeyEvent;
import android.view.Menu;

import com.ternup.caddisfly.R;
import com.ternup.caddisfly.app.Globals;
import com.ternup.caddisfly.app.MainApp;
import com.ternup.caddisfly.fragment.AboutItFragment;
import com.ternup.caddisfly.fragment.CalibrateFragment;
import com.ternup.caddisfly.fragment.HelpFragment;
import com.ternup.caddisfly.fragment.HomeFragment;
import com.ternup.caddisfly.fragment.LocationListFragment;
import com.ternup.caddisfly.fragment.NavigationDrawerFragment;
import com.ternup.caddisfly.fragment.SettingsFragment;
import com.ternup.caddisfly.util.DateUtils;
import com.ternup.caddisfly.util.PreferencesUtils;

import java.util.Calendar;

/**
 * The Main Activity of the App which includes navigation drawer menu
 */
public class MainActivity extends MainActivityBase
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private HomeFragment homeFragment = null;

    private LocationListFragment mLocationListFragment = null;

    private CalibrateFragment calibrateFragment = null;

    private HelpFragment helpFragment = null;

    private AboutItFragment aboutFragment = null;

    private SettingsFragment settingsFragment = null;

    private CharSequence mTitle;

    private NavigationDrawerFragment mNavigationDrawerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MainApp mainApp = (MainApp) getApplicationContext();
        assert getApplicationContext() != null;

        // Set theme according to preference
        mainApp.CurrentTheme = PreferencesUtils.getInt(this, R.string.currentTheme,
                R.style.AppTheme_Light);

        this.setTheme(mainApp.CurrentTheme);

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
            displayView(Globals.HOME_SCREEN_INDEX, false);
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
        showCheckUpdateOption = index == Globals.SETTINGS_SCREEN_INDEX;
        showCheckUpdateOption = index == Globals.ABOUT_SCREEN_INDEX;
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
        super.onCreateOptionsMenu(menu);
        restoreActionBar();

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

        Fragment fragment;
        showCheckUpdateOption = false;
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
                showCheckUpdateOption = true;
                if (settingsFragment == null) {
                    settingsFragment = new SettingsFragment();
                }
                fragment = settingsFragment;
                break;
            case Globals.HELP_SCREEN_INDEX:
                showCheckUpdateOption = true;
                if (helpFragment == null) {
                    helpFragment = new HelpFragment();
                }
                fragment = helpFragment;
                break;
            case Globals.ABOUT_SCREEN_INDEX:
                showCheckUpdateOption = true;
                if (aboutFragment == null) {
                    aboutFragment = new AboutItFragment();
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
    protected void onResume() {
        super.onResume();
        int index = getCurrentFragmentIndex();
        if (index > -1) {
            mNavigationDrawerFragment.checkItem(index);
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

            showCheckUpdateOption = index == Globals.SETTINGS_SCREEN_INDEX;
            showCheckUpdateOption = index == Globals.ABOUT_SCREEN_INDEX;

            invalidateOptionsMenu();
            mNavigationDrawerFragment.checkItem(index);

        } catch (Exception e) {
            e.printStackTrace();
        }
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
