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
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;

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

        String photoFile = String.format("%s-%d-%d", Globals.PHOTO_TEMP_FILE, mTestType, mIndex);

        mLocationId = sharedPreferences.getLong(mContext.getString(R.string.currentLocationId), -1);
        photoFolder = FileUtils.getStoragePath(mContext, mLocationId, mFolderName, true);

        if (mFolderName.length() > 0) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(Globals.FOLDER_NAME_DATE_FORMAT,
                    Locale.US);
            photoFile = String.format("pic-%s", dateFormat.format(new Date()));
            pictureFile = new File(photoFolder + photoFile);
        } else {
            pictureFile = new File(
                    FileUtils.getStoragePath(mContext, -1, Globals.CALIBRATE_FOLDER, true)
                            + photoFile
            );
        }

        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(data);
            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (mFolderName.length() > 0) {
            File smallImageFolder = new File(photoFolder + "/small/");
            if (!smallImageFolder.exists()) {
                smallImageFolder.mkdirs();
            }

            Bitmap bitmap = ImageUtils.getAnalysedBitmap(pictureFile.getAbsolutePath());
            ImageUtils.saveBitmap(bitmap,
                    smallImageFolder.getAbsolutePath() + "/" + photoFile + "-s");
        }

        //Bitmap bitmap = Utility.decodeFile(pictureFile.getAbsolutePath());
        //bitmap = ThumbnailUtils.extractThumbnail(bitmap, 400, 400);

        Message msg = mHandler.obtainMessage();

        ArrayList<Integer> colorRange = ((MainApp) mContext).colorList;

        Bundle bundle;
        bundle = ColorUtils.getPpmValue(pictureFile.getAbsolutePath(), colorRange,
                ((MainApp) mContext).rangeIncrementValue,
                ((MainApp) mContext).rangeStartIncrement);

        long id = -1;
        if (mIndex < 1 && mFolderName.length() > 0) {
            id = saveResult(mFolderName, mTestType, bundle.getDouble("resultValue"));
            bundle.putLong(mContext.getString(R.string.currentTestId), id);

            int value = 0;
            int counter = 0;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            while (value != -1) {
                value = PreferencesUtils
                        .getInt(mContext, String.format("result_%d_%d", id, counter), -1);
                if (value > -1) {
                    editor.remove(String.format("result_%d_%d", id, counter));
                    counter++;
                }
            }
        }

        if (id == -1) {
            id = sharedPreferences.getLong(mContext.getString(R.string.currentTestId), -1);
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(String.format("result_%d_%d", id, mIndex), 100 - bundle.getInt("accuracy"));
        editor.commit();

        bundle.putInt("position", mIndex);
        bundle.putString(mContext.getString(R.string.folderName), mFolderName);
        bundle.putString("file", pictureFile.getAbsolutePath());
        bundle.putLong(mContext.getString(R.string.currentTestId), id);
        msg.setData(bundle);

        mHandler.sendMessage(msg);
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
}
