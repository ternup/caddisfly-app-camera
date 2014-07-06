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

package com.ternup.caddisfly.app;

@SuppressWarnings("HardCodedStringLiteral")
public class Globals extends GlobalsBase {

    // Caddisfly update file name
    public static final String UPDATE_FILE_NAME = "caddisfly_update.apk";

    // Caddisfly update check path
    public static final String UPDATE_CHECK_URL
            = "http://caddisfly.ternup.com/ternupapp/v.txt?check=29";

    // Caddisfly update path
    public static final String UPDATE_URL
            = "http://caddisfly.ternup.com/ternupapp/cadapp_update.apk?check=29";

    //TODO: remove this
    public static final String CONNECT = "";

    public static final String SERVER_BASE_URL = "http://labyrinth-punter.rhcloud.com/testlog/api/";

    public static final String DATABASE_NAME = "caddisfly";

    // Index of screens that gets displayed in the app
    public static final int LOCATION_LIST_SCREEN_INDEX = 1;

    public static final int SETTINGS_SCREEN_INDEX = 3;

    public static final int ABOUT_SCREEN_INDEX = 5;

    public static final String RESULT_SCREEN_TAG = "resultFragment";

    protected Globals() {
        // prevents calls from subclass
        throw new UnsupportedOperationException();
    }

}
