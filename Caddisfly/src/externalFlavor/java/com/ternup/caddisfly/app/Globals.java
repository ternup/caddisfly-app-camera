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

    // For external app connection
    public static final String ACTION_WATER_TEST = "org.akvo.flow.action.externalsource"; //NON-NLS

    // Caddisfly update file name
    public static final String UPDATE_FILE_NAME = "akvo_caddisfly_update.apk";

    // Caddisfly update check path
    public static final String UPDATE_CHECK_URL
            = "http://caddisfly.ternup.com/akvoapp/v.txt?check=30";

    // Caddisfly update path
    public static final String UPDATE_URL
            = "http://caddisfly.ternup.com/akvoapp/akvo_caddisfly_update.apk?check=30";

    public static final String FLOW_SURVEY_PACKAGE_NAME = "com.gallatinsystems.survey.device";

    protected Globals() {
        // prevents calls from subclass
        throw new UnsupportedOperationException();
    }

}
