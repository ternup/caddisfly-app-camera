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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ternup.caddisfly.R;
import com.ternup.caddisfly.app.Globals;
import com.ternup.caddisfly.app.MainApp;
import com.ternup.caddisfly.database.DataStorage;
import com.ternup.caddisfly.fragment.CameraFragment;
import com.ternup.caddisfly.service.CameraService;
import com.ternup.caddisfly.service.CameraServiceReceiver;
import com.ternup.caddisfly.util.AlertUtils;
import com.ternup.caddisfly.util.AudioUtils;
import com.ternup.caddisfly.util.FileUtils;
import com.ternup.caddisfly.util.ImageUtils;
import com.ternup.caddisfly.util.PhotoHandler;
import com.ternup.caddisfly.util.PreferencesHelper;
import com.ternup.caddisfly.util.PreferencesUtils;
import com.ternup.caddisfly.util.ShakeDetector;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ProgressActivityBase extends Activity implements CameraFragment.Cancelled {

    protected final BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            InitializeTest(context);
        }
    };
    final Handler delayHandler = new Handler();
    private final PhotoTakenHandler mPhotoTakenHandler = new PhotoTakenHandler((ProgressActivity) this);
    protected PowerManager.WakeLock wakeLock;
    protected int mInterval;
    protected int mTestType;
    // The folder path where the photos will be stored
    protected String mFolderName;
    CameraFragment mCameraFragment;
    File calibrateFolder;
    ArrayList<String> oldFilePaths;
    //private ProgressBar mSingleProgress;
    Timer timer;
    Runnable delayRunnable;
    private LinearLayout mProgressLayout;
    private LinearLayout mShakeLayout;
    private LinearLayout mStillnessLayout;
    private TextView mTitleText;
    private TextView mRemainingText;
    private ProgressBar mProgressBar;
    //Vibrator mVibrator;
    //private MediaPlayer cameraMediaPlayer;
    private TextView mTimeText;
    private long mNextAlarmTime;
    private TextView mPlaceInStandText;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;
    private MediaPlayer mMediaPlayer;
    private long mId = -1;
    private int mIndex = 0;
    private int mTestTotal;
    private boolean mShakeDevice;
    private boolean mTestCompleted;
    private boolean mSoundAlarm;
    // Track if the test was just started in which case it can be cancelled by back button
    private boolean mWaitingForFirstShake;
    private boolean mWaitingForShake = true;
    private boolean mWaitingForStillness = false;
    private long mLocationId;

    private TextView mRemainingValueText;

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

        this.setTitle(R.string.appName);

        // Gradient shading for title
        mTitleText = (TextView) findViewById(R.id.titleText);
        Shader textShader = new LinearGradient(0, 0, 0, mTitleText.getPaint().getTextSize(),
                new int[]{Color.rgb(28, 53, 63), Color.rgb(44, 85, 103)},
                new float[]{0, 1}, Shader.TileMode.CLAMP);
        mTitleText.getPaint().setShader(textShader);
        mProgressLayout = (LinearLayout) findViewById(R.id.progressLayout);
        mStillnessLayout = (LinearLayout) findViewById(R.id.stillnessLayout);
        mShakeLayout = (LinearLayout) findViewById(R.id.shakeLayout);
        mRemainingValueText = (TextView) findViewById(R.id.remainingValueText);
        mRemainingText = (TextView) findViewById(R.id.remainingText);
        mProgressBar = (ProgressBar) findViewById(R.id.testProgressBar);
        mTimeText = (TextView) findViewById(R.id.timeText);
        mPlaceInStandText = (TextView) findViewById(R.id.placeInStandText);
        //mSingleProgress = (ProgressBar) findViewById(R.id.singleProgress);

        //Set up the shake detector
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mShakeDetector = new ShakeDetector(new ShakeDetector.OnShakeListener() {
            @Override
            public void onShake() {
                if (mWaitingForShake) {
                    mWaitingForShake = false;
                    mWaitingForStillness = true;

                    mWaitingForFirstShake = false;
                    if (mMediaPlayer != null) {
                        mMediaPlayer.release();
                    }
                    mStillnessLayout.setVisibility(View.VISIBLE);
                    mShakeLayout.setVisibility(View.GONE);
                    mProgressLayout.setVisibility(View.GONE);
                } else {
                    if (!mWaitingForStillness && mCameraFragment != null) {
                        mWaitingForStillness = true;
                        mStillnessLayout.setVisibility(View.VISIBLE);
                        mShakeLayout.setVisibility(View.GONE);
                        mProgressLayout.setVisibility(View.GONE);
                        if (mCameraFragment != null) {
                            try {
                                mCameraFragment.stopCamera();
                                mCameraFragment.dismiss();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            mStillnessLayout.setVisibility(View.GONE);
                            mProgressLayout.setVisibility(View.GONE);
                            mShakeLayout.setVisibility(View.GONE);

                            showError(getString(R.string.testInterrupted), null);
                        }

                    }
                }
            }
        }, new ShakeDetector.OnNoShakeListener() {
            @Override
            public void onNoShake() {

                if (!mWaitingForShake && mWaitingForStillness) {
                    mWaitingForStillness = false;
                    dismissShakeAndStartTest();
                }
            }
        });
        mShakeDetector.minShakeAcceleration = 5;
        mShakeDetector.maxShakeDuration = 2000;
    }

    private void showError(String message, Bitmap bitmap) {
        cancelService();
        AlertUtils.showError(this, R.string.error, message, bitmap, R.string.retry,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mIndex = 0;
                        mStillnessLayout.setVisibility(View.VISIBLE);
                        mProgressLayout.setVisibility(View.GONE);
                        mShakeLayout.setVisibility(View.GONE);

                        startNewTest(mTestType);
                        InitializeTest(getApplicationContext());
                    }
                },
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(getIntent());
                        setResult(Activity.RESULT_CANCELED, intent);
                        finish();
                    }
                }
        );
    }

    /**
     * Start the test by displaying the progress bar
     */
    private void dismissShakeAndStartTest() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }
        mSensorManager.unregisterListener(mShakeDetector);
        mProgressLayout.setVisibility(View.VISIBLE);
        mStillnessLayout.setVisibility(View.GONE);
        mShakeLayout.setVisibility(View.GONE);

        startTest(getApplicationContext(), mFolderName);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        // Acquire a wake lock while waiting for user action
