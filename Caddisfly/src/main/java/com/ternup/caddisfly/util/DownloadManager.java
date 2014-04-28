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
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * DO NOT USE ON YOUR OWN. All calls are handled through UpdateChecker.
 *
 * @author Raghav Sood
 * @version API 2
 * @since API 1
 */
class DownloadManager extends AsyncTask<String, Integer, String> {

    private static final int BYTE_ARRAY_LENGTH = 1024;

    private final Context mContext;

    private ProgressDialog progressDialog;

    private boolean installAfterDownload = true;

    private boolean downloaded = false;

    /**
     * Constructor for the Download Manager. DO NOT USE ON YOUR OWN. All calls are through
     * UpdateChecker
     *
     * @param context Activity context
     * @since API 1
     */
    public DownloadManager(Context context) {
        this(context, true);
    }

    /**
     * Constructor for the download manager. DO NOT USE ON YOUR OWN. All calls are handled through
     * UpdateChecker.
     *
     * @param context              Activity context
     * @param installAfterDownload true if you want to install immediately after download, false
     *                             otherwise
     * @since API 2
     */
    public DownloadManager(Context context, boolean installAfterDownload) {
        mContext = context;
        this.installAfterDownload = installAfterDownload;
    }

    /**
     * Checks to see if we have an active internet connection
     *
     * @return true if online, false otherwise
     * @since API 1
     */
    private boolean isOnline() {
        try {
            ConnectivityManager cm = (ConnectivityManager) mContext
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            return cm.getActiveNetworkInfo().isConnectedOrConnecting();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Downloads the update file in a background task
     *
     * @since API 1
     */
    @Override
    protected String doInBackground(String... sUrl) {
        if (isOnline()) {
            try {
                URL url = new URL(sUrl[0]);
                URLConnection connection = url.openConnection();
                connection.connect();
                // this will be useful so that you can show a typical 0-100% progress bar
                //int fileLength = connection.getContentLength();

                File oldFile = new File(Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        .getPath()
                        + "/caddisfly_update.apk");
                if (oldFile.exists()) {
                    oldFile.delete();
                }

                // download the file
                InputStream input = new BufferedInputStream(url.openStream());
                OutputStream output =
                        new FileOutputStream(Environment
                                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                                .getPath()
                                + "/caddisfly_update.apk");
                try {
                    byte[] data = new byte[BYTE_ARRAY_LENGTH];
                    //long total = 0;
                    int count;
                    while ((count = input.read(data)) != -1) {
                        //total += count;
                        // publishing the progress....
                        //publishProgress((int) (total * 100 / fileLength));
                        output.write(data, 0, count);

                        if (isCancelled()) {
                            if (progressDialog != null) {
                                if (progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }
                                progressDialog = null;
                            }
                            return null;
                        }
                    }
                } finally {
                    output.flush();
                    output.close();
                    input.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Updates our progress bar with the download information
     *
     * @since API 1
     */
/*
    @Override
    protected void onProgressUpdate(Integer... changed) {
        progressDialog.setProgress(changed[0]);
    }
*/

    /**
     * Sets up the progress dialog to notify user of download progress
     *
     * @since API 1
     */
    @Override
    protected void onPreExecute() {
        progressDialog = new ProgressDialog(mContext);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(mContext.getString(R.string.gettingUpdate));
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    /**
     * Dismissed progress dialog, calls install() if installAfterDownload is true
     *
     * @since API 1
     */
    @Override
    protected void onPostExecute(String result) {
        if (progressDialog != null) {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
        downloaded = true;
        if (installAfterDownload) {
            install();
        }
    }

    /**
     * Launches an Intent to install the apk update.
     *
     * @since API 2
     */
    public void install() {
        if (downloaded) {
            String filePath =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                            .getPath() + "/caddisfly_update.apk";
            Uri fileLoc = Uri.fromFile(new File(filePath));
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(fileLoc, "application/vnd.android.package-archive");
            mContext.startActivity(intent);
        }
    }

    public void stop() {
        if (progressDialog != null) {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            progressDialog = null;
        }

    }

}
