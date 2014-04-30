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

package com.ternup.caddisfly.widget;

import com.ternup.caddisfly.R;

import android.content.Context;
import android.text.InputType;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;

public class FormEditText extends FormWidget {

    public static final LinearLayout.LayoutParams
            defaultLayoutParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

    protected final EditText _input;


    public FormEditText(Context context, String property) {
        super(context, property);

        _input = new EditText(context);
        _input.setSingleLine(true);
        _input.setTextSize(context.getResources().getDimension(R.dimen.textSize));

        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        params1.bottomMargin = 15;
        params1.topMargin = 0;

        _input.setLayoutParams(params1);
        _input.setImeOptions(EditorInfo.IME_ACTION_NEXT);

        _input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        _layout.addView(_input);
    }

    @Override
    public String getValue() {
        return _input.getText().toString();
    }

    @Override
    public void setValue(String value) {
        _input.setText(value);
    }

    @Override
    public void setHint(String value) {
        _input.setHint(value);
    }

    public EditText getEditText() {
        return _input;
    }
}
