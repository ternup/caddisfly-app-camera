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

    public static int getTestTypeFromCode(String code) {

        if (("FLUOR").equals(code)) {
            return Globals.FLUORIDE_INDEX;
        } else if (("ALKAL").equals(code)) {
            return Globals.FLUORIDE_INDEX;
        } else if (("COLIF").equals(code)) {
            return Globals.BACTERIA_INDEX;
        } else if (("TURBI").equals(code)) {
            return Globals.FLUORIDE_INDEX;
        } else if (("NITRA").equals(code)) {
            return Globals.FLUORIDE_INDEX;
        } else if (("IRONA").equals(code)) {
            return Globals.FLUORIDE_INDEX;
        } else if (("ARSEN").equals(code)) {
            return Globals.FLUORIDE_INDEX;
        }

        return -1;
    }

    public static String getSwatchError(Context context, int errorCode) {
        switch (errorCode) {
            case Globals.ERROR_NOT_YET_CALIBRATED:
                return context.getString(R.string.notCalibrated);
            case Globals.ERROR_DUPLICATE_SWATCH:
                return context.getString(R.string.duplicateSwatch);
            case Globals.ERROR_SWATCH_OUT_OF_PLACE:
                return context.getString(R.string.outOfSequence);
            case Globals.ERROR_OUT_OF_RANGE:
                return context.getString(R.string.outOfRange);
            case Globals.ERROR_COLOR_IS_GRAY:
                return context.getString(R.string.outOfRange);
            case Globals.ERROR_LOW_QUALITY:
                return context.getString(R.string.photoQualityError);
            default:
                return context.getString(R.string.error);
        }
    }
}
