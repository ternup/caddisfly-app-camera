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
import com.ternup.caddisfly.adapter.CheckboxSimpleCursorAdapter;
import com.ternup.caddisfly.database.TestTable;
import com.ternup.caddisfly.provider.TestContentProvider;
import com.ternup.caddisfly.util.DataHelper;
import com.ternup.caddisfly.util.PreferencesHelper;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;

public class ResultListFragment extends ListFragment implements AdapterView.OnItemClickListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private final boolean showCheckbox = false;

    //final private OnItemClickListener mItemClickListener;

    public ItemClicked parentFragment;

    long mLocationId;

    private CheckboxSimpleCursorAdapter adapter;

    public ResultListFragment() {
    }

    //public ResultListFragment(OnItemClickListener listener) {
    //mItemClickListener = listener;
    //}

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        CursorWrapper content = (CursorWrapper) adapterView.getItemAtPosition(i);
        String folderName = content.getString(4);
        long id = content.getLong(0);

        if (parentFragment != null && parentFragment instanceof ItemClicked) {
            parentFragment.resultItemClicked(folderName, id);
        }

        //mItemClickListener.onItemClick(folderName, id);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_result_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fillData();
        getListView().setOnItemClickListener(this);
    }

    private void fillData() {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                getActivity());

        mLocationId = sharedPreferences.getLong(PreferencesHelper.CURRENT_LOCATION_ID_KEY, -1);

        // Fields from the database (projection)
        // Must include the _id column for the adapter to work
        String[] from = new String[]{TestTable.COLUMN_DATE,
                TestTable.COLUMN_TYPE, TestTable.COLUMN_RESULT, TestTable.COLUMN_ID};
        // Fields on the UI to which we map
        int[] to = new int[]{R.id.dateText, R.id.typeText, R.id.resultText};

        getLoaderManager().initLoader(0, null, this);

        adapter = new CheckboxSimpleCursorAdapter(getActivity(), R.layout.row_result, null, from,
                to, 0);

        adapter.notifyDataSetInvalidated();

        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {

            public boolean setViewValue(View aView, final Cursor aCursor, int aColumnIndex) {
                switch (aColumnIndex) {
                    case 1:
                        long date = aCursor.getLong(aColumnIndex);
                        SimpleDateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy");
                        //DateFormat tf = android.text.format.DateFormat.getTimeFormat(getActivity()); // Gets system TF

                        String dateString = df.format(date);
                        ((TextView) aView).setText(dateString);

                        //((TextView) aView).setText(getRelativeTimeSpanString(date, System.currentTimeMillis(), SECOND_IN_MILLIS));
                        //((TextView) aView).setText(TimeUtil.getTimeAgo(date, getActivity()));

                        return true;
                    case 2:
                        TextView typeText = (TextView) aView;

                        String testType = DataHelper
                                .getTestTitle(getActivity(), aCursor.getInt(aColumnIndex));

                        typeText.setText(testType);

                        return true;
                    case 3:
                        TextView resultText = (TextView) aView;
                        double result = aCursor
                                .getDouble(aCursor.getColumnIndex(TestTable.COLUMN_RESULT));

                        if (result < 0) {
                            resultText.setText(
                                    String.format("%s", "0.0"));
                        } else {
                            resultText.setText(String.format("%.2f", result));
                        }
                        return true;
/*
                        String address = aCursor.getString(aColumnIndex);
                        if (address == null) {
                            ((TextView) aView).setText(R.string.addressPending);
                            return true;
                        }
*/
                    case 4:

                        if (showCheckbox) {
                            // CheckBox checkBox = (CheckBox) aView.findViewById(R.id.checkBox);
                            aView.setVisibility(View.VISIBLE);
                            aView.setTag(aCursor.getLong(0));
                        }
                    default:
                        return false;
                }
            }
        });

        setListAdapter(adapter);
        //mResultList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    // creates a new loader after the initLoader () call
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
/*
        if(adapter != null){
        adapter.swapCursor(null);
        }
*/
        String[] projection = {TestTable.TABLE_TEST + "." + TestTable.COLUMN_ID,
                TestTable.TABLE_TEST + "." + TestTable.COLUMN_DATE,
                TestTable.COLUMN_TYPE, TestTable.COLUMN_RESULT, TestTable.COLUMN_FOLDER};

        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getActivity());

        mLocationId = sharedPreferences.getLong(PreferencesHelper.CURRENT_LOCATION_ID_KEY, -1);

        String selection = TestTable.COLUMN_LOCATION_ID + "=?";
        String[] selectionArgs = {String.valueOf(mLocationId)};

        return new CursorLoader(getActivity(),
                TestContentProvider.CONTENT_URI, projection, selection, selectionArgs,
                TestTable.TABLE_TEST + "." + TestTable.COLUMN_DATE + " desc");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);

        //if (data.getCount() == 0) {
        //   ((TextView)getListView().getEmptyView()).setText( "No tests conducted at this location" );
        // }

        //data.moveToPosition(-1);
        if (data.getCount() > 0) {
            setListViewHeightBasedOnChildren(getListView());
        }
        //adapter.swapCursor(data);

        adapter.notifyDataSetChanged();
        data.moveToPosition(-1);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // data is not available anymore, delete reference
        adapter.swapCursor(null);
    }

    public interface ItemClicked {

        public void resultItemClicked(String folderName, long id);
    }

    public interface OnItemClickListener {

        public void onItemClick(String folder, long id);
    }

}
