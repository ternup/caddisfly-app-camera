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

import com.ternup.caddisfly.R;
import com.ternup.caddisfly.activity.ProgressActivity;
import com.ternup.caddisfly.app.Globals;
import com.ternup.caddisfly.app.MainApp;
import com.ternup.caddisfly.database.LocationTable;
import com.ternup.caddisfly.provider.LocationContentProvider;
import com.ternup.caddisfly.util.AlertUtils;
import com.ternup.caddisfly.util.FileUtils;
import com.ternup.caddisfly.util.ImageUtils;
import com.ternup.caddisfly.util.PreferencesHelper;
import com.ternup.caddisfly.util.PreferencesUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;


public class LocationDetailsFragment extends Fragment implements ResultListFragment.ItemClicked {

    private static final int REQUEST_PHOTO = 6;

    private static final int REQUEST_TEST = 1;

    long mLocationId;

    TextView mAddressText;

    TextView mAddressText2;

    TextView mPlaceText;

    TextView mSourceText;

    TextView mNotesText;

    ImageView mPhotoImageView;

    Button mNewTestButton;

    private ResultFragment resultFragment;

    private ResultListFragment resultListFragment;

    public LocationDetailsFragment() {
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_location_details, container, false);

        mAddressText = (TextView) view.findViewById(R.id.addressTextView);
        mAddressText2 = (TextView) view.findViewById(R.id.address2TextView);
        mPlaceText = (TextView) view.findViewById(R.id.placeTextView);
        mSourceText = (TextView) view.findViewById(R.id.sourceTextView);
        mNotesText = (TextView) view.findViewById(R.id.notesTextView);
        mNewTestButton = (Button) view.findViewById(R.id.newTestButton);

        mNewTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());
                builderSingle.setIcon(R.drawable.ic_launcher);
                builderSingle.setTitle(R.string.selectTestType);
                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(),
                        android.R.layout.select_dialog_singlechoice);
                arrayAdapter.add(getString(R.string.fluoride));
                arrayAdapter.add(getString(R.string.fluoride2));
                arrayAdapter.add(getString(R.string.pH));
                arrayAdapter.add(getString(R.string.bacteria));
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

                                MainApp mainContext = (MainApp) getActivity()
                                        .getApplicationContext();
                                mainContext.currentTestType = which;
                                switch (which) {
                                    case Globals.FLUORIDE_INDEX:
                                        mainContext.setFluorideSwatches();
                                        break;
                                    case Globals.FLUORIDE_2_INDEX:
                                        mainContext.setFluoride2Swatches();
                                        break;
                                    case Globals.PH_INDEX:
                                        mainContext.setPhSwatches();
                                        break;
                                }

                                if (which != Globals.BACTERIA_INDEX) {

                                    MainApp mainApp = (MainApp) getActivity()
                                            .getApplicationContext();
                                    final SharedPreferences sharedPreferences = PreferenceManager
                                            .getDefaultSharedPreferences(getActivity());

                                    int minAccuracy = PreferencesUtils
                                            .getInt(getActivity(), R.string.minPhotoQualityKey, 0);

                                    for (int i = 0; i < mainApp.rangeIntervals.size(); i++) {
                                        final int index = i * mainApp.rangeIncrementStep;
                                        int accuracy = Math.max(-1, sharedPreferences
                                                .getInt(String
                                                        .format("%s-a-%s", String.valueOf(which),
                                                                String.valueOf(index)), -1));
                                        if (accuracy < minAccuracy) {
                                            AlertUtils.showAlert(getActivity(), R.string.error,
                                                    R.string.calibrate_error,
                                                    new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(
                                                                DialogInterface dialogInterface,
                                                                int i) {
                                                            Fragment fragment = CalibrateFragment
                                                                    .newInstance();
                                                            if (fragment != null) {
                                                                FragmentManager fragmentManager
                                                                        = getFragmentManager();
                                                                FragmentTransaction ft
                                                                        = fragmentManager
                                                                        .beginTransaction();
                                                                ft.replace(R.id.container, fragment,
                                                                        String.valueOf(
                                                                                Globals.CALIBRATE_SCREEN_INDEX));
                                                                ft.setTransition(
                                                                        FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                                                                ft.addToBackStack(null);
                                                                ft.commit();
                                                            }

                                                        }
                                                    }, null
                                            );
                                            return;
                                        }
                                    }

                                    startTest(which);
                                } else {
                                    startTest(Globals.BACTERIA_INDEX);
                                }
                            }
                        }
                );
                builderSingle.show();
            }
        });

        mPhotoImageView = (ImageView) view.findViewById(R.id.photoImageView);

        resultListFragment = new ResultListFragment();
        resultListFragment.parentFragment = this;
