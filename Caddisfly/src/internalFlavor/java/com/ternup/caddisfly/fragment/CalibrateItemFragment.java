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
import com.ternup.caddisfly.app.Globals;
import com.ternup.caddisfly.app.MainApp;
import com.ternup.caddisfly.util.FileUtils;
import com.ternup.caddisfly.util.PreferencesUtils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class CalibrateItemFragment extends CalibrateItemFragmentBase {


    private TextView mRgbText;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRgbText = (TextView) mListHeader.findViewById(R.id.rgbText);

        final int position = getArguments().getInt(getString(R.string.swatchIndex));

        Button editButton = (Button) mListHeader.findViewById(R.id.editButton);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                editCalibration(position);
            }
        });

    }

    @Override
    void displayInfo(boolean animate) {
        super.displayInfo(animate);

        final MainApp mainApp = ((MainApp) getActivity().getApplicationContext());

        final int position = getArguments().getInt(getString(R.string.swatchIndex));
        final int index = position * mainApp.rangeIncrementStep;

        int color = PreferencesUtils.getInt(mainApp,
                String.format("%s-%s", String.valueOf(mTestType), String.valueOf(index)),
                -1);

        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        mRgbText.setText(
                String.format("%s: %d  %d  %d", mainApp.getString(R.string.rgb), r, g, b));
/*
        if (accuracy == -1) {
            mColorButton.setText(getActivity().getString(R.string.notCalibrated));
            color = Color.WHITE;
            mRgbText.setVisibility(View.GONE);
        } else {
            mRgbText.setVisibility(View.VISIBLE);
        }
*/

    }

    public void editCalibration(final int position) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_PHONE);

        alertDialogBuilder.setView(input);
        alertDialogBuilder.setCancelable(false);

        alertDialogBuilder.setTitle(R.string.enterColorRgb);
        alertDialogBuilder.setPositiveButton(R.string.ok, null);
        alertDialogBuilder
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        closeKeyboard(input);
                        dialog.cancel();
                    }
                });
        final AlertDialog alertDialog = alertDialogBuilder.create(); //create the box

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {

                Button b = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (saveRgb(input.getText().toString(), position)) {
                            closeKeyboard(input);
                            alertDialog.dismiss();
                        } else {
                            input.setError(getString(R.string.invalidColor));
                        }
                    }
                });
            }
        });

        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId
                        == EditorInfo.IME_ACTION_DONE)) {

                    if (saveRgb(input.getText().toString(), position)) {
                        closeKeyboard(input);
                        alertDialog.cancel();
                    } else {
                        input.setError(getString(R.string.invalidColor));
                        input.requestFocus();
                        InputMethodManager imm = (InputMethodManager) getActivity()
                                .getSystemService(
                                        Context.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                    }
                }
                return false;
            }
        });

        alertDialog.show();
        input.requestFocus();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public void closeKeyboard(EditText input) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(input.getWindowToken(), 0);

    }

    public boolean saveRgb(String value, int position) {

        try {
            String[] rgbArray = value.split(" ");
            if (rgbArray.length < 3) {
                rgbArray = value.split("-");
            }
            if (rgbArray.length < 3) {
                rgbArray = value.split("\\.");
            }

            if (rgbArray.length < 3 && value.length() > 8) {
                rgbArray = new String[3];
                rgbArray[0] = value.substring(0, 3);
                rgbArray[1] = value.substring(3, 6);
                rgbArray[2] = value.substring(6, 9);
            }

            if (rgbArray.length > 2) {
                int r = Integer.parseInt(rgbArray[0]);
                int g = Integer.parseInt(rgbArray[1]);
                int b = Integer.parseInt(rgbArray[2]);
                if (r < 256 && g < 256 && b < 256) {
                    super.storeCalibratedData(position, Color.rgb(r, g, b), 100);

                    String folderName = FileUtils.getStoragePath(getActivity(), -1,
                            String.format("%s/%d/%d/", Globals.CALIBRATE_FOLDER, mTestType,
                                    position), false
                    );

                    FileUtils.deleteFolder(getActivity(), -1, folderName);
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return false;
    }

}
