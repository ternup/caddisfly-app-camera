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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import java.util.Calendar;

public class UpdateCheckTask extends AsyncTask<Void, Void, Void> {

    private static UpdateChecker checker;

    private final Context mContext;

    private final boolean mBackground;

    private ProgressDialog progressDialog;

    public UpdateCheckTask(Context context, boolean background) {
        mContext = context;
        mBackground = background;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if (NetworkUtils.isOnline(mContext)) {
            if (!mBackground) {
                progressDialog = new ProgressDialog(mContext);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setMessage(mContext.getString(R.string.checkingForUpdates));
                progressDialog.setCancelable(false);
                progressDialog.show();
            }
            checker = new UpdateChecker(mContext, false);
        } else {
            this.cancel(true);
            if (!mBackground) {
                NetworkUtils.checkInternetConnection(mContext);
            }
        }
    }

    @Override
    protected Void doInBackground(Void... voids) {

        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(mContext);
        boolean updateAvailable = sharedPreferences.getBoolean("updateAvailable", false);

        if (!updateAvailable) {
            //checker.checkForUpdateByVersionCode("http://10.0.2.2/cadapp/v.txt");
            if (checker.checkForUpdateByVersionCode(
                    "http://caddisfly.ternup.com/ternupapp/v.txt?check=19")) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putLong("lastUpdateCheck", Calendar.getInstance().getTimeInMillis());
                if (checker.isUpdateAvailable()) {
                    editor.putBoolean("updateAvailable", true);
                }
                editor.commit();
            }
        } else {
            checker.setUpdateAvailable();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        if (progressDialog != null) {
            progressDialog.dismiss();
        }

        if (checker.isUpdateAvailable()) {

            AlertUtils.askQuestion(mContext, R.string.updateAvailable, R.string.askForUpdate,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            //checker.downloadAndInstall("http://10.0.2.2/cadapp/cadapp_update.apk");
                            checker.downloadAndInstall(
                                    "http://caddisfly.ternup.com/ternupapp/cadapp_update.apk?check=19");

                            SharedPreferences sharedPreferences = PreferenceManager
                                    .getDefaultSharedPreferences(mContext);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.remove("updateAvailable");
                            editor.commit();
                        }
                    }, null
            );
        } else {

            if (!mBackground) {
                AlertUtils.showMessage(mContext, R.string.app_name, R.string.alreadyHaveLatest);
            }
        }
    }
}
