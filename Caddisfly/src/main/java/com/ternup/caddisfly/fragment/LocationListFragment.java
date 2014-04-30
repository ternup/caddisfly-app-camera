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
import com.ternup.caddisfly.activity.SurveyActivity;
import com.ternup.caddisfly.adapter.CheckboxSimpleCursorAdapter;
import com.ternup.caddisfly.database.LocationTable;
import com.ternup.caddisfly.provider.LocationContentProvider;
import com.ternup.caddisfly.util.AlertUtils;
import com.ternup.caddisfly.util.FileUtils;
import com.ternup.caddisfly.util.PreferencesUtils;
import com.ternup.caddisfly.util.TimeUtils;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class LocationListFragment extends ListFragment implements AdapterView.OnItemClickListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int REQUEST_LOCATION = 2;

    final String[] projection = {
            LocationTable.COLUMN_DATE,
            LocationTable.COLUMN_NAME,
            LocationTable.COLUMN_TOWN,
            LocationTable.COLUMN_STATE,
            LocationTable.COLUMN_STREET,
            LocationTable.COLUMN_CITY,
            LocationTable.COLUMN_COUNTRY,
            LocationTable.COLUMN_SOURCE,
            LocationTable.COLUMN_ID
    };

    private final boolean showCheckbox = false;

    int mTestType = 0;

    private LocationDetailsFragment mLocationDetailsFragment;

    private CheckboxSimpleCursorAdapter adapter;

    private Menu mMenu;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_location_list, container, false);
        TextView testTitleTextView = (TextView) view.findViewById(R.id.testTitle);
        if (getArguments() == null) {
            testTitleTextView.setVisibility(View.GONE);
        } else {
/*
            testTitleTextView.setText(
                    Utility.getTestTitle(getActivity(), mTestType) + ": " + getString(
                            R.string.selectLocation)
            );
            testTitleTextView.setBackgroundColor(Utility.getTestColor(getActivity(), mTestType));
*/
        }
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.location, menu);
        mMenu = menu;
    }

    private void displayAddLocation() {

        Intent intent = new Intent(getActivity(), SurveyActivity.class);
        intent.putExtra(getString(R.string.currentLocationId), 0);
        startActivityForResult(intent, REQUEST_LOCATION);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_LOCATION) {
            if (resultCode == Activity.RESULT_OK) {
                long id = data.getLongExtra(getString(R.string.currentLocationId), -1);
                if (id > -1) {
                    displayLocation(id);
                }
            }
        }
    }

    private void displayLocation(long id) {

        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getActivity());

        PreferencesUtils.setLong(getActivity(), R.string.currentLocationId, id);

        if (mLocationDetailsFragment == null) {
            mLocationDetailsFragment = new LocationDetailsFragment();
        } else {
            //TODO: fix this
            try {
                mLocationDetailsFragment.setArguments(null);
            } catch (Exception e) {
                mLocationDetailsFragment = new LocationDetailsFragment();
            }
        }

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.executePendingTransactions();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        Bundle args = new Bundle();
        args.putLong(getString(R.string.currentTestId), id);

        mLocationDetailsFragment.setArguments(args);
        ft.replace(R.id.container, mLocationDetailsFragment, "mLocationDetailsFragment");
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack(null);
        ft.commit();
        fragmentManager.executePendingTransactions();

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        CursorWrapper content = (CursorWrapper) adapterView.getItemAtPosition(i);
        displayLocation(content.getLong(projection.length - 1));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_addLocation:
                displayAddLocation();
                return true;
            case R.id.menu_delete:
                AlertUtils
                        .askQuestion(getActivity(), R.string.delete, R.string.selectedWillBeDeleted,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        for (int j = 0; j < adapter.deleteList.size(); j++) {
                                            Uri uri = ContentUris.withAppendedId(
                                                    LocationContentProvider.CONTENT_URI,
                                                    ((CursorWrapper) adapter
                                                            .getItem(adapter.deleteList.valueAt(j)))
                                                            .getLong(adapter.getCursor()
                                                                    .getColumnIndex(
                                                                            LocationTable.COLUMN_ID))
                                            );

                                            String[] projection = {LocationTable.COLUMN_ID};

                                            Cursor cursor = getActivity().getContentResolver()
                                                    .query(uri, projection, null, null, null);
                                            cursor.moveToFirst();

                                            long folder = cursor.getLong(
                                                    cursor.getColumnIndex(LocationTable.COLUMN_ID));

                                            FileUtils.deleteFolder(getActivity(), folder, "");
                                            getActivity().getContentResolver()
                                                    .delete(uri, null, null);

                                            cursor.close();
                                        }

                                        adapter.deleteList.clear();
                                        adapter.showCheckBox = false;
                                        mMenu.setGroupVisible(R.id.group_1, true);
                                        mMenu.setGroupVisible(R.id.group_2, false);
                                        adapter.notifyDataSetChanged();
                                        adapter.notifyDataSetInvalidated();
                                        getListView().invalidate();
                                    }
                                }, null
                        );
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle(R.string.locations);

        getListView().setOnItemClickListener(this);

        fillData();

        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                adapter.showCheckBox = true;
                adapter.notifyDataSetChanged();
                mMenu.setGroupVisible(R.id.group_1, true);
                mMenu.setGroupVisible(R.id.group_2, true);
                return true;
            }
        });
    }

    private void fillData() {

        // Fields on the UI to which we map
        int[] to = new int[]{R.id.dateText, R.id.placeText, R.id.addressText, R.id.resultText};

        getLoaderManager().initLoader(0, null, this);
        adapter = new CheckboxSimpleCursorAdapter(getActivity(), R.layout.row_location, null,
                projection,
                to, 0);

        adapter.notifyDataSetInvalidated();

        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {

            public boolean setViewValue(View aView, Cursor aCursor, int aColumnIndex) {

                switch (aColumnIndex) {
                    case 0:
                        long date = aCursor.getLong(aColumnIndex);
                        //((TextView) aView).setText(getRelativeTimeSpanString(date, System.currentTimeMillis(), SECOND_IN_MILLIS));
                        ((TextView) aView).setText(TimeUtils.getTimeAgo(date, getActivity()));

                        return true;
                    case 1:
                    case 2:
                        String address = aCursor.getString(aColumnIndex) + " " + aCursor
                                .getString(aColumnIndex + 3);
                        ((TextView) aView).setText(address);
                        return true;

                    case 3:
                        String[] sourceArray = getResources().getStringArray(R.array.source_types);
                        int sourceType = aCursor.getInt(7);
                        if (sourceType > -1) {
                            ((TextView) aView).setText(sourceArray[sourceType]);
                        } else {
                            ((TextView) aView).setText("");
                        }
                        return true;
                    case 4:

                        if (showCheckbox) {
                            // CheckBox checkBox = (CheckBox) aView.findViewById(R.id.checkBox);
                            aView.setVisibility(View.VISIBLE);
                            aView.setTag(aCursor.getLong(0));
                        }
                        break;
                    default:
                        return true;
                }
                return true;
            }
        });
        setListAdapter(adapter);
    }

    // creates a new loader after the initLoader () call
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                LocationContentProvider.CONTENT_URI, projection, null, null,
                LocationTable.COLUMN_DATE + " desc");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
        //handler.sendEmptyMessage(0);

        adapter.notifyDataSetChanged();
        if (data != null) {
            data.moveToPosition(-1);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // data is not available anymore, delete reference
        adapter.swapCursor(null);
    }

    public boolean backPressHandled() {

        if (adapter.showCheckBox) {
            adapter.showCheckBox = false;
            adapter.notifyDataSetChanged();
            mMenu.setGroupVisible(R.id.group_1, true);
            mMenu.setGroupVisible(R.id.group_2, false);

            return true;
        }

        return false;
    }
}