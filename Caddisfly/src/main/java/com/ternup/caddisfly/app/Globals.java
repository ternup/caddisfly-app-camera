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

public class Globals {

    // Link to product web site for about information
    public static final String PRODUCT_WEBSITE = "http://caddisfly.ternup.com";

    // Link to company web site for about information
    public static final String ORG_WEBSITE = "http://akvo.org";

    // Name of folder where app data will be stored
    public static final String APP_FOLDER_NAME = "com.ternup.caddisfly";

    // Tag for debug log filtering
    public static final String DEBUG_TAG = "Caddisfly";

    // Index of screens that gets displayed in the app
    public static final int HOME_SCREEN_INDEX = 0;

    public static final int HISTORY_SCREEN_INDEX = 1;

    public static final int CALIBRATE_SCREEN_INDEX = 2;

    public static final int SETTINGS_SCREEN_INDEX = 3;

    public static final int HELP_SCREEN_INDEX = 4;

    public static final int ABOUT_SCREEN_INDEX = 5;

    // Index of test types
    public static final int FLUORIDE_INDEX = 0;

    public static final int BACTERIA_INDEX = 1;

    public static final int PH_INDEX = 2;

    protected Globals() {
        // prevents calls from subclass
        throw new UnsupportedOperationException();
    }

}
