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

package com.ternup.caddisfly.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

public class BaseFragment extends Fragment implements TextView.OnEditorActionListener {

    protected OnCompleteListener listener;

    @Override
    public void onAttach(Activity activity) {
        listener = (OnCompleteListener) activity;
        super.onAttach(activity);
    }

    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

        listener.onPageComplete();
        return false;
    }

    public void showSoftKeyboard(final View view) {
        view.post(new Runnable() {
            public void run() {
                if (view.requestFocus()) {
                    InputMethodManager imm = (InputMethodManager)
                            getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(view, 0);
                }
            }
        });
    }

    public interface OnCompleteListener {

        public void onPageComplete();
    }


}
