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
import com.ternup.caddisfly.app.MainApp;
import com.ternup.caddisfly.database.LocationTable;
import com.ternup.caddisfly.fragment.FormFragment;
import com.ternup.caddisfly.fragment.LocationFragment;
import com.ternup.caddisfly.fragment.NotesFragment;
import com.ternup.caddisfly.fragment.PhotoFragment;
import com.ternup.caddisfly.provider.LocationContentProvider;
import com.ternup.caddisfly.util.AlertUtils;
import com.ternup.caddisfly.util.FileUtils;
import com.ternup.caddisfly.util.PreferencesUtils;
import com.ternup.caddisfly.view.SlidingTabLayout;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import java.io.File;
import java.util.Date;
import java.util.Locale;

public class SurveyActivity extends Activity implements LocationFragment.OnCompleteListener {

    NotesFragment mNotesFragment;

    LocationFragment mLocationFragment;

    FormFragment mFormFragment;

    boolean isCancelled = false;

    SectionsPagerAdapter mSectionsPagerAdapter;

    SharedPreferences mPrefs;

    private PowerManager.WakeLock wakeLock;


    /**
     * A {@link android.support.v4.view.ViewPager} which will be used in conjunction with the
     * {@link
     * SlidingTabLayout} above.
     */
    private ViewPager mViewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setTheme(((MainApp) getApplicationContext()).CurrentTheme);

        setContentView(R.layout.fragment_survey);

        // BEGIN_INCLUDE (inflate_set_custom_view)
        // Inflate a "Done/Cancel" custom action bar view.
        final LayoutInflater inflater = (LayoutInflater) getActionBar().getThemedContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View customActionBarView = inflater.inflate(
                R.layout.actionbar_custom_view_done_cancel, null, false);

