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

package com.ternup.caddisfly.model;

public class ColorInfo {

    final int mColor;

    final int mCount;

    final int mDominantCount;

    final int mQuality;

    public ColorInfo(int color, int count, int dominantCount, int quality) {
        mColor = color;
        mCount = count;
        mDominantCount = dominantCount;
        mQuality = quality;
    }

    public int getColor() {
        return mColor;
    }

    public int getCount() {
        return mCount;
    }

    public int getDominantCount() {
        return mDominantCount;
    }

    public int getQuality() {
        return mQuality;
    }
}