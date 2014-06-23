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

package com.ternup.caddisfly.database;

import com.ternup.caddisfly.R;
import com.ternup.caddisfly.provider.TestContentProvider;
import com.ternup.caddisfly.util.FileUtils;
import com.ternup.caddisfly.util.PreferencesHelper;
import com.ternup.caddisfly.util.PreferencesUtils;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

import java.util.Date;

public class DataStorage {

    public static long saveResult(Context mContext, String folder, int testType, double result) {

        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(mContext);

        long locationId = sharedPreferences.getLong(mContext.getString(R.string.currentLocationId), -1);

        ContentValues values = new ContentValues();
        values.put(TestTable.COLUMN_FOLDER, folder);
        values.put(TestTable.COLUMN_DATE, (new Date().getTime()));
        values.put(TestTable.COLUMN_TYPE, testType);
        values.put(TestTable.COLUMN_RESULT, result);
        values.put(TestTable.COLUMN_LOCATION_ID, locationId);

        Uri uri = mContext.getContentResolver().insert(TestContentProvider.CONTENT_URI, values);
        long id = ContentUris.parseId(uri);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(mContext.getString(R.string.currentTestId), id);
        editor.commit();

        return id;
    }

    public static void deleteRecord(Context context,long id, long locationId, String folderName) {

        PreferencesUtils.removeKey(context, R.string.currentSamplingCountKey);

        FileUtils.deleteFolder(context, locationId, folderName);
        id = PreferencesHelper.getCurrentTestId(context, null, null);
        if (id > -1) {
            Uri uri = ContentUris.withAppendedId(TestContentProvider.CONTENT_URI, id);
            context.getContentResolver().delete(uri, null, null);

            PreferencesUtils.removeKey(context, PreferencesHelper.CURRENT_TEST_ID_KEY);

            id = -1;
        }
    }


}
