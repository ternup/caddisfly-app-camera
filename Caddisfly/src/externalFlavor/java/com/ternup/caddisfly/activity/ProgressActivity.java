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

package com.ternup.caddisfly.activity;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;

import com.ternup.caddisfly.R;
import com.ternup.caddisfly.app.Globals;
import com.ternup.caddisfly.util.DataHelper;
import com.ternup.caddisfly.util.PreferencesUtils;

import org.akvo.mobile.caddisfly.activity.MainActivity;
import org.akvo.mobile.caddisfly.fragment.ResultFragment;

public class ProgressActivity extends ProgressActivityBase implements ResultFragment.ResultDialogListener {

    ResultFragment mResultFragment;

    @Override
    protected void startHomeActivity(Context context) {
        super.startHomeActivity(context);
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);

        finish();
    }

    @Override
    protected void sendResult(final Message msg) {

        if (mFolderName != null && !mFolderName.isEmpty()) {
            if (msg != null && msg.getData() != null) {

                final double result = msg.getData().getDouble(Globals.RESULT_VALUE_KEY, -1);
                final int quality = msg.getData().getInt(Globals.QUALITY_KEY, 0);

                int minAccuracy = PreferencesUtils
                        .getInt(this, R.string.minPhotoQualityKey, Globals.MINIMUM_PHOTO_QUALITY);

                String title = DataHelper.getTestTitle(this, mTestType);

                if (result >= 0 && quality >= minAccuracy) {
                    mResultFragment = ResultFragment.newInstance(title, result, msg);
                    final FragmentTransaction ft = getFragmentManager().beginTransaction();

                    Fragment prev = getFragmentManager().findFragmentByTag("resultDialog");
                    if (prev != null) {
                        ft.remove(prev);
                    }
                    mResultFragment.show(ft, "resultDialog");
                }
            } else {
                super.sendResult(msg);
            }
        }else {
            super.sendResult(msg);
        }
    }

    @Override
    public void onFinishDialog(Bundle bundle) {
        Message msg = new Message();
        msg.setData(bundle);
        super.sendResult(msg);
    }
}
