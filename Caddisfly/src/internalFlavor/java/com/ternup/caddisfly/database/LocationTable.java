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

import com.ternup.caddisfly.app.MainApp;

import android.database.sqlite.SQLiteDatabase;

public class LocationTable {

    // Database table
    public static final String TABLE_LOCATION = "location";

    public static final String COLUMN_ID = "_id";

    public static final String COLUMN_DATE = "date";

    public static final String COLUMN_LONGITUDE = "longitude";

    public static final String COLUMN_LATITUDE = "latitude";

    public static final String COLUMN_ACCURACY = MainApp.QUALITY_KEY;

    public static final String COLUMN_NAME = "name";

    public static final String COLUMN_STREET = "street";

    public static final String COLUMN_TOWN = "town";

    public static final String COLUMN_CITY = "city";

    public static final String COLUMN_STATE = "state";

    public static final String COLUMN_COUNTRY = "country";

    public static final String COLUMN_SOURCE = "source";

    public static final String COLUMN_NOTES = "notes";

    public static final String COLUMN_SENT = "sent";

    // Database creation SQL statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_LOCATION
            + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_DATE + " long not null, "
            + COLUMN_NAME + " text, "
            + COLUMN_LONGITUDE + " double, "
            + COLUMN_LATITUDE + " double, "
            + COLUMN_ACCURACY + " double, "
            + COLUMN_STREET + " text, "
            + COLUMN_TOWN + " town, "
            + COLUMN_CITY + " text, "
            + COLUMN_STATE + " text, "
            + COLUMN_COUNTRY + " text, "
            + COLUMN_NOTES + " text, "
            + COLUMN_SOURCE + " int, "
            + COLUMN_SENT + " flag boolean"
            + ");";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database) {
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATION);
        onCreate(database);
    }
}