/*
        if (wakeLock == null || !wakeLock.isHeld()) {
            PowerManager pm = (PowerManager) getApplicationContext()
                    .getSystemService(Context.POWER_SERVICE);
            wakeLock = pm
                    .newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP
                            | PowerManager.ON_AFTER_RELEASE, "MyWakeLock");
            wakeLock.acquire();
        }
*/

        MainApp mainApp = (MainApp) getApplicationContext();

        //TODO: setup external app connection
        // Get intent, action and MIME type
        Intent intent = getIntent();
        //String action = intent.getAction();
        //String type = intent.getType();

        mTestType = mainApp.currentTestType;


        // Register receiver for service
        registerReceiver(receiver, new IntentFilter(CameraService.NOTIFICATION));

        getSharedPreferences();

        mIndex = intent.getIntExtra("position", -1);
        if (mIndex > -1) {
            mLocationId = -1;
            mFolderName = "";
            mId = -1;
        } else {
            mLocationId = PreferencesHelper.getCurrentLocationId(this, intent);
        }

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

            if (mTestType == Globals.BACTERIA_INDEX) {
                mRemainingValueText.setText(String.valueOf(mTestTotal));
                mRemainingText.setVisibility(View.VISIBLE);
                mRemainingValueText.setVisibility(View.VISIBLE);
            } else {
                mRemainingText.setVisibility(View.GONE);
                mRemainingValueText.setVisibility(View.GONE);
            }
            mProgressBar.setMax(mTestTotal);
            mProgressBar.setProgress(0);
            startNewTest(mTestType);

            if (mShakeDevice) {
                mShakeLayout.setVisibility(View.VISIBLE);
                mStillnessLayout.setVisibility(View.GONE);
                mProgressLayout.setVisibility(View.GONE);
                mWaitingForFirstShake = true;

                mSensorManager.unregisterListener(mShakeDetector);

                mWaitingForShake = true;
                mWaitingForStillness = false;
                mSensorManager.registerListener(mShakeDetector, mAccelerometer,
                        SensorManager.SENSOR_DELAY_UI);
            } else {

                mWaitingForStillness = true;
                mWaitingForShake = false;
                mWaitingForFirstShake = false;
                mStillnessLayout.setVisibility(View.VISIBLE);
                mShakeLayout.setVisibility(View.GONE);
                mProgressLayout.setVisibility(View.GONE);

                mSensorManager.registerListener(mShakeDetector, mAccelerometer,
                        SensorManager.SENSOR_DELAY_UI);

                //displayInfo();
                //startTest(getApplicationContext(), mFolderName);
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
            }

            mShakeLayout.setVisibility(View.VISIBLE);
            mStillnessLayout.setVisibility(View.GONE);
            mProgressLayout.setVisibility(View.GONE);
            mWaitingForShake = true;
            mWaitingForStillness = false;
            mSensorManager.unregisterListener(mShakeDetector);
            mShakeDetector.minShakeAcceleration = 5;
            mShakeDetector.maxShakeDuration = 2000;
        } else {
            mWaitingForShake = false;
            mWaitingForStillness = true;
        }
        mSensorManager.registerListener(mShakeDetector, mAccelerometer,
                SensorManager.SENSOR_DELAY_UI);

    }

    private void getSharedPreferences() {

        MainApp mainContext = (MainApp) getApplicationContext();

        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());

        switch (mTestType) {
            case Globals.FLUORIDE_INDEX:
                mTestTotal = 1;
                mTitleText.setText(R.string.fluoride);
                mainContext.setFluorideSwatches();
                break;
            case Globals.FLUORIDE_2_INDEX:
                mTestTotal = 1;
                mTitleText.setText(R.string.fluoride2);
                mainContext.setFluoride2Swatches();
                break;
            case Globals.PH_INDEX:
                mTestTotal = 1;
                mTitleText.setText(R.string.pH);
                mainContext.setPhSwatches();
                break;
            case Globals.BACTERIA_INDEX:
                mTitleText.setText(R.string.bacteria);
                mTestTotal = sharedPreferences.getInt("timer_test_count", 3);
                mInterval = sharedPreferences.getInt("analysis_interval", 1) * Globals.MINUTE_IN_MS;
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

    private void startNewTest(int testType) {

        if (mLocationId > -1) {
            mFolderName = getNewFolderName();
        } else if (mIndex > -1) {
            calibrateFolder = new File(
                    FileUtils.getStoragePath(this, -1,
                            String.format("%s/%d/%d/small/", Globals.CALIBRATE_FOLDER, mTestType,
                                    mIndex),
                            false
                    )
            );

            oldFilePaths = FileUtils
                    .getFilePaths(this, calibrateFolder.getAbsolutePath(), -1);
        }

        Context context = getApplicationContext();
        PreferencesUtils.setInt(context, R.string.currentSamplingCountKey, 0);

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

        DataStorage.deleteRecord(this, mId, mLocationId, mFolderName);

        if (mIndex > -1 && mLocationId == -1) {
            FileUtils.deleteFilesExcepting(calibrateFolder, oldFilePaths);
        }
    }

    protected void sendResult(Message msg) {
        mSensorManager.unregisterListener(mShakeDetector);

        mId = PreferencesHelper.getCurrentTestId(this, null, msg.getData());

        final double result = msg.getData().getDouble(Globals.RESULT_VALUE_KEY, -1);
        final int quality = msg.getData().getInt(Globals.QUALITY_KEY, 0);
        final int resultColor = msg.getData().getInt(Globals.RESULT_COLOR_KEY, 0);
        String message = getString(R.string.testFailedMessage);

        int minAccuracy = PreferencesUtils
                .getInt(this, R.string.minPhotoQualityKey, Globals.MINIMUM_PHOTO_QUALITY);

        if (quality < minAccuracy) {
            message = String.format(getString(R.string.testFailedQualityMessage), minAccuracy);
        }

        if (mTestType != Globals.BACTERIA_INDEX && (result < 0 || quality < minAccuracy)) {
            showError(message, ImageUtils.getAnalysedBitmap(msg.getData().getString("file")));
        } else {

            releaseResources();

            Intent intent = new Intent(getIntent());
            intent.putExtra(Globals.RESULT_COLOR_KEY, resultColor);
            intent.putExtra(Globals.QUALITY_KEY, quality);

            if (mFolderName != null && !mFolderName.isEmpty()) {
                if (msg != null && msg.getData() != null) {
                    intent.putExtra(PreferencesHelper.FOLDER_NAME_KEY, mFolderName);
                    intent.putExtra(PreferencesHelper.CURRENT_TEST_ID_KEY, mId);

                    intent.putExtra("result", result);
                    //intent.putExtra("questionId", mQuestionId);
                    intent.putExtra("response", String.valueOf(result));
                }
            }
            this.setResult(Activity.RESULT_OK, intent);

            //if calibration and old photos in the calibrate folder to be deleted
            if (mIndex > -1 && mId == -1) {
                FileUtils.deleteFiles(oldFilePaths);
            }

            finish();
        }
    }

    private void releaseResources() {

        mSensorManager.unregisterListener(mShakeDetector);
        delayHandler.removeCallbacks(delayRunnable);
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

    protected void startHomeActivity(Context context) {
    }

    @SuppressLint("SimpleDateFormat")
    void displayInfo() {

        ArrayList<String> imagePaths = FileUtils
                .getFilePaths(this, mFolderName, "/small/", mLocationId);
        int doneCount = imagePaths.size();

        if (doneCount > 0) {

            Context context = getApplicationContext();
            boolean is24HourFormat = android.text.format.DateFormat.is24HourFormat(context);
            //String timePattern = context.getString(is24HourFormat ? R.string.twentyFourHourTime : R.string.twelveHourTime);

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MILLISECOND, mInterval);

            startTimeDisplay(cal.getTimeInMillis());

            // Using SimpleDateFormat to display seconds also
            //DateFormat timeFormat = new SimpleDateFormat(timePattern);
            //mTimeText.setText(timeFormat.format(cal.getTimeInMillis()));

            mProgressBar.setMax(mTestTotal);
            mProgressBar.setProgress(doneCount);
        }

        mRemainingValueText.setText(String.valueOf(mTestTotal - doneCount));
    }

    private String formatTime(long time) {
        String res = "";
        res += time / 60 + ":";
        if (time % 60 < 10) {
            res += "0";
        }
        res += (time % 60);
        return res;
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void displayRemainingTime() {
        long timeLeft = (mNextAlarmTime - System.currentTimeMillis()) / 1000;
        mTimeText.setText(formatTime(timeLeft));

    }

    private void startTimeDisplay(long nextTime) {
        if (timer != null) {
            timer.cancel();
        }
        mNextAlarmTime = nextTime - Globals.INITIAL_DELAY - Globals.INITIAL_DELAY;
        displayRemainingTime();
        timer = new Timer(5000, 5000);
        timer.start();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    void startTest(final Context context, final String folderName) {

        mWaitingForShake = false;
        mWaitingForFirstShake = false;
        mWaitingForStillness = false;

        mShakeDetector.minShakeAcceleration = 0.5;
        mShakeDetector.maxShakeDuration = 3000;
        mSensorManager.registerListener(mShakeDetector, mAccelerometer,
                SensorManager.SENSOR_DELAY_UI);

        (new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
/*
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
*/
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);

                MainApp mainContext = (MainApp) getApplicationContext();

                if (!hasTestCompleted(folderName)) {

                    PhotoHandler photoHandler = new PhotoHandler(mainContext, mPhotoTakenHandler,
                            mIndex, folderName, mTestType);

                    final FragmentTransaction ft = getFragmentManager().beginTransaction();

                    Fragment prev = getFragmentManager().findFragmentByTag("cameraDialog");
                    if (prev != null) {
                        ft.remove(prev);
                    }
                    ft.addToBackStack(null);

                    mCameraFragment = CameraFragment.newInstance();
                    mCameraFragment.pictureCallback = photoHandler;

                    if (mTestType == Globals.BACTERIA_INDEX) {
                        mCameraFragment.makeShutterSound = true;
                    }

                    if (wakeLock == null || !wakeLock.isHeld()) {
                        PowerManager pm = (PowerManager) getApplicationContext()
                                .getSystemService(Context.POWER_SERVICE);
                        wakeLock = pm
                                .newWakeLock(PowerManager.FULL_WAKE_LOCK
                                        | PowerManager.ACQUIRE_CAUSES_WAKEUP
                                        | PowerManager.ON_AFTER_RELEASE, "MyWakeLock");
                        wakeLock.acquire();
                    }

                    delayRunnable = new Runnable() {
                        @Override
                        public void run() {
                            if (this != null) {
                                mCameraFragment.show(ft, "cameraDialog");
                            }

                        }
                    };

                    delayHandler.postDelayed(delayRunnable, Globals.INITIAL_DELAY);
                }
            }
        }).execute();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
    }

    private boolean hasSamplingCompleted() {

        if (mTestType == Globals.BACTERIA_INDEX) {
            return true;
        }
        Context context = getApplicationContext();

        int samplingCount = PreferencesUtils.getInt(context, R.string.currentSamplingCountKey, 0);
        return samplingCount >= PreferencesUtils
                .getInt(context, R.string.samplingCountKey, Globals.SAMPLING_COUNT_DEFAULT);
    }

    protected boolean hasTestCompleted(String folderName) {

        if (!hasSamplingCompleted() && mTestType != Globals.BACTERIA_INDEX) {
            return false;
        }
        Context context = getApplicationContext();
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);

        mLocationId = PreferencesHelper.getCurrentLocationId(this, null);

        if (sharedPreferences.getString("runningTestFolder", "").isEmpty()) {
            return true;
        } else {
            ArrayList<String> imagePaths = FileUtils
                    .getFilePaths(this, folderName, "/small/", mLocationId);
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
            Intent intent = new Intent(getIntent());
            this.setResult(Activity.RESULT_CANCELED, intent);
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
                Intent intent = new Intent(getIntent());
                this.setResult(Activity.RESULT_CANCELED, intent);
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

    @Override
    public void dialogCancelled() {
        cancelService();
        Intent intent = new Intent(getIntent());
        this.setResult(Activity.RESULT_CANCELED, intent);

        finish();
    }

    public void unregisterShakeSensor() {
        mSensorManager.unregisterListener(mShakeDetector);
    }

    private static class PhotoTakenHandler extends Handler {

        private final WeakReference<ProgressActivity> mService;

        public PhotoTakenHandler(ProgressActivity service) {
            mService = new WeakReference<ProgressActivity>(service);
        }

        @Override
        public void handleMessage(Message msg) {

            ProgressActivity service = mService.get();

            service.mCameraFragment.dismiss();

            if (service != null) {
                Bundle bundle = msg.getData();

                if (bundle != null) {
                    String folderName = msg.getData()
                            .getString(PreferencesHelper.FOLDER_NAME_KEY); //NON-NLS
                    if (service.hasTestCompleted(folderName)) {
                        service.unregisterShakeSensor();
                        service.sendResult(msg);
                    } else {
                        Calendar cal = Calendar.getInstance();
                        cal.add(Calendar.MILLISECOND, (service.mInterval - Globals.INITIAL_DELAY
                                - Globals.INITIAL_DELAY));

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

    public class Timer extends CountDownTimer {

        public Timer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            timer = new Timer(5000, 5000);
            timer.start();
        }

        @Override
        public void onTick(long millisUntilFinished) {
            displayRemainingTime();
        }
    }
}

