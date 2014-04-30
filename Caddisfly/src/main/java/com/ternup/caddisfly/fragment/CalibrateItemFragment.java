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
import com.ternup.caddisfly.app.Globals;
import com.ternup.caddisfly.app.MainApp;
import com.ternup.caddisfly.component.SpeedometerView;
import com.ternup.caddisfly.util.AlertUtils;
import com.ternup.caddisfly.util.CameraUtils;
import com.ternup.caddisfly.util.ColorUtils;
import com.ternup.caddisfly.util.FileUtils;
import com.ternup.caddisfly.util.ImageUtils;
import com.ternup.caddisfly.util.PhotoHandler;
import com.ternup.caddisfly.util.PreferencesUtils;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class CalibrateItemFragment extends Fragment {

    private final PhotoTakenHandler mPhotoTakenHandler = new PhotoTakenHandler(this);

    ImageView mPhotoImageView;

    private int mTestType = 0;

    private SpeedometerView speedometer;

    private ProgressDialog progressDialog;

    private Camera camera;

    private Button mValueButton;

    private Button mColorButton;

    private Button mStartButton;

    private TextView mQualityTextView;

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
        final View rootView = inflater.inflate(R.layout.fragment_calibrate_item, container, false);

        assert rootView != null;
        mValueButton = (Button) rootView.findViewById(R.id.valueButton);
        Button resetButton = (Button) rootView.findViewById(R.id.resetButton);
        mStartButton = (Button) rootView.findViewById(R.id.startButton);
        Button analyzeButton = (Button) rootView.findViewById(R.id.analyzeButton);
        mColorButton = (Button) rootView.findViewById(R.id.colorButton);
        mPhotoImageView = (ImageView) rootView.findViewById(R.id.photoImageView);
        mErrorQualityText = (TextView) rootView.findViewById(R.id.errorQualityText);
        mQualityTextView = (TextView) rootView.findViewById(R.id.qualityTextView);

        // Customize SpeedometerView
        speedometer = (SpeedometerView) rootView.findViewById(R.id.speedometer);

        // Add label converter
        speedometer.setLabelConverter(new SpeedometerView.LabelConverter() {
            @Override
            public String getLabelFor(double progress, double maxProgress) {
                return String.valueOf((int) Math.round(progress));
            }
        });

        // configure value range and ticks
        speedometer.setMaxSpeed(100);
        speedometer.setMajorTickStep(10);
        speedometer.setMinorTicks(1);

        Context context = getActivity();
        int minAccuracy = PreferencesUtils.getInt(context, R.string.minPhotoQuality, 0);

        int mid = minAccuracy / 2;

        // Configure value range colors
        speedometer.addColoredRange(0, mid, Color.RED);
        speedometer.addColoredRange(mid, minAccuracy, Color.rgb(255, 92, 33));
        speedometer.addColoredRange(minAccuracy, 100, Color.GREEN);

        //final ArrayList<String> colorNames = context.colorNames;
//            if (colorNames.get(position * INDEX_INCREMENT_STEP).equals(getContext().getString(R.string.userCalibrated)))

        final int position = getArguments().getInt(getString(R.string.swatchIndex));
        //final int index = position * INDEX_INCREMENT_STEP;

        mTestType = getArguments().getInt(getString(R.string.currentTestTypeId));

        displayInfo(false);

        analyzeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                final MainApp mainApp = ((MainApp) getActivity().getApplicationContext());
                ArrayList<Integer> colorRange = mainApp.colorList;
                String fileName = String.format("%s/%s-%d-%d",
                        FileUtils
                                .getStoragePath(getActivity(), -1, Globals.CALIBRATE_FOLDER, false),
                        Globals.PHOTO_TEMP_FILE, mTestType, position
                );

                Bundle bundle;
                bundle = ColorUtils.getPpmValue(fileName, colorRange,
                        mainApp.rangeIncrementStep,
                        mainApp.rangeStartIncrement);

                storeCalibratedData(position,
                        bundle.getInt("resultColor"), bundle.getInt("accuracy"));

            }
        });

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
                                //resetCalibration(getActivity(), position);
                                startCalibration(position);
                                v.post(new Runnable() {
                                    public void run() {
                                        //notifyDataSetChanged();
                                    }
                                });
                            }
                        }, null
                );
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                AlertUtils.askQuestion(getActivity(), R.string.reset,
                        R.string.areYouSure,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(
                                    DialogInterface dialogInterface,
                                    int i) {
                                resetCalibration(getActivity(), position);
                                v.post(new Runnable() {
                                    public void run() {
                                        //notifyDataSetChanged();
                                    }
                                });
                            }
                        }, null
                );
            }
        });

        return rootView;
    }

    void displayInfo(boolean animate) {
        final MainApp mainApp = ((MainApp) getActivity().getApplicationContext());

        final int position = getArguments().getInt(getString(R.string.swatchIndex));
        final int index = position * mainApp.rangeIncrementStep;
        ArrayList<Integer> colorRange = mainApp.colorList;

        int color = PreferencesUtils.getInt(mainApp,
                String.format("%s-%s", String.valueOf(mTestType), String.valueOf(index)),
                -1);

        int accuracy = Math.max(0, PreferencesUtils.getInt(mainApp,
                String.format("%s-a-%s", String.valueOf(mTestType), String.valueOf(index)),
                101));

        //check if calibration is factory preset or set by user
        if (accuracy < 101) {
            File file = new File(
                    String.format("%s/%s-%d-%d",
                            FileUtils.getStoragePath(getActivity(), -1, Globals.CALIBRATE_FOLDER,
                                    false),
                            Globals.PHOTO_TEMP_FILE, mTestType, position
                    )
            );
            if (file.exists()) {
                //mPhotoImageView.setMinimumHeight(250);
                mPhotoImageView.setImageBitmap(
                        ImageUtils.getAnalysedBitmap(file.getAbsolutePath())
                );
                mPhotoImageView.setMaxHeight(250);
            }
        } else {
            accuracy = 100;
        }

        speedometer.setSpeed(accuracy, animate);

        int minAccuracy = PreferencesUtils.getInt(mainApp, R.string.minPhotoQuality, 0);

        mQualityTextView.setText(accuracy + "%");
        if (accuracy < minAccuracy) {
            mErrorQualityText.setVisibility(View.VISIBLE);
            mColorButton.setText(getString(R.string.error));
        } else {
            mErrorQualityText.setVisibility(View.GONE);
            mColorButton.setText("");
        }

        if (color == -1) {
            color = colorRange.get(index);
        }

        mColorButton.setBackgroundColor(color);

        mValueButton.setText(mainApp.doubleFormat
                .format((position + mainApp.rangeStartIncrement) * (mainApp.rangeIncrementValue
                        * mainApp.rangeIncrementValue)));

        mStartButton.setEnabled(true);
    }

    /**
     * Calibrate by taking a photo and using its color for the chosen index
     *
     * @param index The index of the value to be calibrated
     */
    void startCalibration(final int index) {

        final Context context = getActivity();
        progressDialog = ProgressDialog.show(context,
                context.getString(R.string.working),
                context.getString(R.string.analysingWait), true, false);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                if (camera == null) {
                    camera = CameraUtils.getCamera(context);
                }

                Camera.Parameters parameters = camera.getParameters();

                int zoom = PreferencesUtils.getInt(context, R.string.cameraZoomPref, -1);

                if (zoom == -1) {
                    parameters.setZoom(parameters.getMaxZoom());
                } else {
                    parameters.setZoom(Math.min(zoom, parameters.getMaxZoom()));
                }
                camera.setParameters(parameters);
                camera.startPreview();

                PhotoHandler photoHandler = new PhotoHandler(
                        getActivity().getApplicationContext(), mPhotoTakenHandler, index, "",
                        mTestType);
                camera.takePicture(null, null, photoHandler);
            }
        }, 4000);


    }

    /**
     * Reset the color back to the factory preset
     *
     * @param context  The context
     * @param position The index of the swatch color to be reset
     */
    private void resetCalibration(Context context, int position) {
        // Context context = v.getContext().getApplicationContext();
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        MainApp mainApp = ((MainApp) context.getApplicationContext());

        if (mainApp != null) {

            ArrayList<Integer> colorList = mainApp.colorList;
            ArrayList<Integer> presetColorList = mainApp.presetColorList;
            // ArrayList<String> colorNames = mainApp.colorNames;
            //ArrayList<String> presetColorNames = mainApp.presetColorNames;
            //position = position * INDEX_INCREMENT_STEP;
            int index = position * mainApp.rangeIncrementStep;
            colorList.set(index, presetColorList.get(index));

            //colorNames.set(position, presetColorNames.get(position));

            editor.remove(String.format("%d-%s", mTestType, String.valueOf(index)));
            editor.remove(String.format("%d-a-%s", mTestType, String.valueOf(index)));

            editor.commit();

            File file = new File(
                    String.format("%s/%s-%d-%d",
                            FileUtils.getStoragePath(getActivity(), -1, Globals.CALIBRATE_FOLDER,
                                    false),
                            Globals.PHOTO_TEMP_FILE, mTestType, position
                    )
            );

            file.delete();

            mPhotoImageView.setImageBitmap(null);
            autoGenerateColors(index, colorList.get(index), colorList, mainApp.rangeIncrementStep,
                    editor);

            editor.commit();
        }

        displayInfo(true);
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

                displayInfo(true);

                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                //notifyDataSetChanged();
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

    private static class PhotoTakenHandler extends Handler {

        private final WeakReference<CalibrateItemFragment> mAdapter;

        public PhotoTakenHandler(CalibrateItemFragment adapter) {
            mAdapter = new WeakReference<CalibrateItemFragment>(adapter);
        }

        @Override
        public void handleMessage(Message msg) {
            CalibrateItemFragment adapter = mAdapter.get();
            if (adapter != null) {
                adapter.camera.stopPreview();
                adapter.camera.release();
                adapter.camera = null;
                adapter.storeCalibratedData(msg.getData().getInt("position"),
                        msg.getData().getInt("resultColor"), msg.getData().getInt("accuracy"));
            }
        }
    }

}
