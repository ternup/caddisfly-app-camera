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
import com.ternup.caddisfly.adapter.GalleryListAdapter;
import com.ternup.caddisfly.app.Globals;
import com.ternup.caddisfly.app.MainApp;
import com.ternup.caddisfly.util.AlertUtils;
import com.ternup.caddisfly.util.ColorUtils;
import com.ternup.caddisfly.util.FileUtils;
import com.ternup.caddisfly.util.PhotoHandler;
import com.ternup.caddisfly.util.PreferencesHelper;
import com.ternup.caddisfly.util.PreferencesUtils;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;

public class CalibrateItemFragment extends ListFragment {

    private final PhotoTakenHandler mPhotoTakenHandler = new PhotoTakenHandler(this);

    CameraFragment mCameraFragment;

    GalleryListAdapter mAdapter;

    private PowerManager.WakeLock wakeLock;

    private int mTestType = Globals.FLUORIDE_INDEX;

    private TextView mRgbText;

    private Button mValueButton;

    private Button mColorButton;

    private Button mStartButton;

    private TextView mErrorQualityText;

    public CalibrateItemFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static CalibrateItemFragment newInstance() {
        return new CalibrateItemFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gallery, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View header = getActivity().getLayoutInflater()
                .inflate(R.layout.fragment_calibrate_item, null, false);

        ListView listView = getListView();

        assert header != null;
        mRgbText = (TextView) header.findViewById(R.id.rgbText);
        mValueButton = (Button) header.findViewById(R.id.valueButton);
        Button resetButton = (Button) header.findViewById(R.id.resetButton);
        mStartButton = (Button) header.findViewById(R.id.startButton);
        mColorButton = (Button) header.findViewById(R.id.colorButton);
        mErrorQualityText = (TextView) header.findViewById(R.id.errorQualityText);

        final int position = getArguments().getInt(getString(R.string.swatchIndex));
        //final int index = position * INDEX_INCREMENT_STEP;

        mTestType = getArguments().getInt(getString(R.string.currentTestTypeId));

        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                AlertUtils.askQuestion(getActivity(), R.string.calibrate,
                        R.string.calibrate_info,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(
                                    DialogInterface dialogInterface,
                                    int i) {
                                PreferencesUtils
                                        .setInt(getActivity(), R.string.currentSamplingCountKey, 0);
                                deleteCalibration(position);
                                if (wakeLock == null || !wakeLock.isHeld()) {
                                    PowerManager pm = (PowerManager) getActivity()
                                            .getApplicationContext()
                                            .getSystemService(Context.POWER_SERVICE);
                                    wakeLock = pm
                                            .newWakeLock(PowerManager.FULL_WAKE_LOCK
                                                    | PowerManager.ACQUIRE_CAUSES_WAKEUP
                                                    | PowerManager.ON_AFTER_RELEASE, "MyWakeLock");
                                    wakeLock.acquire();
                                }
                                startCalibration(position);
                            }
                        }, null
                );
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                editCalibration(position);

            }
        });

        String folderName = FileUtils.getStoragePath(getActivity(), -1,
                String.format("%s/%d/%d/small/", Globals.CALIBRATE_FOLDER, mTestType, position),
                false);

        ArrayList<String> files = FileUtils
                .getFilePaths(getActivity(), folderName, "", -1);

