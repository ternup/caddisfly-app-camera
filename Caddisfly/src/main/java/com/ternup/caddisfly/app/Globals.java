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
public class Globals {

    public static final String DEFAULT_LOCALE = "en";

    // Link to product web site for about information
    public static final String PRODUCT_WEBSITE = "http://caddisfly.ternup.com";

    // Link to company web site for about information
    public static final String ORG_WEBSITE = "http://akvo.org";

    // Name of folder where app data will be stored
    public static final String APP_FOLDER_NAME = "com.ternup.caddisfly";

    // Tag for debug log filtering
    public static final String DEBUG_TAG = "Caddisfly";

    // Caddisfly update file name
    public static final String UPDATE_FILE_NAME = "caddisfly_update.apk";

    // Caddisfly update path
    public static final String UPDATE_URL = "http://caddisfly.ternup.com/ternupapp/v.txt?check=19";

    public static final String DATABASE_NAME = "caddisfly";

    public static final String PHOTO_TEMP_FILE = "cad";

    // new folder name using date
    public static final String FOLDER_NAME_DATE_FORMAT = "yyyyMMddHHmmss";

    // Index of screens that gets displayed in the app
    public static final int HOME_SCREEN_INDEX = 0;

    public static final int LOCATION_LIST_SCREEN_INDEX = 1;

    public static final int CALIBRATE_SCREEN_INDEX = 2;

    public static final int SETTINGS_SCREEN_INDEX = 3;

    public static final int HELP_SCREEN_INDEX = 4;

    public static final int ABOUT_SCREEN_INDEX = 5;

    // Index of test types
    public static final int FLUORIDE_INDEX = 0;

    public static final int PH_INDEX = 1;

    // width and height of cropped image
    public static final int IMAGE_SAMPLE_LENGTH = 600;

    public static final int INDEX_INCREMENT_STEP = 5;

    public static final String CALIBRATE_FOLDER = "calibrate";

    public static final String RESULT_SCREEN_TAG = "resultFragment";

    protected Globals() {
        // prevents calls from subclass
        throw new UnsupportedOperationException();
    }

}
