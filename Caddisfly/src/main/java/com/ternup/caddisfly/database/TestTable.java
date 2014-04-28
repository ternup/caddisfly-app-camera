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

import android.database.sqlite.SQLiteDatabase;

public class TestTable {

    // Database table
    public static final String TABLE_TEST = "test";

    public static final String COLUMN_ID = "_id";

    public static final String COLUMN_LOCATION_ID = "locationId";

    public static final String COLUMN_DATE = "date";

    public static final String COLUMN_FOLDER = "folder";

    public static final String COLUMN_TYPE = "type";

    public static final String COLUMN_RESULT = "result";

    public static final String COLUMN_SENT = "sent";

    // Database creation SQL statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_TEST
            + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_LOCATION_ID + " integer, "
            + COLUMN_DATE + " long not null, "
            + COLUMN_FOLDER + " text not null, "
            + COLUMN_TYPE + " int not null, "
            + COLUMN_RESULT + " double not null, "
            + COLUMN_SENT + " flag boolean"
            + ");";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database) {
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_TEST);
        onCreate(database);
    }
}
