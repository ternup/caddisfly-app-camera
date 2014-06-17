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

package com.ternup.caddisfly.util;

import com.ternup.caddisfly.R;
import com.ternup.caddisfly.app.Globals;
import com.ternup.caddisfly.app.MainApp;
import com.ternup.caddisfly.database.TestTable;
import com.ternup.caddisfly.provider.TestContentProvider;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PhotoHandler implements PictureCallback {

    private final Context mContext;

    private final Handler mHandler;

    private final int mIndex;

    private final String mFolderName;

    private final int mTestType;

    private long mLocationId;

    public PhotoHandler(Context context, Handler handler, int index, String folderName,
            int testType) {
        this.mContext = context;
        this.mHandler = handler;
        this.mIndex = index;
        this.mFolderName = folderName;
        this.mTestType = testType;
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {

        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(mContext);

        File pictureFile;
        String photoFolder;

        String photoFile;
        //= String.format("%s-%d-%d", Globals.PHOTO_TEMP_FILE, mTestType, mIndex);

        mLocationId = sharedPreferences.getLong(mContext.getString(R.string.currentLocationId), -1);
        SimpleDateFormat dateFormat = new SimpleDateFormat(Globals.FOLDER_NAME_DATE_FORMAT,
                Locale.US);
        photoFile = String.format("pic-%s", dateFormat.format(new Date()));

        if (mFolderName.length() > 0) {
            photoFolder = FileUtils.getStoragePath(mContext, mLocationId, mFolderName, true);
            pictureFile = new File(photoFolder + photoFile);
        } else {
            int currentSamplingCount = PreferencesUtils
                    .getInt(mContext, R.string.currentSamplingCountKey, 2);
            //photoFile = String.format("%s-%d", Globals.PHOTO_TEMP_FILE, currentSamplingCount);
            photoFolder = FileUtils.getStoragePath(mContext, -1,
                    String.format("%s/%d/%d/", Globals.CALIBRATE_FOLDER, mTestType, mIndex),
                    true);
            pictureFile = new File(photoFolder + photoFile);
        }

        if (PreferencesUtils.getBoolean(mContext, R.string.saveOriginalPhotoKey, false)) {
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        int sampleLength = PreferencesUtils.getInt(mContext, R.string.photoSampleDimensionKey, 200);
        int[] pixels = new int[sampleLength * sampleLength];
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        bitmap.setHasAlpha(true);
        bitmap.getPixels(pixels, 0, sampleLength,
                (bitmap.getWidth() - sampleLength) / 2,
                (bitmap.getHeight() - sampleLength) / 2,
                sampleLength,
                sampleLength);
        bitmap = Bitmap.createBitmap(pixels, 0, sampleLength,
                sampleLength,
                sampleLength,
                Bitmap.Config.ARGB_8888);
        bitmap = ImageUtils.getRoundedShape(bitmap, sampleLength);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
        byte[] croppedData = bos.toByteArray();

        //Bitmap bitmap = Utility.decodeFile(pictureFile.getAbsolutePath());
        //bitmap = ThumbnailUtils.extractThumbnail(bitmap, 400, 400);

        Message msg = mHandler.obtainMessage();

        ArrayList<Integer> colorRange = ((MainApp) mContext).colorList;

        Bundle bundle;

        bundle = ColorUtils.getPpmValue(croppedData, colorRange,
                ((MainApp) mContext).rangeIncrementValue,
                ((MainApp) mContext).rangeStartIncrement, sampleLength);

        File smallImageFolder = new File(photoFolder + "/small/");
        if (!smallImageFolder.exists()) {
            smallImageFolder.mkdirs();
        }

        File smallFile = new File(smallImageFolder.getAbsolutePath() + "/" + photoFile + "-s");

        try {
            FileOutputStream fos = new FileOutputStream(smallFile);
            fos.write(croppedData);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


/*
        Bitmap bitmap = ImageUtils.getAnalysedBitmap(pictureFile.getAbsolutePath());
        ImageUtils.saveBitmap(bitmap,
                smallImageFolder.getAbsolutePath() + "/" + photoFile + "-s");
*/

        long id = -1;
        if (mIndex < 1 && mFolderName.length() > 0) {
            saveTempResult(mContext, bundle.getDouble(MainApp.RESULT_VALUE_KEY),
                    bundle.getInt(MainApp.RESULT_COLOR_KEY),
                    bundle.getInt(MainApp.QUALITY_KEY));

            if (hasSamplingCompleted(mContext)) {
                getAverageResult(mContext, bundle);
                id = saveResult(mFolderName, mTestType, bundle.getDouble(MainApp.RESULT_VALUE_KEY));
                saveResultToPreferences(id);
            }
            bundle.putLong(mContext.getString(R.string.currentTestId), id);

/*
            int value = 0;
            int counter = 0;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            while (value != -1) {
                value = PreferencesUtils
                        .getInt(mContext,
                                String.format(mContext.getString(R.string.resultQualityKey),
                                        mTestType, id, counter), -1
                        );
                if (value > -1) {
                    editor.remove(
                            String.format(mContext.getString(R.string.resultQualityKey), mTestType,
                                    id, counter)
                    );
                    counter++;
                }
            }
*/
        } else {

            saveTempResult(mContext, 0,
                    bundle.getInt(MainApp.RESULT_COLOR_KEY),
                    bundle.getInt(MainApp.QUALITY_KEY));

            if (hasSamplingCompleted(mContext)) {
                getAverageResult(mContext, bundle);
                saveResultToPreferences(mIndex);
            }
        }

        if (id == -1) {
            id = sharedPreferences.getLong(mContext.getString(R.string.currentTestId), -1);
        }
/*
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(
                String.format(mContext.getString(R.string.resultQualityKey), mTestType, id, mIndex),
                bundle.getInt(
                        MainApp.QUALITY_KEY)
        );
        editor.commit();
*/

        bundle.putInt("position", mIndex);
        bundle.putString(mContext.getString(R.string.folderName), mFolderName);
        bundle.putString("file", smallFile.getAbsolutePath());
        bundle.putLong(mContext.getString(R.string.currentTestId), id);
        msg.setData(bundle);

        mHandler.sendMessage(msg);
    }

    private double mostFrequent(double[] ary) {
        Map<Double, Integer> m = new HashMap<Double, Integer>();

        for (double a : ary) {
            if (a >= 0) {
                Integer freq = m.get(a);
                m.put(a, (freq == null) ? 1 : freq + 1);
            }
        }

        int max = -1;
        double mostFrequent = -1;

        for (Map.Entry<Double, Integer> e : m.entrySet()) {
            if (e.getValue() > max) {
                mostFrequent = e.getKey();
                max = e.getValue();
            }
        }

        return mostFrequent;
    }

    private double getAverageResult(Context context, Bundle bundle) {
        //double result = Math.max(0, resultValue);

        double result = 0;

        int samplingCount = PreferencesUtils.getInt(context, R.string.samplingCountKey, 5);
        //saveTempResult(context, resultValue, bundle.getInt(MainApp.RESULT_COLOR_KEY));
        int counter = 0;
        double commonResult = 0;
        double[] results = new double[samplingCount];
        int[] colors = new int[samplingCount];
        for (int i = 0; i < samplingCount; i++) {
            String key = String.format(context.getString(R.string.samplingIndexKey), i);
            results[i] = PreferencesUtils.getDouble(context, key);
            key = String.format(context.getString(R.string.samplingColorIndexKey), i);
            colors[i] = PreferencesUtils.getInt(context, key, -1);
            commonResult = mostFrequent(results);
        }

        int red = 0;
        int green = 0;
        int blue = 0;
        for (int i = 0; i < results.length; i++) {
            if (results[i] >= 0 && colors[i] != -1) {
                if (Math.abs(results[i] - commonResult) < 0.5) {
                    counter++;
                    result += results[i];
                    red += Color.red(colors[i]);
                    green += Color.green(colors[i]);
                    blue += Color.blue(colors[i]);
                }
            }
        }

        if (counter > 0) {
            result = result / counter;
            bundle.putDouble(MainApp.RESULT_VALUE_KEY, result);
            bundle.putInt(MainApp.RESULT_COLOR_KEY,
                    Color.rgb(red / counter, green / counter, blue / counter));
        } else {
            result = -1;
        }

/*
        for (int i = 1; i < samplingCount; i++) {
            String key = String.format(context.getString(R.string.samplingIndexKey), i);
            double tempResult = PreferencesUtils.getDouble(context, key);
            if (tempResult >= 0) {
                counter++;
                result += tempResult;
            }
        }
*/

        return result;
    }

    private void saveTempResult(Context context, double resultValue, int resultColor, int quality) {

        int samplingCount = PreferencesUtils.getInt(context, R.string.currentSamplingCountKey, 0);
        String key = String.format(context.getString(R.string.samplingIndexKey), samplingCount);
        PreferencesUtils.setDouble(context, key, resultValue);
        key = String.format(context.getString(R.string.samplingColorIndexKey), samplingCount);
        PreferencesUtils.setInt(context, key, resultColor);
        key = String.format(context.getString(R.string.samplingQualityIndexKey), samplingCount);
        PreferencesUtils.setInt(context, key, quality);
    }


    private boolean hasSamplingCompleted(Context context) {
        if (mTestType == Globals.BACTERIA_INDEX) {
            return true;
        }

        int currentSamplingCount = PreferencesUtils
                .getInt(context, R.string.currentSamplingCountKey, 0);
        int samplingCount = PreferencesUtils.getInt(context, R.string.samplingCountKey, 5);
        return currentSamplingCount >= samplingCount - 1;
    }

    private long saveResult(String folder, int testType, double result) {

        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(mContext);

        mLocationId = sharedPreferences.getLong(mContext.getString(R.string.currentLocationId), -1);

        ContentValues values = new ContentValues();
        values.put(TestTable.COLUMN_FOLDER, folder);
        values.put(TestTable.COLUMN_DATE, (new Date().getTime()));
        values.put(TestTable.COLUMN_TYPE, testType);
        values.put(TestTable.COLUMN_RESULT, result);
        values.put(TestTable.COLUMN_LOCATION_ID, mLocationId);

        Uri uri = mContext.getContentResolver().insert(TestContentProvider.CONTENT_URI, values);
        long id = ContentUris.parseId(uri);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(mContext.getString(R.string.currentTestId), id);
        editor.commit();

        return id;
    }

    private void saveResultToPreferences(long id) {
        int samplingCount = PreferencesUtils.getInt(mContext, R.string.samplingCountKey, 5);
        for (int i = 0; i < samplingCount; i++) {
            String key = String.format(mContext.getString(R.string.samplingIndexKey), i);
            double tempResult = PreferencesUtils.getDouble(mContext, key);
            PreferencesUtils.setDouble(mContext,
                    String.format(mContext.getString(R.string.resultValueKey), mTestType, id, i),
                    tempResult);

            key = String.format(mContext.getString(R.string.samplingColorIndexKey), i);
            int tempColor = PreferencesUtils.getInt(mContext, key, -1);
            PreferencesUtils.setInt(mContext,
                    String.format(mContext.getString(R.string.resultColorKey), mTestType, id, i),
                    tempColor);

            key = String.format(mContext.getString(R.string.samplingQualityIndexKey), i);
            int tempQuality = PreferencesUtils.getInt(mContext, key, -1);
            PreferencesUtils.setInt(mContext,
                    String.format(mContext.getString(R.string.resultQualityKey), mTestType, id, i),
                    tempQuality);
        }

    }
}
