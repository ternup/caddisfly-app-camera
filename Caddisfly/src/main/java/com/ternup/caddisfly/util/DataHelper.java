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

package com.ternup.caddisfly.util;

import com.ternup.caddisfly.R;
import com.ternup.caddisfly.app.Globals;

import android.content.Context;

public class DataHelper {

    public static String getTestTitle(Context context, int testType) {
        switch (testType) {
            case Globals.FLUORIDE_INDEX:
                return context.getString(R.string.fluoride);
            case Globals.FLUORIDE_2_INDEX:
                return context.getString(R.string.fluoride2);
            case Globals.PH_INDEX:
                return context.getString(R.string.pH);
            case Globals.BACTERIA_INDEX:
                return context.getString(R.string.bacteria);
            default:
                return context.getString(R.string.fluoride);
        }

    }

}
