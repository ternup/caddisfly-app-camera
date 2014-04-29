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

import com.ternup.caddisfly.R;
import com.ternup.caddisfly.app.MainApp;
import com.ternup.caddisfly.widget.FormEditText;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Address;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

public class FormFragment extends BaseFragment implements TextView.OnEditorActionListener {

    FormEditText mPlaceEditText;

    FormEditText mThoroughfareEditText;

    FormEditText mSubLocalityEditText;

    FormEditText mLocalityEditText;

    FormEditText mStateEditText;

    FormEditText mCountryEditText;

    TextWatcher textWatcher;

    // Handle to SharedPreferences for this app
    SharedPreferences mPrefs;

    private Address mAddress;

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            mPlaceEditText.getEditText().removeTextChangedListener(textWatcher);

            MainApp mainApp = (MainApp) getActivity().getApplicationContext();
            mAddress = mainApp.address;

            mPlaceEditText.getEditText().setText(mAddress.getFeatureName());

            mThoroughfareEditText.getEditText().setText(mAddress.getThoroughfare());

            mSubLocalityEditText.getEditText().setText(mAddress.getSubLocality());

            mLocalityEditText.getEditText().setText(mAddress.getLocality());

            mStateEditText.getEditText().setText(mAddress.getAdminArea());

            mCountryEditText.getEditText().setText(mAddress.getCountryName());

            mPlaceEditText.getEditText().addTextChangedListener(textWatcher);

        }
    };

    public FormFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static FormFragment newInstance() {
        return new FormFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Register to receive messages.
        // We are registering an observer (mMessageReceiver) to receive Intents
        // with actions named "custom-event-name".
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver,
                new IntentFilter("locationUpdated"));

        MainApp mainApp = (MainApp) getActivity().getApplicationContext();
        mAddress = mainApp.address;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_form, container, false);
/*
        TextView textView = (TextView) rootView.findViewById(R.id.section_label);
        textView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
*/
        LinearLayout linearLayout = (LinearLayout) rootView.findViewById(R.id.rootLayout);

        //mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (mAddress == null) {
                    MainApp mainApp = (MainApp) getActivity().getApplicationContext();
                    mainApp.address = new Address(Locale.getDefault());
                    mAddress = mainApp.address;

                }
                mAddress.setFeatureName(mPlaceEditText.getEditText().getText().toString());

            }
        };

        mPlaceEditText = new FormEditText(getActivity(), "Place");
        mPlaceEditText.getEditText().requestFocus();
        linearLayout.addView(mPlaceEditText.getView());
        mPlaceEditText.getEditText().addTextChangedListener(textWatcher);

        mThoroughfareEditText = new FormEditText(getActivity(), "Street");
        linearLayout.addView(mThoroughfareEditText.getView());

        mSubLocalityEditText = new FormEditText(getActivity(), "Town");
        linearLayout.addView(mSubLocalityEditText.getView());

        mLocalityEditText = new FormEditText(getActivity(), "City");
        linearLayout.addView(mLocalityEditText.getView());

        mStateEditText = new FormEditText(getActivity(), "State");
        linearLayout.addView(mStateEditText.getView());

        mCountryEditText = new FormEditText(getActivity(), "Country");
        mCountryEditText.getEditText().setImeActionLabel("Done", EditorInfo.IME_ACTION_DONE);
        linearLayout.addView(mCountryEditText.getView());
        mCountryEditText.getEditText().setOnEditorActionListener(this);

        return rootView;
    }

    public void showKeyboard() {

        if (mPlaceEditText.getEditText().getText().toString().isEmpty()) {

            (new Handler()).postDelayed(new Runnable() {

                public void run() {
                    mPlaceEditText.getEditText().dispatchTouchEvent(MotionEvent
                            .obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                                    MotionEvent.ACTION_DOWN, 0, 0, 0));
                    mPlaceEditText.getEditText().dispatchTouchEvent(
                            MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock
                                    .uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0)
                    );

                }
            }, 300);
        }

/*
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mPlaceEditText.getInputControl(), InputMethodManager.SHOW_FORCED);
*/
    }


    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        if (EditorInfo.IME_ACTION_DONE == EditorInfo.IME_ACTION_DONE) {
            // Return input text to activity

            String resultPlace = mPlaceEditText.getEditText().getText().toString().trim();
            //String address = mAddressEdit.getText().toString().trim();
            //int resultSource = mSourceSpinner.getSelectedItemPosition();

            if (resultPlace.isEmpty()) {
                mPlaceEditText.getEditText().setError("Please enter a Place name");
                mPlaceEditText.getEditText().post(new Runnable() {
                    public void run() {
                        mPlaceEditText.getEditText().requestFocusFromTouch();
                        InputMethodManager lManager = (InputMethodManager) getActivity()
                                .getSystemService(Context.INPUT_METHOD_SERVICE);
                        lManager.showSoftInput(mPlaceEditText.getEditText(), 0);
                    }
                });
            } else {
                listener.onPageComplete();
            }

            /*else if (address.isEmpty()) {
                mAddressEdit.setError("Please enter an Address");
                mAddressEdit.post(new Runnable() {
                    public void run() {
                        mAddressEdit.requestFocusFromTouch();
                        InputMethodManager lManager = (InputMethodManager) getActivity()
                                .getSystemService(Context.INPUT_METHOD_SERVICE);
                        lManager.showSoftInput(mAddressEdit, 0);
                    }
                });
            } else if (resultSource == 0) {
                mPlaceEdit.setError(null);
                Toast toast = Toast
                        .makeText(getActivity(), "Please select a Source type", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.TOP, 0, 40);
                toast.show();
            } else {

                ContentValues values = new ContentValues();
                //values.put(TestTable.COLUMN_ADDRESS, placeEdit.getText().toString().trim());
                //values.put(TestTable.COLUMN_SOURCE, resultSource - 1);

                //Uri uri = ContentUris.withAppendedId(LocationContentProvider.CONTENT_URI, mId);
                //mContext.getContentResolver().update(uri, values, null, null);

                long locationId = saveResult();

                AddressDialogListener activity = (AddressDialogListener) getTargetFragment();
                activity.onFinishEditDialog(locationId);
                dialog.dismiss();

                return true;

            }*/
        }
        return false;
    }

    @Override
    public void onDestroy() {
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }
}
