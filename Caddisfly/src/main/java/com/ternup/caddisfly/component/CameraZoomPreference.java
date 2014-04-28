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

import com.ternup.caddisfly.R;
import com.ternup.caddisfly.util.ShowCamera;

import android.content.Context;
import android.content.res.TypedArray;
import android.hardware.Camera;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SeekBar;

public class CameraZoomPreference extends DialogPreference {

    private ShowCamera showCamera;

    private SeekBar mSeekBar;

    private int mValue;

    public CameraZoomPreference(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
    }

    public CameraZoomPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        // set layout
        setDialogLayoutResource(R.layout.activity_camera_zoom);
        setPositiveButtonText(R.string.ok);
        setNegativeButtonText(R.string.cancel);
        setDialogIcon(null);
    }

    private static Camera getCamera() {
        try {
            return Camera.open();
        } catch (Exception e) {
            return null;
        }
    }

    int getValue() {
        return mValue;
    }

    void setValue(int value) {
        if (value != mValue) {
            mValue = value;
            persistInt(value);
            notifyChanged();
        }
    }

    @Override
    protected void onSetInitialValue(boolean restore, Object defaultValue) {
        setValue(restore ? getPersistedInt(100) : (Integer) defaultValue);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, 100);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        Camera camera = getCamera();
        showCamera = new ShowCamera(getContext(), camera);
        FrameLayout preview = (FrameLayout) view.findViewById(R.id.camera_preview);
        preview.addView(showCamera);

        mSeekBar = (SeekBar) view.findViewById(R.id.zoomSeekBar);
        mSeekBar.setMax(showCamera.getMaxZoom());
        mSeekBar.setProgress(mValue);

        if (mValue == -1) {
            showCamera.setMaxZoom();
        } else {
            showCamera.setZoom(mValue);
        }

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                showCamera.setZoom(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        // when the user selects "OK", persist the new value
        if (positiveResult) {
            int value = mSeekBar.getProgress();
            if (callChangeListener(value)) {
                setValue(value);
            }
        }
    }

    @Override
    public CharSequence getSummary() {
        return String.format("%d", mValue);
/*
        if (mSummary != null) {
            summary += " " + mSummary;
        }
        return summary;
*/

    }

    @Override
    protected Parcelable onSaveInstanceState() {
        // save the instance state so that it will survive screen orientation changes and other events that may temporarily destroy it
        final Parcelable superState = super.onSaveInstanceState();

        // set the state's value with the class member that holds current setting value
        final SavedState myState = new SavedState(superState);
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
        setValue(myState.value);

        super.onRestoreInstanceState(myState.getSuperState());
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

        int value;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public SavedState(Parcel source) {
            super(source);

            value = source.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);

            dest.writeInt(value);
        }
    }
}