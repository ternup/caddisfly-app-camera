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

package com.ternup.caddisfly.component;

import android.content.Context;
import android.content.DialogInterface;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.ternup.caddisfly.R;
import com.ternup.caddisfly.util.AlertUtils;
import com.ternup.caddisfly.util.PreferencesUtils;
import com.ternup.caddisfly.util.UpdateCheckTask;

public class ConfirmPreference extends CheckBoxPreference {

    public ConfirmPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        //if (!PreferencesUtils.getBoolean(getContext(), R.string.revertVersionKey, false)) {

        //}
        //setTitle("Revert to version " + MainApp.getPreviousVersion(getContext()));
        return super.onCreateView(parent);
    }

    @Override
    protected void onClick() {
        if (!PreferencesUtils.getBoolean(getContext(), R.string.revertVersionKey, false)) {
            AlertUtils.askQuestion(getContext(),
                    R.string.appVersion,
                    R.string.revertVersion,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            PreferencesUtils
                                    .setBoolean(getContext(), R.string.revertVersionKey, true);
                            setChecked(true);
                            UpdateCheckTask updateCheckTask = new UpdateCheckTask(getContext(), false, true);
                            updateCheckTask.execute();
                        }
                    }, null
            );
        } else {
            PreferencesUtils.setBoolean(getContext(), R.string.revertVersionKey, false);
            setChecked(false);
            UpdateCheckTask updateCheckTask = new UpdateCheckTask(getContext(), false, false);
            updateCheckTask.execute();
        }
    }
}
