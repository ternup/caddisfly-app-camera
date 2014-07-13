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

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputFilter;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.ternup.caddisfly.R;
import com.ternup.caddisfly.app.Globals;
import com.ternup.caddisfly.app.MainApp;
import com.ternup.caddisfly.model.ColorInfo;
import com.ternup.caddisfly.util.AlertUtils;
import com.ternup.caddisfly.util.ColorUtils;
import com.ternup.caddisfly.util.FileUtils;

import java.io.File;
import java.util.ArrayList;


public class CalibrateFragment extends CalibrateFragmentBase {

    private CalibrateItemFragment mCalibrateItemFragment;


    public static CalibrateFragment newInstance() {
        return new CalibrateFragment();
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Spinner testTypeSpinner = (Spinner) view.findViewById(R.id.testTypeSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.testTypes, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_layout);
        testTypeSpinner.setAdapter(adapter);

        final MainApp mainApp = (MainApp) getActivity().getApplicationContext();

        if (mainApp.currentTestType < testTypeSpinner.getCount()) {
            testTypeSpinner.setSelection(mainApp.currentTestType, false);
        }

        testTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int position, long arg3) {
                changeTestType(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        changeTestType(mainApp.currentTestType);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.calibrate, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final MainApp mainApp = ((MainApp) getActivity().getApplicationContext());
        switch (item.getItemId()) {
            case R.id.menu_save:
                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                final EditText input = new EditText(getActivity());
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(22)});

                alertDialogBuilder.setView(input);
                alertDialogBuilder.setCancelable(false);

                alertDialogBuilder.setTitle(R.string.saveCalibration);
                alertDialogBuilder.setMessage(R.string.giveNameForCalibration);


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

                                if (!input.getText().toString().trim().isEmpty()) {
                                    final ArrayList<String> exportList = new ArrayList<String>();

                                    for (ColorInfo aColorList : mainApp.colorList) {
                                        exportList.add(ColorUtils.getColorRgbString(aColorList.getColor()));
                                    }

                                    File external = Environment.getExternalStorageDirectory();
                                    final String path = external.getPath() + "/" + Globals.APP_FOLDER_NAME + "/calibrate/";

                                    File file = new File(path + input.getText());
                                    if (file.exists()) {
                                        AlertUtils.askQuestion(getActivity(), R.string.overwriteFile,
                                                R.string.nameAlreadyExists, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        FileUtils.saveToFile(getActivity(), path, input.getText().toString(),
                                                                exportList.toString());
                                                    }
                                                }, null
                                        );
                                    } else {
                                        FileUtils.saveToFile(getActivity(), path, input.getText().toString(),
                                                exportList.toString());
                                    }

                                    closeKeyboard(input);
                                    alertDialog.dismiss();
                                } else {
                                    input.setError(getString(R.string.invalidName));
                                }
                            }
                        });
                    }
                });

                input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId
                                == EditorInfo.IME_ACTION_DONE)) {

                        }
                        return false;
                    }
                });

                alertDialog.show();
                input.requestFocus();
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

                return true;
            case R.id.menu_swatches:
                SwatchFragment fragment = new SwatchFragment();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction ft = fragmentManager.beginTransaction();
                ft.replace(R.id.container, fragment, "SwatchFragment");
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.addToBackStack(null);
                ft.commit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void closeKeyboard(EditText input) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
    }


    @Override
    protected void displayCalibrateItem(int index) {
        super.displayCalibrateItem(index);

        if (mCalibrateItemFragment == null) {
            mCalibrateItemFragment = new CalibrateItemFragment();
        } else {
            //TODO: fix this
            try {
                mCalibrateItemFragment.setArguments(null);
            } catch (Exception e) {
                mCalibrateItemFragment = new CalibrateItemFragment();
            }
        }
        //mCalibrateItemFragment = CalibrateItemFragment.newInstance();

        FragmentManager fragmentManager = getFragmentManager();
        assert fragmentManager != null;
        fragmentManager.executePendingTransactions();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        Bundle args = new Bundle();
        args.putInt(getString(R.string.swatchIndex), index);
        MainApp mainApp = (MainApp) getActivity().getApplicationContext();
        args.putInt(getString(R.string.currentTestTypeId), mainApp.currentTestType);
        mCalibrateItemFragment.setArguments(args);
        ft.replace(R.id.container, mCalibrateItemFragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack(null);
        ft.commit();
        fragmentManager.executePendingTransactions();
    }


}
