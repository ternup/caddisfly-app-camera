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

import com.ternup.caddisfly.app.Globals;

import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;
import java.io.IOException;

public class UpgradeCheckTask extends AsyncTask<Void, Void, Void> {

    public UpgradeCheckTask() {
    }

    @Override
    protected Void doInBackground(Void... voids) {

        File sdDir = Environment.getExternalStorageDirectory();

        File appDir = new File(sdDir, Globals.APP_FOLDER_NAME);

        if (appDir.exists()) {

            Runtime runtime = Runtime.getRuntime();
            try {
                runtime.exec("rm -r " + appDir.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
