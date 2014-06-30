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

import android.content.Context;
import android.content.res.Resources;

/**
 *
 */
public class TimeUtils {

    /**
     * One second (in milliseconds)
     */
    private static final int _A_SECOND = 1000;

    /**
     * One minute (in milliseconds)
     */
    private static final int _A_MINUTE = 60 * _A_SECOND;

    /**
     * One hour (in milliseconds)
     */
    private static final int _AN_HOUR = 60 * _A_MINUTE;

    /**
     * One day (in milliseconds)
     */
    private static final int _A_DAY = 24 * _AN_HOUR;

    public static String getTimeAgo(long time, Context context) {
        if (time < 1000000000000L)
        // if timestamp given in seconds, convert to millis
        {
            time *= 1000;
        }

        final long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return "";
        }

        final Resources res = context.getResources();
        final long time_difference = now - time;

        int tempTime;
        if (time_difference < _A_MINUTE) {
            return res.getString(R.string.just_now);
        } else if (time_difference < 60 * _A_MINUTE) {
            tempTime = (int) time_difference / _A_MINUTE;
            return res.getQuantityString(R.plurals.minutes, tempTime, tempTime);
        } else if (time_difference < 24 * _AN_HOUR) {
            tempTime = (int) time_difference / _AN_HOUR;
            return res.getQuantityString(R.plurals.hours, tempTime, tempTime);
        } else if (time_difference < 48 * _AN_HOUR) {
            return res.getString(R.string.yesterday);
        } else {
            tempTime = (int) time_difference / _A_DAY;
            return res.getQuantityString(R.plurals.days, tempTime, tempTime);
        }
    }
}
