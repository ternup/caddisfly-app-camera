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
import com.ternup.caddisfly.app.MainApp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class AlertUtils {

    protected AlertUtils() {
        // prevents calls from subclass
        throw new UnsupportedOperationException();
    }

    public static void showMessage(Context context, int title, int message) {
        showAlert(context, title, message, null, null);
    }

    public static void askQuestion(Context context, int title, int message,
            DialogInterface.OnClickListener callback,
            DialogInterface.OnClickListener cancelListener) {
        showAlert(context, title, message, callback, cancelListener);
    }

    public static void askQuestion(Context context, int title, String message,
            DialogInterface.OnClickListener callback,
            DialogInterface.OnClickListener cancelListener) {
        showAlert(context, title, message, callback, cancelListener);
    }

    private static void showAlert(Context context, int title, int message,
            DialogInterface.OnClickListener callback,
            DialogInterface.OnClickListener cancelListener) {

        showAlert(context, title, context.getString(message), callback, cancelListener);
    }

    private static void showAlert(Context context, int title, String message,
            DialogInterface.OnClickListener callback,
            DialogInterface.OnClickListener cancelListener) {
        //if ( title == null ) title = context.getResources().getString(R.string.app_name);

        //TODO: remove this
        int iconId;
        assert context.getApplicationContext() != null;
        if (((MainApp) context.getApplicationContext()).CurrentTheme
                == R.style.AppTheme_Dark) {
            iconId = android.R.drawable.ic_dialog_alert;
        } else {
            iconId = R.drawable.ic_dialog_alert_light;
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(message)
                .setIcon(iconId)
                .setCancelable(false);

        if (callback != null) {
            builder.setPositiveButton(R.string.ok, callback);
        }

        if (cancelListener == null) {
            int buttonText = R.string.cancel;
            if (callback == null) {
                buttonText = R.string.ok;
            }
            builder.setNegativeButton(buttonText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
        } else {
            builder.setNegativeButton(R.string.cancel, cancelListener);
        }

        builder.create().show();
    }

    public static void showFutureFeature(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.futureFeature);
        builder.setMessage(R.string.featureNotImplemented);
        builder.setPositiveButton(R.string.ok, null);
        AlertDialog alert = builder.create();
        alert.show();
    }
}
