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

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.ternup.caddisfly.R;
import com.ternup.caddisfly.adapter.GalleryListAdapter;
import com.ternup.caddisfly.app.Globals;
import com.ternup.caddisfly.app.MainApp;
import com.ternup.caddisfly.database.LocationTable;
import com.ternup.caddisfly.database.TestTable;
import com.ternup.caddisfly.provider.TestContentProvider;
import com.ternup.caddisfly.util.AlertUtils;
import com.ternup.caddisfly.util.DataHelper;
import com.ternup.caddisfly.util.DateUtils;
import com.ternup.caddisfly.util.FileUtils;
import com.ternup.caddisfly.util.NetworkUtils;
import com.ternup.caddisfly.util.PreferencesHelper;
import com.ternup.caddisfly.util.PreferencesUtils;
import com.ternup.caddisfly.util.WebClient;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class DetailsFragment extends ListFragment {

    private String mTestType;

    private int mTestTypeId;

    private long mLocationId = -1;

    private Context mContext;

    private long mId;

    private TextView mDateView;

    private TextView mResultTextView;

    private TextView mAddressText;

    private TextView mAddress2Text;

    private TextView mAddress3Text;

    private TextView mSourceText;

    private TextView mPpmText;

    private TextView mTitleView;

    private String mFolderName;

    private ProgressDialog progressDialog = null;

    private PowerManager.WakeLock wakeLock;

    private int count = 0;

    private int totalCount = 0;

    public DetailsFragment() {
        //to prevent menu loading more than once
        setRetainInstance(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gallery, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.result, menu);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle(R.string.details);
        mContext = getActivity();

        ListView listView = getListView();

        mFolderName = getArguments().getString(PreferencesHelper.FOLDER_NAME_KEY);

        View header = getActivity().getLayoutInflater()
                .inflate(R.layout.fragment_result, null, false);

        mPpmText = (TextView) header.findViewById(R.id.ppmText);
        mDateView = (TextView) header.findViewById(R.id.testDate);
        mTitleView = (TextView) header.findViewById(R.id.titleView);
        mResultTextView = (TextView) header.findViewById(R.id.result);
        mAddressText = (TextView) header.findViewById(R.id.address1);
        mAddress2Text = (TextView) header.findViewById(R.id.address2);
        mAddress3Text = (TextView) header.findViewById(R.id.address3);
        mSourceText = (TextView) header.findViewById(R.id.sourceType);

        final SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(mContext);

        mFolderName = getArguments().getString(PreferencesHelper.FOLDER_NAME_KEY);
        mId = getArguments().getLong(getString(R.string.currentTestId));
        mLocationId = sharedPreferences.getLong(getString(R.string.currentLocationId), -1);

        File directory = new File(
                FileUtils.getStoragePath(getActivity(), mLocationId, mFolderName, false));

        long locationId = sharedPreferences.getLong(getString(R.string.currentLocationId), -1);

        final ArrayList<String> imagePaths = FileUtils
                .getFilePaths(getActivity(), mFolderName, "/small/", locationId);

        if (mId > -1) {
            if (!directory.exists()) {
                Uri uri = ContentUris.withAppendedId(TestContentProvider.CONTENT_URI, mId);
                mContext.getContentResolver().delete(uri, null, null);
                goBack();
            } else if (imagePaths.size() > 0) {
                displayResult();
            } else {
                FileUtils.deleteFolder(getActivity(), mLocationId, mFolderName);
                goBack();
            }
        }

        ListView drawerList = (ListView) getActivity().findViewById(R.id.navigation_drawer);
        drawerList.setItemChecked(-1, true);
        drawerList.setSelection(-1);

        assert listView != null;
        listView.addHeaderView(header);

        // Gradient shading for title
        assert header != null;

        //Collections.sort(imagePaths);

        GalleryListAdapter adapter = new GalleryListAdapter(getActivity(), mTestTypeId, mId,
                imagePaths, true);
        setListAdapter(adapter);

        Shader textShader = new LinearGradient(0, 0, 0, mTitleView.getPaint().getTextSize(),
                new int[]{getResources().getColor(R.color.textGradientStart),
                        getResources().getColor(R.color.textGradientEnd)},
                new float[]{0, 1}, Shader.TileMode.CLAMP
        );
        mTitleView.getPaint().setShader(textShader);

    }

    private void goBack() {
        FragmentManager fm = getFragmentManager();
        try {
            if (fm.getBackStackEntryCount() > 0) {
                fm.popBackStack();
                fm.executePendingTransactions();
            } else {
                Fragment fragment = new HomeFragment();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction ft = fragmentManager.beginTransaction();
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.replace(R.id.container, fragment, "0");
                ft.addToBackStack(null);
                ft.commit();
                fm.executePendingTransactions();
                ListView drawerList = (ListView) getActivity().findViewById(R.id.navigation_drawer);
                drawerList.setItemChecked(0, true);
                drawerList.setSelection(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_sendResult:
                if (NetworkUtils.checkInternetConnection(mContext)) {
                    if (progressDialog == null) {
                        progressDialog = new ProgressDialog(getActivity());
                        progressDialog.setMessage(getString(R.string.sending));
                        progressDialog.setCancelable(false);
                    }
                    progressDialog.show();
                    postResult(mFolderName);
                }
                return true;
            case R.id.menu_delete:
                AlertUtils.askQuestion(getActivity(), R.string.delete, R.string.selectedWillBeDeleted,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                FileUtils.deleteFolder(getActivity(), mLocationId, mFolderName);

                                Uri uri = ContentUris
                                        .withAppendedId(TestContentProvider.CONTENT_URI, mId);
                                mContext.getContentResolver().delete(uri, null, null);

                                double value = 0;
                                int counter = 0;
                                while (value != -1) {
                                    String key = String.format(getString(R.string.resultValueKey),
                                            mTestTypeId, mId, counter);
                                    if (PreferencesUtils.contains(mContext, key)) {
                                        PreferencesUtils.removeKey(mContext, key);
                                    } else {
                                        value = -1;
                                    }
                                    counter++;
                                }
                                goBack();
                            }

                        }, null
                );
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void displayResult() {

        String[] projection = {TestTable.TABLE_TEST + "." + TestTable.COLUMN_ID,
                TestTable.TABLE_TEST + "." + TestTable.COLUMN_DATE,
                TestTable.COLUMN_RESULT,
                TestTable.COLUMN_TYPE,
                TestTable.COLUMN_FOLDER,
                LocationTable.COLUMN_NAME,
                LocationTable.COLUMN_STREET,
                LocationTable.COLUMN_TOWN,
                LocationTable.COLUMN_CITY,
                LocationTable.COLUMN_STATE,
                LocationTable.COLUMN_COUNTRY,
                LocationTable.COLUMN_STREET,
                LocationTable.COLUMN_SOURCE
        };

        Log.d("Result", mId + " test");

        Uri uri = ContentUris.withAppendedId(TestContentProvider.CONTENT_URI, mId);
        Cursor cursor = mContext.getContentResolver().query(uri, projection, null, null, null);
        cursor.moveToFirst();

        if (cursor.getCount() > 0) {
            mAddressText
                    .setText(cursor.getString(cursor.getColumnIndex(LocationTable.COLUMN_NAME))
                            + ", " +
                            cursor.getString(cursor.getColumnIndex(LocationTable.COLUMN_STREET)));

            mAddress2Text
                    .setText(cursor.getString(cursor.getColumnIndex(LocationTable.COLUMN_TOWN))
                            + ", " +
                            cursor.getString(cursor.getColumnIndex(LocationTable.COLUMN_CITY)));

            mAddress3Text
                    .setText(
                            cursor.getString(cursor.getColumnIndex(LocationTable.COLUMN_STATE))
                                    + ", " +
                                    cursor.getString(
                                            cursor.getColumnIndex(LocationTable.COLUMN_COUNTRY))
                    );

            if (mAddress2Text.getText().equals(", ")) {
                mAddress2Text.setVisibility(View.GONE);
            } else {
                mAddress2Text.setVisibility(View.VISIBLE);
            }
            if (mAddress3Text.getText().equals(", ")) {
                mAddress3Text.setVisibility(View.GONE);
            } else {
                mAddress3Text.setVisibility(View.VISIBLE);
            }
            String[] sourceArray = getResources().getStringArray(R.array.source_types);
            int sourceType = cursor.getInt(cursor.getColumnIndex(LocationTable.COLUMN_SOURCE));
            if (sourceType > -1) {
                mSourceText.setText(sourceArray[sourceType]);
                mSourceText.setVisibility(View.VISIBLE);
            } else {
                mSourceText.setVisibility(View.GONE);
            }
            Date date = new Date(cursor.getLong(cursor.getColumnIndex(TestTable.COLUMN_DATE)));
            SimpleDateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy");
            DateFormat tf = android.text.format.DateFormat
                    .getTimeFormat(getActivity()); // Gets system TF

            String dateString = df.format(date.getTime()) + ", " + tf.format(date.getTime());

            mTestType = DataHelper.getTestTitle(getActivity(),
                    cursor.getInt(cursor.getColumnIndex(TestTable.COLUMN_TYPE)));
            mTestTypeId = cursor.getInt(cursor.getColumnIndex(TestTable.COLUMN_TYPE));

            mTitleView.setText(mTestType);
            mDateView.setText(dateString);

            Double resultPpm = cursor.getDouble(cursor.getColumnIndex(TestTable.COLUMN_RESULT));

            if (mTestTypeId == Globals.PH_INDEX) {
                mPpmText.setText("");
            } else {
                mPpmText.setText(R.string.ppm);
            }

            if (resultPpm < 0) {
                mResultTextView.setText("0.0");
                //mResultIcon.setVisibility(View.GONE);
                mPpmText.setVisibility(View.GONE);
            } else {
                mResultTextView.setText(String.format("%.2f", resultPpm));

                Context context = getActivity().getApplicationContext();

                int resourceAttribute;

                if (resultPpm <= Globals.FLUORIDE_MAX_DRINK) {
                    resourceAttribute = R.attr.drink;
                } else if (resultPpm <= Globals.FLUORIDE_MAX_COOK) {
                    resourceAttribute = R.attr.cook;
                } else if (resultPpm <= Globals.FLUORIDE_MAX_BATHE) {
                    resourceAttribute = R.attr.bath;
                } else {
                    resourceAttribute = R.attr.wash;
                }

                TypedArray a = context.getTheme()
                        .obtainStyledAttributes(((MainApp) context.getApplicationContext())
                                .CurrentTheme, new int[]{resourceAttribute});
                int attributeResourceId = a.getResourceId(0, 0);
                //mResultIcon.setImageResource(attributeResourceId);

                //mResultIcon.setVisibility(View.VISIBLE);
                mPpmText.setVisibility(View.VISIBLE);
            }
        }
        cursor.close();
    }


    public void postItem(final int newId, final ArrayList<String> filePaths) {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        df.setTimeZone(tz);

        File myFile = new File(filePaths.get(count));
        if (myFile.getName().contains("-s")) {
            if (myFile.exists()) {
                try {
                    RequestParams params = new RequestParams();
                    String date = df.format(DateUtils.getDateFromFilename(myFile.getName()));
                    params.put("date", date);
                    params.put("test", String.valueOf(newId));
                    params.put("image", myFile);

                    WebClient.post("testresults", params, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers,
                                byte[] responseBody) {
                            count++;
                            if (count >= totalCount) {
                                getActivity().runOnUiThread(new Runnable() {
                                    public void run() {
                                        if (progressDialog != null) {
                                            progressDialog.dismiss();
                                        }

                                        if (wakeLock != null && wakeLock.isHeld()) {
                                            wakeLock.release();
                                        }

                                        AlertUtils.showMessage(getActivity(),
                                                R.string.success, R.string.result_sent);
                                    }
                                });
                            } else {
                                postItem(newId, filePaths);
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers,
                                byte[] responseBody, Throwable error) {
                            Log.d(Globals.DEBUG_TAG, "fail: " + error.getMessage());
                            getActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    if (progressDialog != null) {
                                        progressDialog.dismiss();
                                    }
                                    if (wakeLock != null && wakeLock.isHeld()) {
                                        wakeLock.release();
                                    }

                                    AlertUtils.showMessage(getActivity(),
                                            R.string.error, R.string.send_failed);

                                    //showInternetAlert();
                                }
                            });
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void postResult(String folderName) {

        RequestParams params = new RequestParams();
        TimeZone tz = TimeZone.getTimeZone("UTC");
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        df.setTimeZone(tz);

        final ArrayList<String> filePaths = FileUtils
                .getFilePaths(getActivity(), folderName, "/small/", mLocationId);
        File myFile = new File(filePaths.get(0));
        String date = df.format(DateUtils.getDateFromFilename(myFile.getName()));

        params.put("date", date);
        String deviceId = Build.MANUFACTURER + " " + Build.MODEL;
        if (deviceId.length() > 32) {
            deviceId = deviceId.substring(1, 32);
        }
        params.put("deviceId", deviceId);
        params.put("type", String.valueOf(mTestTypeId + 1));

        if (wakeLock == null || !wakeLock.isHeld()) {
            PowerManager pm = (PowerManager) getActivity().getApplicationContext()
                    .getSystemService(Context.POWER_SERVICE);
            wakeLock = pm
                    .newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP
                            | PowerManager.ON_AFTER_RELEASE, "MyWakeLock");
            wakeLock.acquire();
        }

        WebClient.post("tests", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = responseBody == null ? null : new String(responseBody);
                try {
                    JSONObject json = new JSONObject(response);
                    final int newId = json.getInt("id");
                    if (filePaths.size() > 0) {
                        count = 0;
                        totalCount = filePaths.size();
                        postItem(newId, filePaths);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody,
                    final Throwable error) {
                Log.d(Globals.DEBUG_TAG, "fail: " + error.getMessage());

                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                        }

                        if (wakeLock != null && wakeLock.isHeld()) {
                            wakeLock.release();
                        }
                        AlertUtils
                                .showAlert(getActivity(), R.string.error, error.getMessage(), null,
                                        null);
                    }
                });
            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

        //to prevent error: 'Cannot add header view to list — setAdapter has already been called.'
        setListAdapter(null);
    }
}
