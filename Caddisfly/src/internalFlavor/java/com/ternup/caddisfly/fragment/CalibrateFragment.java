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

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;


public class CalibrateFragment extends CalibrateFragmentBase {

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

        switch (item.getItemId()) {
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
}
