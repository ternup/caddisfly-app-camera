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
public class GlobalsBase {

    public static final String DEFAULT_LOCALE = "en";

    // Link to product web site for about information
    public static final String PRODUCT_WEBSITE = "http://caddisfly.ternup.com";

    // Link to company web site for about information
    public static final String ORG_WEBSITE = "http://akvo.org";

    // Name of folder where app data will be stored
    public static final String APP_FOLDER_NAME = "com.ternup.caddisfly";

    // Tag for debug log filtering
    public static final String DEBUG_TAG = "Caddisfly";

    // new folder name using date
    public static final String FOLDER_NAME_DATE_FORMAT = "yyyyMMddHHmmss";

    // Index of screens that gets displayed in the app
    public static final int HOME_SCREEN_INDEX = 0;

    public static final int CALIBRATE_SCREEN_INDEX = 2;

    public static final int HELP_SCREEN_INDEX = 4;

    // Index of test types
    public static final int FLUORIDE_INDEX = 0;

    public static final int FLUORIDE_2_INDEX = 1;

    public static final int PH_INDEX = 2;

    public static final int BACTERIA_INDEX = 3;

    // width and height of cropped image
    public static final int IMAGE_CROP_LENGTH = 300;

    // folder for calibration photos
    public static final String CALIBRATE_FOLDER = "calibrate";

    // safety ranges for fluoride
    public static final double FLUORIDE_MAX_DRINK = 1.0;

    public static final double FLUORIDE_MAX_COOK = 1.5;

    public static final double FLUORIDE_MAX_BATHE = 2.5;

    public static final int MINUTE_IN_MS = 60000;

    public static final int INITIAL_DELAY = 4000;

    public static final int MINIMUM_PHOTO_QUALITY = 50;

    public static final int SAMPLING_COUNT_DEFAULT = 5;

    // width and height of cropped image
    public static final int SAMPLE_CROP_LENGTH_DEFAULT = 150;

    public static final String RESULT_VALUE_KEY = "resultValue";

    public static final String RESULT_COLOR_KEY = "resultColor";

    public static final String QUALITY_KEY = "accuracy";
    public static final int ERROR_NOT_YET_CALIBRATED = 1;
    public static final int ERROR_LOW_QUALITY = 2;
    public static final int ERROR_DUPLICATE_SWATCH = 3;
    public static final int ERROR_SWATCH_OUT_OF_PLACE = 4;
    public static final int ERROR_OUT_OF_RANGE = 5;
    public static final int ERROR_COLOR_IS_GRAY = 6;

    protected GlobalsBase() {
        // prevents calls from subclass
        throw new UnsupportedOperationException();
    }

}
