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

import android.app.Activity;
import android.app.Fragment;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;

import com.ternup.caddisfly.R;
import com.ternup.caddisfly.app.Globals;
import com.ternup.caddisfly.app.MainApp;
import com.ternup.caddisfly.util.DateUtils;
import com.ternup.caddisfly.util.PreferencesUtils;
import com.ternup.caddisfly.util.UpdateCheckTask;

import java.util.Calendar;
import java.util.Locale;

public class MainActivityBase extends Activity {

    protected long updateLastCheck;

    protected boolean showCheckUpdateOption = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadSavedPreferences();

        updateLastCheck = PreferencesUtils.getLong(this, R.string.lastUpdateCheck);

        // last update check date
        Calendar lastCheckDate = Calendar.getInstance();
        lastCheckDate.setTimeInMillis(updateLastCheck);

        Calendar currentDate = Calendar.getInstance();

        if (!PreferencesUtils.getBoolean(this, R.string.revertVersionKey, false)) {
            if (DateUtils.getDaysDifference(lastCheckDate, currentDate) > 0) {
                checkUpdate(true);
            }
        }
    }

    /**
     * Load user preferences
     */
    private void loadSavedPreferences() {
        assert getApplicationContext() != null;

        MainApp mainApp = ((MainApp) getApplicationContext());

        // Default app to Fluoride swatches
        mainApp.setFluorideSwatches();

        // Set the locale according to preference
        Locale myLocale = new Locale(
                PreferencesUtils.getString(this, R.string.languageKey, Globals.DEFAULT_LOCALE));
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            conf.setLayoutDirection(myLocale);
        }
        res.updateConfiguration(conf, dm);
    }


    /**
     * @param background true: check for update silently, false: show messages to user
     */
    protected void checkUpdate(boolean background) {
        UpdateCheckTask updateCheckTask = new UpdateCheckTask(this, background, false);
        updateCheckTask.execute();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Show the 'Check Update' button for Settings and About screens
        if (showCheckUpdateOption) {
            menu.add(Menu.NONE, R.id.menu_update, Menu.NONE, R.string.checkUpdate)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_update:
                PreferencesUtils.setBoolean(this, R.string.revertVersionKey, false);
                checkUpdate(false);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * @return index of fragment currently showing
     */
    protected int getCurrentFragmentIndex() {
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

}
