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

import android.content.Context;

import java.io.File;

public class FileUtils {

    private FileUtils() {
    }

    public static String getStoragePath(Context context, long locationId, String folderName,
            boolean create) {

        if (folderName != null && folderName.contains(File.separator)) {
            return folderName;
        }

        if (locationId > -1) {
            assert folderName != null;
            if (!folderName.isEmpty()) {
                folderName = locationId + File.separator + folderName;
            } else {
                folderName = String.valueOf(locationId);
            }
        }

        //File sdDir = Environment.getExternalStorageDirectory();

        File sdDir = context.getExternalFilesDir(null);

        File appDir = new File(sdDir, folderName);

        if (!appDir.exists()) {
            if (!create) {
                return "";
            }
            if (!appDir.mkdirs()) {
                return "";
            }
        }

        return appDir.getPath() + File.separator;
    }

}