        final Context context = this;
        customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        long locationId = saveData();
                        if (locationId == -1) {
                            mViewPager.setCurrentItem(1, true);
                            AlertUtils.showMessage(context, R.string.incomplete,
                                    R.string.incompleteMessage);
                        } else {
                            Intent intent = new Intent();
                            intent.putExtra(getString(R.string.currentLocationId), locationId);
                            setResult(Activity.RESULT_OK, intent);
                            //todo: change to finished method
                            isCancelled = true;
                            finish();
                        }
                    }
                }
        );
        customActionBarView.findViewById(R.id.actionbar_cancel).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cancelSurvey();
                    }
                }
        );

        // Show the custom action bar view and hide the normal Home icon and title.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(
                ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
                        | ActionBar.DISPLAY_SHOW_TITLE
        );
        actionBar.setCustomView(customActionBarView,
                new ActionBar.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT)
        );
        // END_INCLUDE (inflate_set_custom_view)

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setOffscreenPageLimit(6);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset,
                    int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 1:
                        //mFormFragment.showKeyboard();
                        break;
                    case 4:
                        mNotesFragment.showKeyboard();
                        break;
                    default:

                        (new Handler()).postDelayed(new Runnable() {

                            public void run() {
                                ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                                        .hideSoftInputFromWindow(mViewPager.getWindowToken(), 0);

                            }
                        }, 300);
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        // END_INCLUDE (setup_viewpager)

        // BEGIN_INCLUDE (setup_slidingTabLayout)
        // Give the SlidingTabLayout the ViewPager, this must be done AFTER the ViewPager has had
        // it's PagerAdapter set.
        /*
      A custom {@link android.support.v4.view.ViewPager} title strip which looks much like Tabs
      present in Android v4.0 and
      above, but is designed to give continuous feedback to the user when scrolling.
     */
        SlidingTabLayout slidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        slidingTabLayout.setViewPager(mViewPager);

    }

    private long saveData() {

        if (isFormIncomplete()) {
            return -1;
        }

        MainApp mainApp = (MainApp) getApplicationContext();
        ContentValues values = new ContentValues();
        values.put(LocationTable.COLUMN_DATE, (new Date().getTime()));

        String featureName = mainApp.address.getFeatureName();
        if (featureName == null || featureName.isEmpty()) {
            return -1;
        }
        values.put(LocationTable.COLUMN_NAME, featureName);

        values.put(LocationTable.COLUMN_STREET,
                mainApp.address.getThoroughfare() == null ? "" : mainApp.address.getThoroughfare());
        values.put(LocationTable.COLUMN_TOWN,
                mainApp.address.getSubLocality() == null ? "" : mainApp.address.getSubLocality());
        values.put(LocationTable.COLUMN_CITY,
                mainApp.address.getLocality() == null ? "" : mainApp.address.getLocality());
        values.put(LocationTable.COLUMN_STATE,
                mainApp.address.getAdminArea() == null ? "" : mainApp.address.getAdminArea());
        values.put(LocationTable.COLUMN_COUNTRY,
                mainApp.address.getCountryName() == null ? "" : mainApp.address.getCountryName());

        values.put(LocationTable.COLUMN_LONGITUDE, mainApp.location.getLongitude());
        values.put(LocationTable.COLUMN_LATITUDE, mainApp.location.getLatitude());
        values.put(LocationTable.COLUMN_ACCURACY, mainApp.location.getAccuracy());

        Bundle bundle = mainApp.address.getExtras();
        int sourceType = bundle.getInt("sourceType", 0);
        String notes = "";
        if (mNotesFragment != null) {
            notes = mNotesFragment.getNotes();
        }
        values.put(LocationTable.COLUMN_SOURCE, sourceType);
        values.put(LocationTable.COLUMN_NOTES, notes);

        Uri uri = getContentResolver().insert(LocationContentProvider.CONTENT_URI, values);
        long id = ContentUris.parseId(uri);

        PreferencesUtils.setLong(this, R.string.currentLocationId, id);

        mainApp.address = new Address(Locale.getDefault());
        mainApp.location = new Location(LocationManager.GPS_PROVIDER);

        File file = new File(FileUtils.getStoragePath(this, 0, "", true) + "photo");

        file.renameTo(new File(FileUtils.getStoragePath(this, id, "", true) + "photo"));

        return id;

    }

    //TODO: rewrite this function
    private boolean isFormEmpty() {
        return true;
    }

    private boolean isFormIncomplete() {

        MainApp mainApp = (MainApp) getApplicationContext();

        if (mainApp.address == null) {
            return true;
        }

/*
        if (
                mainApp.address.getFeatureName().isEmpty() ||
                        mainApp.address.getThoroughfare().isEmpty() ||
                        mainApp.address.getSubLocality().isEmpty() ||
                        mainApp.address.getLocality().isEmpty() ||
                        mainApp.address.getAdminArea().isEmpty() ||
                        mainApp.address.getCountryName().isEmpty()
                ) {
            return true;
        }
*/

        return false;
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return super.onCreateView(name, context, attrs);
    }

    @Override
    public void onPageComplete() {
        mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1, true);
    }

    @Override
    public void onBackPressed() {

        if (mViewPager.getCurrentItem() > 0) {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1, true);
        } else {
            cancelSurvey();
        }
    }

    public void cancelSurvey() {
        if (isFormEmpty()) {
            setResult(Activity.RESULT_CANCELED);
            isCancelled = true;
            finish();
        } else {
            AlertUtils.askQuestion(this, R.string.closeSurvey,
                    R.string.closeLocationSurvey,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(
                                DialogInterface dialogInterface,
                                int i) {
                            setResult(Activity.RESULT_CANCELED);
                            finish();
                        }
                    }, null
            );
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (wakeLock == null || !wakeLock.isHeld()) {
            PowerManager pm = (PowerManager) getApplicationContext()
                    .getSystemService(Context.POWER_SERVICE);
            wakeLock = pm
                    .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
            wakeLock.acquire();
        }
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        isCancelled = true;
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (isCancelled) {
            if (mLocationFragment != null) {
                mLocationFragment.stopUpdates();
                if (wakeLock != null && wakeLock.isHeld()) {
                    wakeLock.release();
                }
                PreferencesUtils.setBoolean(this, R.string.locationUpdatesRequested, false);
            }
        }

    }

    /**
     * A {@link android.support.v13.app.FragmentPagerAdapter} that returns a fragment corresponding
     * to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {


        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).

            switch (position) {
                case 0:
                    if (mLocationFragment == null) {
                        mLocationFragment = LocationFragment.newInstance();
                    }
                    return mLocationFragment;
                case 1:
                    if (mFormFragment == null) {
                        mFormFragment = FormFragment.newInstance();
                    }
                    return mFormFragment;
                case 2:
                    return PhotoFragment.newInstance();
                case 3:
                    if (mNotesFragment == null) {
                        mNotesFragment = NotesFragment.newInstance();
                    }
                    return mNotesFragment;
                default:
                    return mNotesFragment;
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.location).toUpperCase(l);
                case 1:
                    return getString(R.string.address).toUpperCase(l);
                case 2:
                    return getString(R.string.photo).toUpperCase(l);
                case 3:
                    return getString(R.string.other).toUpperCase(l);
            }
            return null;
        }


    }
}