/*
        resultListFragment = new ResultListFragment(new ResultListFragment.OnItemClickListener() {
            @Override
            public void onItemClick(String folder, long id) {
            }
        });
*/

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();

        ft.replace(R.id.resultPanel, resultListFragment, "resultListFragment");
        ft.setTransition(FragmentTransaction.TRANSIT_NONE);
        ft.commit();

        fillData();
        return view;
    }

    public Cursor getCursor(long id) {
        String[] projection = {LocationTable.COLUMN_ID,
                LocationTable.COLUMN_DATE,
                LocationTable.COLUMN_NAME,
                LocationTable.COLUMN_STREET,
                LocationTable.COLUMN_TOWN,
                LocationTable.COLUMN_CITY,
                LocationTable.COLUMN_STATE,
                LocationTable.COLUMN_COUNTRY,
                LocationTable.COLUMN_STREET,
                LocationTable.COLUMN_SOURCE,
                LocationTable.COLUMN_NOTES};

        Uri uri = ContentUris.withAppendedId(LocationContentProvider.CONTENT_URI, id);
        Cursor cursor = getActivity().getContentResolver().query(uri, projection, null, null, null);
        cursor.moveToFirst();
        return cursor;
    }

    private void fillData() {

        mLocationId = PreferencesHelper.getCurrentLocationId(getActivity(), null);
        Cursor cursor = getCursor(mLocationId);

        if (cursor.getCount() == 0) {
            // something went wrong there is no location. return to home screen
            try {
                FragmentManager manager = getFragmentManager();
                manager.popBackStack(manager.getBackStackEntryAt(0).getId(),
                        FragmentManager.POP_BACK_STACK_INCLUSIVE);
            } catch (Exception e) {
                e.printStackTrace();
                // do nothing
            }
            return;
        }
        String[] sourceArray = getResources().getStringArray(R.array.source_types);

        mPlaceText
                .setText(cursor.getString(cursor.getColumnIndex(LocationTable.COLUMN_NAME)) + ", " +
                        cursor.getString(cursor.getColumnIndex(LocationTable.COLUMN_STREET)));

        getActivity()
                .setTitle(cursor.getString(cursor.getColumnIndex(LocationTable.COLUMN_NAME)) + " " +
                        cursor.getString(cursor.getColumnIndex(LocationTable.COLUMN_TOWN)) + " " +
                        cursor.getString(cursor.getColumnIndex(LocationTable.COLUMN_CITY)));

        mAddressText
                .setText(cursor.getString(cursor.getColumnIndex(LocationTable.COLUMN_TOWN)) + " " +
                        cursor.getString(cursor.getColumnIndex(LocationTable.COLUMN_CITY)));

        mAddressText2
                .setText(cursor.getString(cursor.getColumnIndex(LocationTable.COLUMN_STATE)) + " " +
                        cursor.getString(cursor.getColumnIndex(LocationTable.COLUMN_COUNTRY)));

        int sourceType = cursor.getInt(cursor.getColumnIndex(LocationTable.COLUMN_SOURCE));
        if (sourceType > -1) {
            mSourceText.setText(
                    sourceArray[sourceType]);
        }

        mNotesText.setText(cursor.getString(cursor.getColumnIndex(LocationTable.COLUMN_NOTES)));

        cursor.close();

        File file = new File(
                FileUtils.getStoragePath(getActivity(), mLocationId, "", false) + "/photo");
        if (file.exists()) {
            mPhotoImageView.setImageBitmap(
                    ImageUtils.decodeFile(
                            FileUtils.getStoragePath(getActivity(), mLocationId, "", false)
                                    + "/photo"
                    )
            );
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        try {
            FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
            ft.remove(resultListFragment);
            ft.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startTest(int testType) {

        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getActivity());

        long locationId = sharedPreferences.getLong(PreferencesHelper.CURRENT_LOCATION_ID_KEY, -1);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(PreferencesHelper.CURRENT_TEST_ID_KEY, -1);
        editor.putLong(PreferencesHelper.CURRENT_LOCATION_ID_KEY, locationId);
        editor.commit();

        final Context context = getActivity().getApplicationContext();
        final Intent intent = new Intent(context, ProgressActivity.class);
        intent.putExtra("startTest", true);
        intent.putExtra(PreferencesHelper.CURRENT_TEST_TYPE_KEY, testType);
        intent.putExtra(PreferencesHelper.CURRENT_LOCATION_ID_KEY, locationId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivityForResult(intent, REQUEST_TEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_PHOTO:
                mPhotoImageView.setImageBitmap(ImageUtils.decodeFile(
                        FileUtils.getStoragePath(getActivity(), mLocationId, "", false)));
                break;
            case REQUEST_TEST:
                if (resultCode == Activity.RESULT_OK) {
                    String folderName = data.getStringExtra(PreferencesHelper.FOLDER_NAME_KEY);
                    long id = data.getLongExtra(PreferencesHelper.CURRENT_TEST_ID_KEY, -1);
                    if (id > -1) {
                        displayResult(folderName, id);
                    }
                }
                break;
        }
    }

    private void displayResult(String folderName, long id) {
        Fragment fragment = new ResultFragment();
        if (fragment != null) {

            Bundle args = new Bundle();
            args.putString(PreferencesHelper.FOLDER_NAME_KEY, folderName);
            args.putLong(PreferencesHelper.CURRENT_TEST_ID_KEY, id);
            fragment.setArguments(args);

            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.executePendingTransactions();
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.replace(R.id.container, fragment, Globals.RESULT_SCREEN_TAG);
            ft.addToBackStack(null);
            ft.commit();
            fragmentManager.executePendingTransactions();
        }
    }


    @Override
    public void resultItemClicked(String folder, long id) {
        if (resultFragment == null) {
            resultFragment = new ResultFragment();
        } else {

            //TODO: fix this
            try {
                resultFragment.setArguments(null);
            } catch (Exception e) {
                resultFragment = new ResultFragment();
            }
        }

        FragmentManager fragmentManager = getFragmentManager();
        //fragmentManager.executePendingTransactions();
        FragmentTransaction ft = fragmentManager.beginTransaction();

        Bundle args = new Bundle();
        args.putString(PreferencesHelper.FOLDER_NAME_KEY, folder);
        args.putLong(PreferencesHelper.CURRENT_TEST_ID_KEY, id);

        resultFragment.setArguments(args);
        ft.replace(R.id.container, resultFragment, "resultFragment");
        ft.setTransition(FragmentTransaction.TRANSIT_NONE);
        ft.addToBackStack(null);
        ft.commit();
        fragmentManager.executePendingTransactions();

    }
}
