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

package org.akvo.mobile.caddisfly.fragment;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.ternup.caddisfly.R;
import com.ternup.caddisfly.app.Globals;
import com.ternup.caddisfly.app.MainApp;
import com.ternup.caddisfly.fragment.CalibrateFragmentBase;

import java.io.File;

public class CalibrateFragment extends CalibrateFragmentBase {

    private CalibrateItemFragment mCalibrateItemFragment;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        Intent LaunchIntent = getActivity().getPackageManager()
                .getLaunchIntentForPackage(Globals.CADDISFLY_PACKAGE_NAME);
        if (LaunchIntent != null) {
            File external = Environment.getExternalStorageDirectory();
            String path = external.getPath() + "/com.ternup.caddisfly/calibrate/";
            File folder = new File(path);
            if (folder.exists()) {
                inflater.inflate(R.menu.calibrate, menu);
            }
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainApp mainApp = (MainApp) getActivity().getApplicationContext();
        changeTestType(mainApp.currentTestType);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

}
