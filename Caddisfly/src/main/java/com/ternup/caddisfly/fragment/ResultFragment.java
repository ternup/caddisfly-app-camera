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
import com.ternup.caddisfly.app.Globals;
import com.ternup.caddisfly.app.MainApp;
import com.ternup.caddisfly.database.LocationTable;
import com.ternup.caddisfly.database.TestTable;
import com.ternup.caddisfly.provider.TestContentProvider;
import com.ternup.caddisfly.util.AlertUtils;
import com.ternup.caddisfly.util.DataHelper;
import com.ternup.caddisfly.util.DateUtils;
import com.ternup.caddisfly.util.FileUtils;
import com.ternup.caddisfly.util.PreferencesHelper;
import com.ternup.caddisfly.util.WebClient;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
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

public class ResultFragment extends Fragment {

    private String folderName;

    private String mTestType;

    private long mTestTypeId;

    private Context mContext;

    private long mId;

    private TextView mDateView;

    private TextView mResultTextView;

    //private ImageView mResultIcon;

    private TextView mAddressText;

    private TextView mAddress2Text;

    private TextView mAddress3Text;

    private TextView mSourceText;

    private TextView mPpmText;

    private DetailsFragment detailsFragment;

    private long mLocationId = -1;

    private ProgressDialog progressDialog = null;

    private PowerManager.WakeLock wakeLock;

    private int count = 0;

    private int totalCount = 0;

    private TextView mTitleView;

    public ResultFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_result, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle(R.string.result);

        mContext = getActivity();
        mPpmText = (TextView) view.findViewById(R.id.ppmText);
        mDateView = (TextView) view.findViewById(R.id.testDate);
        mTitleView = (TextView) view.findViewById(R.id.titleView);
        mResultTextView = (TextView) view.findViewById(R.id.result);
        //mResultIcon = (ImageView) view.findViewById(R.id.resultIcon);

        mAddressText = (TextView) view.findViewById(R.id.address1);
        mAddress2Text = (TextView) view.findViewById(R.id.address2);
        mAddress3Text = (TextView) view.findViewById(R.id.address3);
        mSourceText = (TextView) view.findViewById(R.id.sourceType);

        ListView drawerList = (ListView) getActivity().findViewById(R.id.navigation_drawer);
        drawerList.setItemChecked(-1, true);
        drawerList.setSelection(-1);

        folderName = getArguments().getString(PreferencesHelper.FOLDER_NAME_KEY);
        mId = getArguments().getLong(getString(R.string.currentTestId));

        final SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(mContext);

        mLocationId = sharedPreferences.getLong(getString(R.string.currentLocationId), -1);


        /*Button deleteButton = (Button) view.findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertUtils.askQuestion(getActivity(), R.string.delete, R.string.areYouSure,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                FileUtils.deleteFolder(getActivity(), mLocationId, folderName);

                                Uri uri = ContentUris
                                        .withAppendedId(TestContentProvider.CONTENT_URI, mId);
                                mContext.getContentResolver().delete(uri, null, null);

                                int value = 0;
                                int counter = 0;
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                while (value != -1) {
                                    value = sharedPreferences
                                            .getInt(String.format("result_%d_%d", mId, counter),
                                                    -1);
                                    if (value > -1) {
                                        editor.remove(String.format("result_%d_%d", mId, counter));
                                        editor.commit();
                                        counter++;
                                    }
                                }
                                goBack();
                            }

                        }, null
                );
            }
        });

        Button sendButton = (Button) view.findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (NetworkUtils.checkInternetConnection(mContext)) {
                    if (progressDialog == null) {
                        progressDialog = new ProgressDialog(getActivity());
                        progressDialog.setMessage("Sending...");
                        progressDialog.setCancelable(false);
                    }
                    progressDialog.show();
                    postResult("testresults");
                }
            }
        });
*/
        ArrayList<String> filePaths = FileUtils
                .getFilePaths(getActivity(), folderName, mLocationId);

        File directory = new File(
                FileUtils.getStoragePath(getActivity(), mLocationId, folderName, false));
        if (!directory.exists()) {
            Uri uri = ContentUris.withAppendedId(TestContentProvider.CONTENT_URI, mId);
            mContext.getContentResolver().delete(uri, null, null);

            goBack();

        } else if (filePaths.size() > 0) {

            displayResult();

        } else {
            FileUtils.deleteFolder(getActivity(), mLocationId, folderName);
        }
    }

    public JSONObject getJson() {
        JSONObject object = new JSONObject();

        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        df.setTimeZone(tz);
        String nowAsISO = df.format(new Date());

        try {
            object.put("date", nowAsISO);
            object.put("test", Integer.valueOf(1));
            object.put("value", Double.valueOf(12.334232));
            object.put("method", Integer.valueOf(1));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
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

    public void postResult(final String url) {

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
                    Throwable error) {
                Log.d(Globals.DEBUG_TAG, "fail: " + error.getMessage());
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                        }

                        if (wakeLock != null && wakeLock.isHeld()) {
                            wakeLock.release();
                        }
                    }
                });


            }
        });
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
                fragmentManager.executePendingTransactions();
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
    public void onStop() {
        super.onStop();
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
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

        mAddressText
                .setText(cursor.getString(cursor.getColumnIndex(LocationTable.COLUMN_NAME)) + ", " +
                        cursor.getString(cursor.getColumnIndex(LocationTable.COLUMN_STREET)));

        mAddress2Text
                .setText(cursor.getString(cursor.getColumnIndex(LocationTable.COLUMN_TOWN)) + ", " +
                        cursor.getString(cursor.getColumnIndex(LocationTable.COLUMN_CITY)));

        mAddress3Text
                .setText(
                        cursor.getString(cursor.getColumnIndex(LocationTable.COLUMN_STATE)) + ", " +
                                cursor.getString(
                                        cursor.getColumnIndex(LocationTable.COLUMN_COUNTRY)));

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

        cursor.close();
    }


}