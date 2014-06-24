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

/*
http://www.lukehorvat.com/blog/android-numberpickerdialogpreference/
*/
package com.ternup.caddisfly.component;

import com.ternup.caddisfly.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

/**
 * A {@link DialogPreference} that provides a user with the means to select an integer from a
 * {@link
 * NumberPicker}, and persist it.
 *
 * @author lukehorvat
 */
public class NumberPickerPreference extends DialogPreference {

    private static final int DEFAULT_MIN_VALUE = 0;

    private static final int DEFAULT_MAX_VALUE = 100;

    private static final int DEFAULT_VALUE = 0;

    private static final int DEFAULT_INTERVAL_VALUE = 1;

    private int mMinValue;

    private int mMaxValue;

    private int mValue;

    private int mInterval;

    private NumberPicker mNumberPicker;

    private String mSummary;

    private String[] displayedValues;

    public NumberPickerPreference(Context context) {
        this(context, null);
    }

    public NumberPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        // get attributes specified in XML
        TypedArray a = context.getTheme()
                .obtainStyledAttributes(attrs, R.styleable.NumberPickerPreference, 0, 0);
        try {
            setMinValue(a.getInteger(R.styleable.NumberPickerPreference_min, DEFAULT_MIN_VALUE));
            setMaxValue(a.getInteger(R.styleable.NumberPickerPreference_android_max,
                    DEFAULT_MAX_VALUE));
            setIntervalValue(a.getInteger(R.styleable.NumberPickerPreference_interval,
                    DEFAULT_INTERVAL_VALUE));
            mSummary = a.getString(R.styleable.NumberPickerPreference_android_summary);

        } finally {
            assert a != null;
            a.recycle();
        }

        // set layout
        setDialogLayoutResource(R.layout.preference_numberpicker);
        setPositiveButtonText(R.string.ok);
        setNegativeButtonText(R.string.cancel);
        setDialogIcon(null);
    }

    @Override
    protected void onSetInitialValue(boolean restore, Object defaultValue) {
        setValue(restore ? getPersistedInt(DEFAULT_VALUE) : (Integer) defaultValue);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, DEFAULT_VALUE);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        TextView dialogMessageText = (TextView) view.findViewById(R.id.text_dialog_message);
        dialogMessageText.setText(getDialogMessage());

        mNumberPicker = (NumberPicker) view.findViewById(R.id.number_picker);

        int count = ((mMaxValue - mMinValue) / mInterval) + 1;

        displayedValues = new String[count];
        for (int i = 0; i < count; i++) {
            displayedValues[i] = String.format("%d", mMinValue + (i * mInterval));
        }

        mNumberPicker.setDisplayedValues(displayedValues);
        mNumberPicker.setMinValue(0);
        mNumberPicker.setMaxValue(count - 1);
        mNumberPicker.setValue((mValue / mInterval) - (mMinValue / mInterval));

        EditText input = findInput(mNumberPicker);
        input.setInputType(InputType.TYPE_CLASS_PHONE);

    }

    int getMinValue() {
        return mMinValue;
    }

    void setMinValue(int minValue) {
        mMinValue = minValue;
        setValue(Math.max(mValue, mMinValue));
    }

    int getMaxValue() {
        return mMaxValue;
    }

    void setMaxValue(int maxValue) {
        mMaxValue = maxValue;
        setValue(Math.min(mValue, mMaxValue));
    }

    void setIntervalValue(int value) {
        mInterval = value;
    }

    int getValue() {
        return mValue;
    }

    void setValue(int value) {
        value = Math.max(Math.min(value, mMaxValue), mMinValue);

        if (value != mValue) {
            mValue = value;
            persistInt(value);
            notifyChanged();
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        // when the user selects "OK", persist the new value
        if (positiveResult) {
            int numberPickerValue = Integer.parseInt(displayedValues[mNumberPicker.getValue()]);
            if (callChangeListener(numberPickerValue)) {
                setValue(numberPickerValue);
            }
        }
    }

    @Override
    public CharSequence getSummary() {
        String summary = String.format("%d", mValue);
        if (mSummary != null) {
            summary = String.format("%s %s", summary, mSummary);
        }

        return summary;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        // save the instance state so that it will survive screen orientation changes and other events that may temporarily destroy it
        final Parcelable superState = super.onSaveInstanceState();

        // set the state's value with the class member that holds current setting value
        final SavedState myState = new SavedState(superState);
        myState.minValue = getMinValue();
        myState.maxValue = getMaxValue();
        myState.value = getValue();

        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        // check whether we saved the state in onSaveInstanceState()
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // didn't save the state, so call superclass
            super.onRestoreInstanceState(state);
            return;
        }

        // restore the state
        SavedState myState = (SavedState) state;
        setMinValue(myState.minValue);
        setMaxValue(myState.maxValue);
        setValue(myState.value);

        super.onRestoreInstanceState(myState.getSuperState());
    }

    private EditText findInput(ViewGroup np) {
        int count = np.getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = np.getChildAt(i);
            if (child instanceof ViewGroup) {
                findInput((ViewGroup) child);
            } else if (child instanceof EditText) {
                return (EditText) child;
            }
        }
        return null;
    }

    private static class SavedState extends BaseSavedState {

        @SuppressWarnings("unused")
        public static final Parcelable.Creator<SavedState> CREATOR
                = new Parcelable.Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };

        int minValue;

        int maxValue;

        int value;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public SavedState(Parcel source) {
            super(source);

            minValue = source.readInt();
            maxValue = source.readInt();
            value = source.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);

            dest.writeInt(minValue);
            dest.writeInt(maxValue);
            dest.writeInt(value);
        }

    }
}
