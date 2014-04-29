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
import android.view.Gravity;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;

public class MultiLineEditText extends FormWidget {

    protected EditText _input;

    public MultiLineEditText(Context context, String property) {
        super(context, property);

        _input = new EditText(context);
        _input.setTextSize(context.getResources().getDimension(R.dimen.textSize));
        _input.setMaxLines(8);
        _input.setMinLines(4);
        _input.setHorizontallyScrolling(false);
        _input.setVerticalScrollBarEnabled(true);

        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        params1.bottomMargin = 15;
        params1.topMargin = 0;
        //params1.height = 200;
        params1.gravity = Gravity.TOP;

        _input.setLines(4);

        _input.setLayoutParams(params1);
        _input.setSingleLine(false);
        _input.setGravity(Gravity.TOP);

        _input.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
        _input.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

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
