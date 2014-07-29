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

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;

import com.ternup.caddisfly.R;
import com.ternup.caddisfly.app.Globals;
import com.ternup.caddisfly.app.MainApp;
import com.ternup.caddisfly.database.DataStorage;
import com.ternup.caddisfly.model.ColorInfo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

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

        mLocationId = sharedPreferences.getLong(mContext.getString(R.string.currentLocationId), -1);
        SimpleDateFormat dateFormat = new SimpleDateFormat(Globals.FOLDER_NAME_DATE_FORMAT,
                Locale.US);
        photoFile = String.format("pic-%s", dateFormat.format(new Date()));

        if (mFolderName.length() > 0) {
            photoFolder = FileUtils.getStoragePath(mContext, mLocationId, mFolderName, true);
            pictureFile = new File(photoFolder + photoFile);
        } else {
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

        int sampleLength = PreferencesUtils.getInt(mContext, R.string.photoSampleDimensionKey,
                Globals.SAMPLE_CROP_LENGTH_DEFAULT);
        int[] pixels = new int[sampleLength * sampleLength];
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        bitmap.getPixels(pixels, 0, sampleLength,
                (bitmap.getWidth() - sampleLength) / 2,
                (bitmap.getHeight() - sampleLength) / 2,
                sampleLength,
                sampleLength);
        bitmap = Bitmap.createBitmap(pixels, 0, sampleLength,
                sampleLength,
                sampleLength,
                Bitmap.Config.ARGB_8888);

        byte[] croppedData;
        if (PreferencesUtils.getBoolean(mContext, R.string.cropToSquareKey, false)) {
//            ByteBuffer byteBuffer = ByteBuffer.allocate(bitmap.getByteCount());
//            bitmap.copyPixelsToBuffer(byteBuffer);
//            byteBuffer.rewind();
//            //croppedData = byteBuffer.array();
//            croppedData = new byte[bitmap.getByteCount()];
//            byteBuffer.get(croppedData);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            croppedData = bos.toByteArray();
        } else {
            bitmap = ImageUtils.getRoundedShape(bitmap, sampleLength);
            bitmap.setHasAlpha(true);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
            croppedData = bos.toByteArray();
        }

        bitmap.recycle();

        Message msg = mHandler.obtainMessage();

        ArrayList<ColorInfo> colorRange = ((MainApp) mContext).colorList;

        Bundle bundle = ColorUtils.getPpmValue(croppedData, colorRange,
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

        long id = -1;
        if (mIndex < 1 && mFolderName.length() > 0) {
            DataHelper.saveTempResult(mContext, bundle.getDouble(Globals.RESULT_VALUE_KEY),
                    bundle.getInt(Globals.RESULT_COLOR_KEY),
                    bundle.getInt(Globals.QUALITY_KEY));

            PreferencesHelper.incrementPhotoTakenCount(mContext);

            if (hasSamplingCompleted(mContext)) {
                DataHelper.getAverageResult(mContext, bundle);
                id = DataStorage.saveResult(mContext, mFolderName, mTestType,
                        bundle.getDouble(Globals.RESULT_VALUE_KEY));
                DataHelper.saveResultToPreferences(mContext, mTestType, id);
            }
            bundle.putLong(mContext.getString(R.string.currentTestId), id);
        } else {

            DataHelper.saveTempResult(mContext, 0,
                    bundle.getInt(Globals.RESULT_COLOR_KEY),
                    bundle.getInt(Globals.QUALITY_KEY));

            PreferencesHelper.incrementPhotoTakenCount(mContext);

            if (hasSamplingCompleted(mContext)) {
                DataHelper.getAverageResult(mContext, bundle);
                DataHelper.saveResultToPreferences(mContext, mTestType, mIndex);
            }
        }

        if (id == -1) {
            id = sharedPreferences.getLong(mContext.getString(R.string.currentTestId), -1);
        }

        bundle.putInt("position", mIndex);
        bundle.putString(mContext.getString(R.string.folderName), mFolderName);
        bundle.putString("file", smallFile.getAbsolutePath());
        bundle.putLong(mContext.getString(R.string.currentTestId), id);
        msg.setData(bundle);

        if (hasSamplingCompleted(mContext)) {
            mHandler.sendMessage(msg);
        }
    }

    private boolean hasSamplingCompleted(Context context) {
        if (mTestType == Globals.BACTERIA_INDEX) {
            return true;
        }

        int currentSamplingCount = PreferencesUtils
                .getInt(context, R.string.currentSamplingCountKey, 0);
        int samplingCount = PreferencesUtils
                .getInt(context, R.string.samplingCountKey, Globals.SAMPLING_COUNT_DEFAULT);
        return currentSamplingCount >= samplingCount;
    }

}
