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

import com.ternup.caddisfly.util.UpgradeCheckTask;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TestDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "caddisfly";

    private static final int DATABASE_VERSION = 5;

    //private final Context mContext;

    public TestDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        //mContext = context;
    }

    // Method is called during creation of the database
    @Override
    public void onCreate(SQLiteDatabase database) {
        TestTable.onCreate(database);
        LocationTable.onCreate(database);

        importFolderData();
    }

    // Method is called during an upgrade of the database,
    // e.g. if you increase the database version
    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion,
            int newVersion) {
        TestTable.onUpgrade(database);
        LocationTable.onUpgrade(database);

        importFolderData();
    }

    private void importFolderData() {
        UpgradeCheckTask updateCheckTask = new UpgradeCheckTask();
        updateCheckTask.execute();
    }
}
