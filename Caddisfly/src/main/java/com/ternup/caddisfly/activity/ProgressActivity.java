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
import com.ternup.caddisfly.app.Globals;
import com.ternup.caddisfly.app.MainApp;
import com.ternup.caddisfly.provider.TestContentProvider;
import com.ternup.caddisfly.service.CameraService;
import com.ternup.caddisfly.service.CameraServiceReceiver;
import com.ternup.caddisfly.util.AudioUtils;
import com.ternup.caddisfly.util.CameraUtils;
import com.ternup.caddisfly.util.FileUtils;
import com.ternup.caddisfly.util.ImageUtils;
import com.ternup.caddisfly.util.PhotoHandler;
import com.ternup.caddisfly.util.PreferencesHelper;
import com.ternup.caddisfly.util.PreferencesUtils;
import com.ternup.caddisfly.util.ShakeDetector;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ProgressActivity extends Activity {

    private final PhotoTakenHandler mPhotoTakenHandler = new PhotoTakenHandler(this);

    private final BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            InitializeTest(context);
        }
    };

    private LinearLayout mProgressLayout;

    private LinearLayout mShakeLayout;

    private TextView mTitleText;

    private TextView mRemainingText;

    private ProgressBar mProgressBar;

    private TextView mTimeText;

    private TextView mPlaceInStandText;

    private SensorManager mSensorManager;

    private Sensor mAccelerometer;

    private ShakeDetector mShakeDetector;

    //Vibrator mVibrator;
    //private MediaPlayer cameraMediaPlayer;

    private PowerManager.WakeLock wakeLock;

    private MediaPlayer mMediaPlayer;

    // The folder path where the photos will be stored
    private String mFolderName;

    private long mId = -1;

    private int mIndex = 0;

    private int mInterval;

    private int mTestTotal;

    private boolean mShakeDevice;

    private boolean mTestCompleted;

    private boolean mSoundAlarm;

    // Track if the test was just started in which case it can be cancelled by back button
    private boolean mWaitingForFirstShake;

    private boolean mWaitingForShake = true;

    private boolean mWaitingForStillness = false;

    private int mTestType;

    private long mLocationId;

    private String mQuestionId;

    @Override
    public void onAttachedToWindow() {

        // disable the key guard when device wakes up and shake alert is displayed
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_FULLSCREEN |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setTheme(((MainApp) getApplicationContext()).CurrentTheme);

        setContentView(R.layout.activity_progress);

        this.setTitle(R.string.app_name);

        // Gradient shading for title
        mTitleText = (TextView) findViewById(R.id.titleText);
        Shader textShader = new LinearGradient(0, 0, 0, mTitleText.getPaint().getTextSize(),
                new int[]{Color.rgb(28, 53, 63), Color.rgb(44, 85, 103)},
                new float[]{0, 1}, Shader.TileMode.CLAMP);
        mTitleText.getPaint().setShader(textShader);
        mProgressLayout = (LinearLayout) findViewById(R.id.progressLayout);
        mShakeLayout = (LinearLayout) findViewById(R.id.shakeLayout);
        mRemainingText = (TextView) findViewById(R.id.remainingText);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mTimeText = (TextView) findViewById(R.id.timeText);
        mPlaceInStandText = (TextView) findViewById(R.id.placeInStandText);

        //Set up the shake detector
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector(new ShakeDetector.OnShakeListener() {
            @Override
            public void onShake() {
                if (mWaitingForShake) {
                    mWaitingForShake = false;

                    mWaitingForFirstShake = false;
                    if (mMediaPlayer != null) {
                        mMediaPlayer.release();
                    }
                    mPlaceInStandText.setVisibility(View.VISIBLE);
                    mShakeLayout.setVisibility(View.GONE);
                }
            }
        }, new ShakeDetector.OnNoShakeListener() {
            @Override
            public void onNoShake() {

                if (!mWaitingForShake && !mWaitingForStillness) {
                    mWaitingForStillness = true;
                    dismissShakeAndStartTest();
                }
            }
        });
    }

    /**
     * Start the test by displaying the progress bar
     */
    private void dismissShakeAndStartTest() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }
        mSensorManager.unregisterListener(mShakeDetector);
        mShakeLayout.setVisibility(View.GONE);
        mPlaceInStandText.setVisibility(View.GONE);
        mProgressLayout.setVisibility(View.VISIBLE);
        startTest(getApplicationContext(), mFolderName);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Acquire a wake lock while waiting for user action
        if (wakeLock == null || !wakeLock.isHeld()) {
            PowerManager pm = (PowerManager) getApplicationContext()
                    .getSystemService(Context.POWER_SERVICE);
            wakeLock = pm
                    .newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP
                            | PowerManager.ON_AFTER_RELEASE, "MyWakeLock");
            wakeLock.acquire();
        }

        //TODO: setup external app connection
        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Globals.ACTION_WATER_TEST.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                // todo: recode this
            }
            mTestType = -1;
        } else {
            mTestType = PreferencesHelper.getCurrentTestTypeId(this, intent);
        }
        mLocationId = PreferencesHelper.getCurrentLocationId(this, intent);

        // Register receiver for service
        registerReceiver(receiver, new IntentFilter(CameraService.NOTIFICATION));

        getSharedPreferences();

        // If test is already completed go back to home page
        if (mTestCompleted) {
            startHomeActivity(getApplicationContext());
            return;
        }

        if (mFolderName != null && !mFolderName.isEmpty()) {

            if (!hasTestCompleted(mFolderName)) {
                // If test was automatically started then initialize
                if (getIntent().getBooleanExtra("alarm", false)) {
                    getIntent().removeExtra("alarm");
                    InitializeTest(getApplicationContext());
                }
                return;
            }

            // Test is complete so cancel
            cancelService();

        } else if (mTestType > -1) {

            mRemainingText.setText(String.valueOf(mTestTotal));
            mProgressBar.setMax(mTestTotal);
            mProgressBar.setProgress(0);
            startNewTest(mTestType);

            if (mShakeDevice) {
                mShakeLayout.setVisibility(View.VISIBLE);
                mProgressLayout.setVisibility(View.GONE);
                mWaitingForFirstShake = true;

                mSensorManager.unregisterListener(mShakeDetector);

                mWaitingForShake = true;
                mWaitingForStillness = false;
                mSensorManager.registerListener(mShakeDetector, mAccelerometer,
                        SensorManager.SENSOR_DELAY_UI);
            } else {
                mProgressLayout.setVisibility(View.VISIBLE);
                mShakeLayout.setVisibility(View.GONE);

                displayInfo();
                startTest(getApplicationContext(), mFolderName);
            }
        } else {
            startHomeActivity(getApplicationContext());
        }
    }

    private void InitializeTest(Context context) {

        if (wakeLock == null || !wakeLock.isHeld()) {
            PowerManager pm = (PowerManager) getApplicationContext()
                    .getSystemService(Context.POWER_SERVICE);
            wakeLock = pm
                    .newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP
                            | PowerManager.ON_AFTER_RELEASE, "MyWakeLock");
            wakeLock.acquire();
        }

        if (mShakeDevice) {

            if (mSoundAlarm) {
                if (mMediaPlayer != null) {
                    mMediaPlayer.release();
                }
                if (mIndex > 0) {
                    mMediaPlayer = AudioUtils.playAlarmSound(context);
                }
            } else {
            }

            mProgressLayout.setVisibility(View.GONE);
            mShakeLayout.setVisibility(View.VISIBLE);
            mWaitingForShake = true;
            mWaitingForStillness = false;
            mSensorManager.unregisterListener(mShakeDetector);
            mSensorManager.registerListener(mShakeDetector, mAccelerometer,
                    SensorManager.SENSOR_DELAY_UI);
        } else {
            mProgressLayout.setVisibility(View.VISIBLE);
            mShakeLayout.setVisibility(View.GONE);

            startTest(context, mFolderName);
        }
    }

    private void getSharedPreferences() {

        if (mTestType == -1) {
            mQuestionId = getIntent().getStringExtra("questionId");
            String questionTitle = getIntent().getStringExtra("questionTitle");

            String code = questionTitle.substring(Math.max(0, questionTitle.length() - 5));
            mTestType = getTestType(code);
        }

        MainApp mainContext = (MainApp) getApplicationContext();

        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());

        switch (mTestType) {
            case Globals.FLUORIDE_INDEX:
                mTestTotal = 1;
                mTitleText.setText(R.string.fluoride);
                mainContext.setFluorideSwatches();
                break;
            case Globals.PH_INDEX:
                mTestTotal = 1;
                mTitleText.setText(R.string.pH);
                mainContext.setPhSwatches();
                break;
            case Globals.BACTERIA_INDEX:
                mTitleText.setText(R.string.bacteria);
                mTestTotal = sharedPreferences.getInt("timer_test_count", 3);
                mInterval = sharedPreferences.getInt("analysis_interval", 1) * 60000;
                break;
        }

        mTitleText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissShakeAndStartTest();
            }
        });

        mShakeDevice = sharedPreferences.getBoolean("requireShakePref", true);
        mSoundAlarm = sharedPreferences.getBoolean("sound_alarm", true);

        mFolderName = PreferencesUtils.getString(this, R.string.runningTestFolder, "");

        mTestCompleted = sharedPreferences.getBoolean("testCompleted", false);

        if (mId == -1) {
            mId = PreferencesHelper.getCurrentTestId(this, getIntent(), null);
        }
    }

    private int getTestType(String code) {

        if (("FLUOR").equals(code)) {
            return Globals.FLUORIDE_INDEX;
        } else if (("ALKAL").equals(code)) {
            return Globals.PH_INDEX;
        } else if (("COLIF").equals(code)) {
            return Globals.BACTERIA_INDEX;
        } else if (("TURBI").equals(code)) {
            return Globals.PH_INDEX;
        } else if (("NITRA").equals(code)) {
            return Globals.PH_INDEX;
        } else if (("IRONA").equals(code)) {
            return Globals.PH_INDEX;
        } else if (("ARSEN").equals(code)) {
            return Globals.PH_INDEX;
        }

        return -1;
    }

    private void startNewTest(int testType) {

        mFolderName = getNewFolderName();

        Context context = getApplicationContext();

        // store the folder name of the current test to be able to refer to if the app is restarted
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getString(R.string.runningTestFolder), mFolderName);
        editor.putInt(PreferencesHelper.CURRENT_TEST_TYPE_KEY, testType);
        editor.commit();
    }

    private void cancelService() {

        releaseResources();

        deleteRecord();
    }

    private void deleteRecord() {
        Context context = getApplicationContext();
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);

        FileUtils.deleteFolder(this, mLocationId, mFolderName);
        mId = PreferencesHelper.getCurrentTestId(this, null, null);
        if (mId > -1) {
            Uri uri = ContentUris.withAppendedId(TestContentProvider.CONTENT_URI, mId);
            getApplicationContext().getContentResolver().delete(uri, null, null);

            PreferencesUtils.removeKey(this, PreferencesHelper.CURRENT_TEST_ID_KEY);

            mId = -1;
        }
    }

    private void sendResult(Message msg) {

        Context context = getApplicationContext();
        mId = PreferencesHelper.getCurrentTestId(this, null, msg.getData());

        double result = msg.getData().getDouble("resultValue", -1);
        int accuracy = msg.getData().getInt("accuracy", 0);
        String message = getString(R.string.testFailedMessage);

        int minAccuracy = PreferencesUtils.getInt(this, R.string.minPhotoQuality, 0);
        if (accuracy < minAccuracy) {
            message = String.format(getString(R.string.testFailedQualityMessage), minAccuracy);
        }

        if (mTestType != Globals.BACTERIA_INDEX && (result < 0 || accuracy < minAccuracy)) {

            AlertDialog myDialog;
            View alertView;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
            ViewGroup parent = (ViewGroup) findViewById(R.id.linearLayout);

            alertView = inflater.inflate(R.layout.dialog_error, parent, false);
            builder.setView(alertView);

            builder.setTitle(R.string.error);

            builder.setMessage(message);

            ImageView image = (ImageView) alertView.findViewById(R.id.image);
            //image.setImageResource(R.drawable.ic_launcher);
            image.setImageBitmap(
                    ImageUtils.getAnalysedBitmap(msg.getData().getString("file"))
            );

            builder.setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mIndex = 0;
                    deleteRecord();
                    dialog.dismiss();
                    InitializeTest(getApplicationContext());
                }
            });

            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    deleteRecord();
                    cancelService();
                    finish();
                }
            });

            builder.setCancelable(false);
            myDialog = builder.create();

            myDialog.show();

        } else {

            releaseResources();

            Intent intent = new Intent();
            if (mFolderName != null && !mFolderName.isEmpty()) {
                if (msg != null && msg.getData() != null) {
                    intent.putExtra(PreferencesHelper.FOLDER_NAME_KEY, mFolderName);
                    intent.putExtra(PreferencesHelper.CURRENT_TEST_ID_KEY, mId);

                    intent.putExtra("result", result);
                    intent.putExtra("questionId", mQuestionId);
                    intent.putExtra("response", String.valueOf(result));
                }
            }
            this.setResult(Activity.RESULT_OK, intent);

            finish();
        }
    }

    private void releaseResources() {

        try {
            unregisterReceiver(receiver);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Context context = getApplicationContext();
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(getString(R.string.runningTestFolder)); //NON-NLS
        editor.remove(PreferencesHelper.CURRENT_TEST_ID_KEY);
        editor.commit();

        mSensorManager.unregisterListener(mShakeDetector);

        MainApp mainContext = (MainApp) context;

        if (mainContext.camera != null) {
            mainContext.camera.stopPreview();
            mainContext.camera.release();
            mainContext.camera = null;
        }

        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }

        Intent serviceIntent = new Intent(context, CameraServiceReceiver.class);
        PendingIntent pi = PendingIntent
                .getBroadcast(context, 0, serviceIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pi);
    }

    private void startHomeActivity(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);

        finish();
    }

    @SuppressLint("SimpleDateFormat")
    void displayInfo() {

        ArrayList<String> imagePaths = FileUtils.getFilePaths(this, mFolderName, mLocationId);
        int doneCount = imagePaths.size();

        if (doneCount > 0) {

            Context context = getApplicationContext();
            boolean is24HourFormat = android.text.format.DateFormat.is24HourFormat(context);
            String timePattern = context.getString(
                    is24HourFormat ? R.string.twentyFourHourTime : R.string.twelveHourTime);

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MILLISECOND, mInterval);

            // Using SimpleDateFormat to display seconds also
            DateFormat timeFormat = new SimpleDateFormat(timePattern);
            mTimeText.setText(timeFormat.format(cal.getTimeInMillis()));

            mProgressBar.setMax(mTestTotal);
            mProgressBar.setProgress(doneCount);
        }

        mRemainingText.setText(String.valueOf(mTestTotal - doneCount));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        try {
            unregisterReceiver(receiver);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Context context = getApplicationContext();
        MainApp mainContext = (MainApp) context;

        if (mainContext.camera != null) {
            mainContext.camera.stopPreview();
            mainContext.camera.release();
            mainContext.camera = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    void startTest(final Context context, final String folderName) {

        (new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);

                MainApp mainContext = (MainApp) getApplicationContext();

                if (!hasTestCompleted(folderName)) {

                    if (mainContext.camera == null) {
                        mainContext.camera = CameraUtils.getCamera(mainContext);
                    }

                    Camera.Parameters parameters = mainContext.camera.getParameters();
                    SharedPreferences sharedPreferences = PreferenceManager
                            .getDefaultSharedPreferences(context);

                    int zoom = sharedPreferences.getInt("camera_zoom", -1);

                    if (zoom == -1) {
                        parameters.setZoom(parameters.getMaxZoom());
                    } else {
                        parameters.setZoom(Math.min(zoom, parameters.getMaxZoom()));
                    }
                    mainContext.camera.setParameters(parameters);
                    mainContext.camera.startPreview();

                    PhotoHandler photoHandler = new PhotoHandler(mainContext, mPhotoTakenHandler,
                            mIndex, folderName, mTestType);
                    //soundCamera(context);
                    mainContext.camera.takePicture(null, null, photoHandler);
                }
            }
        }).execute();

    }

    private boolean hasTestCompleted(String folderName) {

        Context context = getApplicationContext();
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);

        mLocationId = PreferencesHelper.getCurrentLocationId(this, null);

        if (sharedPreferences.getString("runningTestFolder", "").isEmpty()) {
            return true;
        } else {
            ArrayList<String> imagePaths = FileUtils.getFilePaths(this, folderName, mLocationId);
            mIndex = imagePaths.size();
            return imagePaths.size() >= mTestTotal;
        }
    }

    private String getNewFolderName() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(Globals.FOLDER_NAME_DATE_FORMAT,
                Locale.US);
        return dateFormat.format(new Date()).trim() + "-" + mTestType;
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (mWaitingForFirstShake) {
            cancelService();
        }
    }

    @Override
    public void onBackPressed() {
        if (mWaitingForFirstShake) {
            cancelService();
            finish();
        } else {
            //Clear the activity back stack
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_cancel:
                cancelService();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.progress, menu);
        return true;
    }

    private static class PhotoTakenHandler extends Handler {

        private final WeakReference<ProgressActivity> mService;

        public PhotoTakenHandler(ProgressActivity service) {
            mService = new WeakReference<ProgressActivity>(service);
        }

        @Override
        public void handleMessage(Message msg) {

            ProgressActivity service = mService.get();

            if (service != null) {

                MainApp mainContext = (MainApp) service.getApplicationContext();

                if (mainContext.camera != null) {
                    mainContext.camera.stopPreview();
                    mainContext.camera.release();
                    mainContext.camera = null;
                }

                Bundle bundle = msg.getData();

                if (bundle != null) {
                    String folderName = msg.getData()
                            .getString(PreferencesHelper.FOLDER_NAME_KEY); //NON-NLS
                    if (service.hasTestCompleted(folderName)) {
                        service.sendResult(msg);
                    } else {
                        Calendar cal = Calendar.getInstance();
                        cal.add(Calendar.MILLISECOND, (service.mInterval - 3000));

                        Intent serviceIntent = new Intent(service.getApplicationContext(),
                                CameraServiceReceiver.class);
                        PendingIntent pi = PendingIntent
                                .getBroadcast(service.getApplicationContext(), 0, serviceIntent,
                                        PendingIntent.FLAG_CANCEL_CURRENT);
                        AlarmManager alarmManager = (AlarmManager) service.getApplicationContext()
                                .getSystemService(Context.ALARM_SERVICE);

                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                            alarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
                        } else {
                            alarmManager
                                    .setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
                        }

                        service.displayInfo();

                        service.registerReceiver(service.receiver,
                                new IntentFilter(CameraService.NOTIFICATION));

                        if (service.wakeLock != null && service.wakeLock.isHeld()) {
                            service.wakeLock.release();
                        }
                    }
                }
            }
        }
    }
}
