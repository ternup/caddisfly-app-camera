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
import com.ternup.caddisfly.app.MainApp;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;

public class CalibrateFragment extends ListFragment implements AdapterView.OnItemClickListener {

    CalibrateItemFragment mCalibrateItemFragment;

    private int mTestType = 0;

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

        final Spinner testTypeSpinner = (Spinner) view.findViewById(R.id.testTypeSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.testTypes, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_layout);
        testTypeSpinner.setAdapter(adapter);

        testTypeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                    int position, long arg3) {

                mTestType = position;
                // TODO Auto-generated method stub
                Activity activity = getActivity();
                MainApp mainApp = (MainApp) activity.getApplicationContext();

                switch (position) {
                    case 0:
                        mainApp.setFluorideSwatches();
                        setAdapter();
                        break;
                    case 1:
                        mainApp.setPhSwatches();
                        setAdapter();
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.calibrate, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_swatches:
/*
                SwatchFragment fragment = new SwatchFragment();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction ft = fragmentManager.beginTransaction();
                ft.replace(R.id.container, fragment, "SwatchFragment");
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.addToBackStack(null);
                ft.commit();
*/
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setAdapter() {

        Activity activity = getActivity();
        MainApp mainApp = (MainApp) activity.getApplicationContext();

        assert mainApp != null;
        ArrayList<Double> rangeIntervals = mainApp.rangeIntervals;
        Double[] rangeArray = rangeIntervals.toArray(new Double[rangeIntervals.size()]);

        CalibrateListAdapter customList = new CalibrateListAdapter(getActivity(), rangeArray);
        customList.setTestType(mTestType);
        setListAdapter(customList);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        //CursorWrapper content = (CursorWrapper) adapterView.getItemAtPosition(i);
        displayLocation(i);

    }

    private void displayLocation(int index) {

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

        FragmentManager fragmentManager = getFragmentManager();
        assert fragmentManager != null;
        FragmentTransaction ft = fragmentManager.beginTransaction();
        Bundle args = new Bundle();
        args.putInt(getString(R.string.swatchIndex), index);
        args.putInt(getString(R.string.testType), mTestType);
        mCalibrateItemFragment.setArguments(args);
        ft.replace(R.id.container, mCalibrateItemFragment, "mCalibrateItemFragment");
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack(null);
        ft.commit();
        fragmentManager.executePendingTransactions();
    }
}
