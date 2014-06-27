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
import com.ternup.caddisfly.adapter.CalibrateListAdapter;
import com.ternup.caddisfly.app.Globals;
import com.ternup.caddisfly.app.MainApp;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import java.util.ArrayList;

public class CalibrateFragmentBase extends ListFragment implements AdapterView.OnItemClickListener {

    //private int mTestType = 0;

    public CalibrateFragmentBase() {
    }

    @SuppressWarnings("NullableProblems")
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        //getActivity().getActionBar().hide();
        return inflater.inflate(R.layout.fragment_calibrate, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle(R.string.calibrate);
        getListView().setOnItemClickListener(this);
    }

    public void changeTestType(int position) {
        final MainApp mainApp = (MainApp) getActivity().getApplicationContext();
        mainApp.currentTestType = position;

        switch (position) {
            case Globals.FLUORIDE_INDEX:
                mainApp.setFluorideSwatches();
                setAdapter();
                break;
            case Globals.FLUORIDE_2_INDEX:
                mainApp.setFluoride2Swatches();
                setAdapter();
                break;
            case Globals.PH_INDEX:
                mainApp.setPhSwatches();
                setAdapter();
                break;
            default:
                break;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public void setAdapter() {

        Activity activity = getActivity();
        MainApp mainApp = (MainApp) activity.getApplicationContext();

        assert mainApp != null;
        ArrayList<Double> rangeIntervals = mainApp.rangeIntervals;
        Double[] rangeArray = rangeIntervals.toArray(new Double[rangeIntervals.size()]);

        CalibrateListAdapter customList = new CalibrateListAdapter(getActivity(), rangeArray);
        customList.setTestType(mainApp.currentTestType);
        setListAdapter(customList);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        //CursorWrapper content = (CursorWrapper) adapterView.getItemAtPosition(i);
        displayCalibrateItem(i);
    }

    protected void displayCalibrateItem(int index) {
    }
}
