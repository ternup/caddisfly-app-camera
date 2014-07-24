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

package com.ternup.caddisfly.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.ternup.caddisfly.R;
import com.ternup.caddisfly.app.Globals;

import java.util.Locale;

public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int HORIZONTAL_MARGIN = 16;

    private OnCalibrateListener mOnCalibrateListener;

    private Activity mActivity;

    public SettingsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.fragmented_preferences);

        PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext())
                .registerOnSharedPreferenceChangeListener(this);

        Preference calibratePreference = findPreference("calibrate");
        if (calibratePreference != null) {
            calibratePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {

                    mOnCalibrateListener.onCalibrate();
                    return true;
                }
            });
        }

        Intent LaunchIntent = getActivity().getPackageManager()
                .getLaunchIntentForPackage(Globals.CADDISFLY_PACKAGE_NAME);
        if (LaunchIntent == null) {
            PreferenceScreen screen = getPreferenceScreen();
            Preference pref = screen.findPreference(getString(R.string.revertVersionKey));
            //PreferenceCategory generalCategory = (PreferenceCategory) findPreference("otherKey");
            if (screen != null) {
                screen.removePreference(pref);
            }
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        if (v != null) {
            ListView lv = (ListView) v.findViewById(android.R.id.list);
            lv.setPadding(0, 0, 0, 0);
            ViewGroup parent = (ViewGroup) lv.getParent();
            if (parent != null) {
                parent.setPadding(0, 0, 0, 0);
            }
        }
        mActivity = getActivity();

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle(R.string.settings);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (mActivity != null) {
            Context context = mActivity.getApplicationContext();
            assert context != null;
            //noinspection CallToStringEquals
            if (context.getString(R.string.currentLocale).equals(s)) {

                PreferenceManager.getDefaultSharedPreferences(mActivity.getApplicationContext())
                        .unregisterOnSharedPreferenceChangeListener(this);
                Locale myLocale = new Locale(
                        sharedPreferences.getString(s, Globals.DEFAULT_LOCALE));
                Resources res = mActivity.getResources();
                DisplayMetrics dm = res.getDisplayMetrics();
                Configuration conf = res.getConfiguration();
                conf.locale = myLocale;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    conf.setLayoutDirection(myLocale);
                }
                res.updateConfiguration(conf, dm);
                mActivity.recreate();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext())
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext())
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mOnCalibrateListener = (OnCalibrateListener) activity;
        } catch (ClassCastException e) {

        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mOnCalibrateListener = null;
    }

    public interface OnCalibrateListener {

        public void onCalibrate();
    }

}
