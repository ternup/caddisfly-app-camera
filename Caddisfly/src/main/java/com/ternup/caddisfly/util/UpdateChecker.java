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

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Raghav Sood
 * @version API 2
 * @since API 1
 */

class UpdateChecker {

    private final Context mContext;

    private boolean updateAvailable = false;

    private boolean haveValidContext = false;

    private boolean useToasts = false;

    private DownloadManager downloadManager;

    /**
     * Constructor for UpdateChecker
     * <p/>
     * Example call:
     * UpdateChecker updateChecker = new UpdateChecker(this, false);
     *
     * @param context An instance of your Activity's context
     * @param toasts  True if you want toast notifications, false by default
     * @since API 2
     */
    public UpdateChecker(Context context, boolean toasts) {
        mContext = context;
        if (mContext != null) {
            haveValidContext = true;
            useToasts = toasts;
        }
    }

    /**
     * Checks for app update by version code.
     * <p/>
     * Example call:
     * updateChecker.checkForUpdateByVersionCode("http://www.example.com/version.txt");
     *
     * @param url URL at which the text file containing your latest version code is located.
     * @since API 1
     */
    @SuppressWarnings("SameParameterValue")
    public boolean checkForUpdateByVersionCode(String url) {

        if (NetworkUtils.checkInternetConnection(mContext)) {
            if (haveValidContext) {
                int versionCode = getVersionCode();
                int readCode;
                if (versionCode >= 0) {
                    try {
                        readCode = Integer.parseInt(readFile(url));
                        // Check if update is available.
                        if (readCode > versionCode) {
                            updateAvailable = true;
                        }
                    } catch (NumberFormatException e) {
                        Log.e(Globals.DEBUG_TAG,
                                "Invalid number online");
                    }

                } else {
                    Log.e(Globals.DEBUG_TAG, "Invalid version code in app");
                }
                return true;
            }
            return false;
        } else {
            if (useToasts) {
                makeToastFromString("Update check failed. No internet connection available")
                        .show();
            }
            return false;
        }
    }

    /**
     * Gets the version code of your app by the context passed in the constructor
     *
     * @return The version code if successful, -1 if not
     * @since API 1
     */
    int getVersionCode() {
        int code;
        try {
            code = mContext.getPackageManager()
                    .getPackageInfo(mContext.getPackageName(), 0).versionCode;
            return code; // Found the code!
        } catch (Exception e) {
            Log.e(Globals.DEBUG_TAG, "Version Code not available");
        }

        return -1; // There was a problem.
    }

    /**
     * Downloads and installs the update apk from the URL
     *
     * @param apkUrl URL at which the update is located
     * @since API 1
     */
    @SuppressWarnings("SameParameterValue")
    public void downloadAndInstall(String apkUrl) {
        if (NetworkUtils.isOnline(mContext)) {
            downloadManager = new DownloadManager(mContext, true);
            downloadManager.execute(apkUrl);
        } else {
            makeToastFromString("Update failed. No internet connection available").show();
        }
    }

    public void setUpdateAvailable() {
        updateAvailable = true;
    }

    /**
     * Should be called after checkForUpdateByVersionCode()
     *
     * @return Returns true if an update is available, false if not.
     * @since API 1
     */
    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    /**
     * Makes a toast message with a short duration from the given text.
     *
     * @param text The text to be displayed by the toast
     * @return The toast object.
     * @since API 2
     */
    @SuppressLint("ShowToast")
    Toast makeToastFromString(String text) {
        return Toast.makeText(mContext, text, Toast.LENGTH_SHORT);
    }

    /**
     * Reads a file at the given URL
     *
     * @param url The URL at which the file is located
     * @return Returns the content of the file if successful
     * @since API 1
     */

    //http://developer.android.com/reference/java/net/URLConnection.html#setConnectTimeout(int)
    String readFile(String url) {
        String result;
        InputStream inputStream;
        try {
            inputStream = new URL(url).openStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            result = bufferedReader.readLine();
            return result;
        } catch (MalformedURLException e) {
            Log.e(Globals.DEBUG_TAG, "Invalid URL");
        } catch (IOException e) {
            Log.e(Globals.DEBUG_TAG, "IO exception");
        } catch (Exception e) {
            Log.e(Globals.DEBUG_TAG, e.getMessage());
        }

        Log.e(Globals.DEBUG_TAG, "File read error");
        return "File read error";
    }

    public void cancel() {
        if (downloadManager != null) {
            downloadManager.stop();
        }
    }

}
