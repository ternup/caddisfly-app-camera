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

package com.ternup.caddisfly.provider;

import com.ternup.caddisfly.database.LocationTable;
import com.ternup.caddisfly.database.TestDatabaseHelper;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class LocationContentProvider extends ContentProvider {

    // used for the UriMatcher
    private static final int TESTS = 10;

    private static final int TEST_ID = 20;

    private static final String AUTHORITY = "com.ternup.caddisfly.locationProvider";

    private static final String BASE_PATH = "location";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
            + "/" + BASE_PATH);

    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        URI_MATCHER.addURI(AUTHORITY, BASE_PATH, TESTS);
        URI_MATCHER.addURI(AUTHORITY, BASE_PATH + "/#", TEST_ID);
    }

    // database
    private TestDatabaseHelper database;

    @Override
    public boolean onCreate() {
        database = new TestDatabaseHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {

        // Using SQLiteQueryBuilder instead of query() method
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        // check if the caller has requested a column which does not exists
        //checkColumns(projection);

        // Set the table
        queryBuilder.setTables(LocationTable.TABLE_LOCATION);

        int uriType = URI_MATCHER.match(uri);
        switch (uriType) {
            case TESTS:
                break;
            case TEST_ID:
                // adding the ID to the original query
                queryBuilder.appendWhere(LocationTable.COLUMN_ID + "="
                        + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = database.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection,
                selectionArgs, null, null, sortOrder);
        // make sure that potential listeners are getting notified
        assert cursor != null;
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = URI_MATCHER.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        long id;
        switch (uriType) {
            case TESTS:
                assert sqlDB != null;
                id = sqlDB.insert(LocationTable.TABLE_LOCATION, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(BASE_PATH + "/" + id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = URI_MATCHER.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        assert sqlDB != null;
        int rowsDeleted;
        switch (uriType) {
            case TESTS:
                rowsDeleted = sqlDB.delete(LocationTable.TABLE_LOCATION, selection,
                        selectionArgs);
                break;
            case TEST_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(LocationTable.TABLE_LOCATION,
                            LocationTable.COLUMN_ID + "=" + id,
                            null);
                } else {
                    rowsDeleted = sqlDB.delete(LocationTable.TABLE_LOCATION,
                            LocationTable.COLUMN_ID + "=" + id
                                    + " and " + selection,
                            selectionArgs
                    );
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {

        int uriType = URI_MATCHER.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        assert sqlDB != null;
        int rowsUpdated;
        switch (uriType) {
            case TESTS:
                rowsUpdated = sqlDB.update(LocationTable.TABLE_LOCATION,
                        values,
                        selection,
                        selectionArgs);
                break;
            case TEST_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = sqlDB.update(LocationTable.TABLE_LOCATION,
                            values,
                            LocationTable.COLUMN_ID + "=" + id,
                            null);
                } else {
                    rowsUpdated = sqlDB.update(LocationTable.TABLE_LOCATION,
                            values,
                            LocationTable.COLUMN_ID + "=" + id
                                    + " and "
                                    + selection,
                            selectionArgs
                    );
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }
}