/*
        Shader textShader = new LinearGradient(0, 0, 0, mTitleView.getPaint().getTextSize(),
                new int[]{getResources().getColor(R.color.textGradientStart),
                        getResources().getColor(R.color.textGradientEnd)},
                new float[]{0, 1}, Shader.TileMode.CLAMP
        );
        mTitleView.getPaint().setShader(textShader);
*/

        Collections.sort(files);

        setListAdapter(null);

        assert listView != null;
        listView.addHeaderView(header);

        mAdapter = new GalleryListAdapter(getActivity(), mTestType, position, files, false);
        this.setListAdapter(mAdapter);
    }

    private void deleteCalibration(int position) {
        File file = new File(
                FileUtils.getStoragePath(getActivity(), -1,
                        String.format("%s/%d/%d/", Globals.CALIBRATE_FOLDER, mTestType,
                                position),
                        false
                )
        );

        FileUtils.deleteFolder(getActivity(), -1, file.getAbsolutePath());

    }

    public void editCalibration(final int position) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_PHONE);

        alertDialogBuilder.setView(input);
        alertDialogBuilder.setCancelable(true);
        alertDialogBuilder.setTitle(R.string.enterColorRgb);
        alertDialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                closeKeyboard(input);
                dialog.cancel();
                saveRgb(input.getText().toString(), position);
            }
        });
        alertDialogBuilder
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        closeKeyboard(input);
                        dialog.cancel();
                    }
                });
        final AlertDialog alertDialog = alertDialogBuilder.create(); //create the box

        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId
                        == EditorInfo.IME_ACTION_DONE)) {

                    closeKeyboard(input);
                    alertDialog.cancel();

                    saveRgb(input.getText().toString(), position);
                }
                return false;
            }
        });

        alertDialog.show();
        input.requestFocus();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);


    }

    public void closeKeyboard(EditText input) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(input.getWindowToken(), 0);

    }

    public void saveRgb(String value, int position) {

        try {
            String[] rgbArray = value.split(" ");
            if (rgbArray.length < 3) {
                rgbArray = value.split("-");
            }
            if (rgbArray.length < 3) {
                rgbArray = value.split("\\.");
            }

            if (rgbArray.length < 3 && value.length() > 8) {
                rgbArray = new String[3];
                rgbArray[0] = value.substring(0, 3);
                rgbArray[1] = value.substring(3, 6);
                rgbArray[2] = value.substring(6, 9);
            }

            if (rgbArray.length > 2) {
                int r = Integer.parseInt(rgbArray[0]);
                int g = Integer.parseInt(rgbArray[1]);
                int b = Integer.parseInt(rgbArray[2]);
                storeCalibratedData(position, Color.rgb(r, g, b), 100);

                String folderName = FileUtils.getStoragePath(getActivity(), -1,
                        String.format("%s/%d/%d/", Globals.CALIBRATE_FOLDER, mTestType, position),
                        false);

                FileUtils.deleteFolder(getActivity(), -1, folderName);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        displayInfo(false);

    }

    void displayInfo(boolean animate) {

        final MainApp mainApp = ((MainApp) getActivity().getApplicationContext());

        final int position = getArguments().getInt(getString(R.string.swatchIndex));
        final int index = position * mainApp.rangeIncrementStep;
        ArrayList<Integer> colorRange = mainApp.colorList;

        int color = PreferencesUtils.getInt(mainApp,
                String.format("%s-%s", String.valueOf(mTestType), String.valueOf(index)),
                -1);

        mRgbText.setText(String.format("%s: %s  %s  %s", mainApp.getString(R.string.rgb),
                String.format("%d", Color.red(color)),
                String.format("%d", Color.green(color)),
                String.format("%d", Color.blue(color))
        ));

        int accuracy = Math.max(0, PreferencesUtils.getInt(mainApp,
                String.format("%s-a-%s", String.valueOf(mTestType), String.valueOf(index)),
                101));

        //check if calibration is factory preset or set by user
        if (accuracy < 101) {

            int samplingCount = PreferencesUtils
                    .getInt(getActivity(), R.string.samplingCountKey, 1);
            String photoFile = String.format("%s-%d", Globals.PHOTO_TEMP_FILE, 1);
            File file = new File(
                    FileUtils.getStoragePath(getActivity(), -1,
                            String.format("%s/%d/%d/", Globals.CALIBRATE_FOLDER, mTestType,
                                    position),
                            false
                    ) + photoFile
            );

        } else {
            accuracy = -1;
        }

        if (color == -1) {
            color = colorRange.get(index);
        }

        int minAccuracy = PreferencesUtils.getInt(mainApp, R.string.minPhotoQualityKey, 0);

        if (accuracy < minAccuracy && accuracy > -1) {
            mErrorQualityText.setVisibility(View.VISIBLE);
            mColorButton.setText(getString(R.string.error));
        } else {
            mErrorQualityText.setVisibility(View.GONE);
            mColorButton.setText("");
        }

        if (accuracy == -1) {
            mColorButton.setText(getActivity().getString(R.string.notCalibrated));
            color = Color.WHITE;
            mRgbText.setVisibility(View.GONE);
        } else {
            mRgbText.setVisibility(View.VISIBLE);
        }

        mColorButton.setBackgroundColor(color);

        mValueButton.setText(mainApp.doubleFormat
                .format((position + mainApp.rangeStartIncrement) * (mainApp.rangeIncrementStep
                        * mainApp.rangeIncrementValue)));

        mStartButton.setEnabled(true);
    }

    /**
     * Calibrate by taking a photo and using its color for the chosen index
     *
     * @param index The index of the value to be calibrated
     */
    void startCalibration(final int index) {
        (new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                PhotoHandler photoHandler = new PhotoHandler(
                        getActivity().getApplicationContext(), mPhotoTakenHandler, index, "",
                        mTestType);
                //camera.takePicture(null, null, photoHandler);

                FragmentTransaction ft = getFragmentManager().beginTransaction();
                Fragment prev = getFragmentManager().findFragmentByTag("cameraDialog");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);

                mCameraFragment = CameraFragment.newInstance();
                mCameraFragment.pictureCallback = photoHandler;
                //mCameraFragment.makeShutterSound = true;
                mCameraFragment.show(ft, "cameraDialog");

            }
        }).execute();

    }

    void storeCalibratedData(final int position, final int resultColor, final int accuracy) {

        (new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                Context context = getActivity().getApplicationContext();

                if (context != null) {
                    MainApp mainApp = ((MainApp) context.getApplicationContext());
                    ArrayList<Integer> colorList = ((MainApp) context).colorList;
                    int index = position * mainApp.rangeIncrementStep;

                    colorList.set(index, resultColor);

                    SharedPreferences sharedPreferences = PreferenceManager
                            .getDefaultSharedPreferences(context);
                    SharedPreferences.Editor editor = sharedPreferences.edit();

                    editor.putInt(String.format("%d-%s", mTestType, String.valueOf(index)),
                            resultColor);
                    editor.putInt(String.format("%d-a-%s", mTestType, String.valueOf(index)),
                            accuracy);

                    autoGenerateColors(index, colorList.get(index), colorList,
                            mainApp.rangeIncrementStep, editor);
                    editor.commit();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                // TODO Auto-generated method stub
                super.onPostExecute(result);
                String folderName = FileUtils.getStoragePath(getActivity(), -1,
                        String.format("%s/%d/%d/small/", Globals.CALIBRATE_FOLDER, mTestType,
                                position),
                        false
                );

                ArrayList<String> files = FileUtils
                        .getFilePaths(getActivity(), folderName, "", -1);
                mAdapter = new GalleryListAdapter(getActivity(), mTestType, position, files, false);
                setListAdapter(mAdapter);

                //mAdapter.notifyDataSetChanged();
                displayInfo(true);
            }
        }).execute();
    }

    void autoGenerateColors(int index, int startColor,
            ArrayList<Integer> colorList, int incrementStep,
            SharedPreferences.Editor editor) {

        if (index < 30) {
            for (int i = 1; i < incrementStep; i++) {
                int nextColor = ColorUtils.getGradientColor(startColor,
                        colorList.get(index + incrementStep), incrementStep,
                        i);
                colorList.set(index + i, nextColor);

                editor.putInt(String.format("%d-%s", mTestType, String.valueOf(index + i)),
                        nextColor);
            }
        }

        if (index > 0) {
            for (int i = 1; i < incrementStep; i++) {
                int nextColor = ColorUtils.getGradientColor(startColor,
                        colorList.get(index - incrementStep), incrementStep,
                        i);
                colorList.set(index - i, nextColor);
                editor.putInt(String.format("%d-%s", mTestType, String.valueOf(index - i)),
                        nextColor);
            }
        }
    }

    private boolean hasSamplingCompleted() {
        Context context = getActivity();
        int currentSamplingCount = PreferencesUtils
                .getInt(context, R.string.currentSamplingCountKey, 0);
        return currentSamplingCount >= PreferencesUtils
                .getInt(context, R.string.samplingCountKey, 5);
    }

    private void releaseResources() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        releaseResources();
    }

    private static class PhotoTakenHandler extends Handler {

        private final WeakReference<CalibrateItemFragment> mAdapter;

        public PhotoTakenHandler(CalibrateItemFragment adapter) {
            mAdapter = new WeakReference<CalibrateItemFragment>(adapter);
        }

        @Override
        public void handleMessage(Message msg) {
            CalibrateItemFragment adapter = mAdapter.get();
            PreferencesHelper.incrementPhotoTakenCount(adapter.getActivity());

            //if (adapter.mCameraFragment != null) {
            adapter.mCameraFragment.dismiss();
            //}

            if (adapter != null) {
                if (!adapter.hasSamplingCompleted()) {
                    adapter.startCalibration(msg.getData().getInt("position"));
                } else {
                    adapter.storeCalibratedData(msg.getData().getInt("position"),
                            msg.getData().getInt(MainApp.RESULT_COLOR_KEY),
                            msg.getData().getInt(MainApp.QUALITY_KEY));
                    adapter.releaseResources();
                }
            }
        }
    }
}
