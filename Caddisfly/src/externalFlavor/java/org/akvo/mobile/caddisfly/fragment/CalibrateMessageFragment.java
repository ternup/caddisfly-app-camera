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

package org.akvo.mobile.caddisfly.fragment;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.ternup.caddisfly.R;

public class CalibrateMessageFragment extends DialogFragment {

    public CalibrateMessageFragment() {
    }

    public static CalibrateMessageFragment newInstance() {
        return new CalibrateMessageFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_calibrate_message, container, false);
        Button button = (Button) view.findViewById(R.id.endSurveyButton);
        getDialog().setTitle(R.string.welcome);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ResultDialogListener listener = (ResultDialogListener) getActivity();
                listener.onFinishDialog();
                getDialog().dismiss();
            }
        });

        return view;
    }

    public interface ResultDialogListener {
        void onFinishDialog();
    }
}
