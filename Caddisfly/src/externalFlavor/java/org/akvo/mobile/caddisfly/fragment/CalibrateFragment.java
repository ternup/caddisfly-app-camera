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

import com.ternup.caddisfly.BuildConfig;
import com.ternup.caddisfly.R;
import com.ternup.caddisfly.adapter.CalibrateListAdapter;
import com.ternup.caddisfly.app.Globals;
import com.ternup.caddisfly.app.MainApp;
import com.ternup.caddisfly.fragment.CalibrateFragmentBase;
import com.ternup.caddisfly.util.AlertUtils;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;

public class CalibrateFragment extends CalibrateFragmentBase {

    private static final int REQUEST_IMPORT = 100;

    private CalibrateItemFragment mCalibrateItemFragment;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        Intent LaunchIntent = getActivity().getPackageManager()
                .getLaunchIntentForPackage(BuildConfig.PACKAGE_NAME);
        if (LaunchIntent != null) {
            inflater.inflate(R.menu.calibrate, menu);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        changeTestType(Globals.FLUORIDE_INDEX);
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

        switch (item.getItemId()) {
            case R.id.menu_import:
                AlertUtils
                        .askQuestion(getActivity(), R.string.importAction,
                                R.string.importFromCaddisfly,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent = new Intent();
                                        intent.setAction(Globals.ACTION_IMPORT_CALIBRATION);
                                        //intent.addCategory("android.intent.category.LAUNCHER");
                                        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        try {
                                            startActivityForResult(intent, REQUEST_IMPORT);
                                        } catch (ActivityNotFoundException e) {
                                            AlertUtils.showMessage(getActivity(), R.string.error,
                                                    R.string.updateRequired);

                                        }
                                    }
                                }, null
                        );
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMPORT && resultCode == Activity.RESULT_OK) {
            final ArrayList<Integer> swatchList = data.getIntegerArrayListExtra("swatches");
            final MainApp mainApp = (MainApp) getActivity().getApplicationContext();

            (new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... params) {
                    mainApp.saveCalibratedSwatches(Globals.FLUORIDE_INDEX, swatchList);
                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {
                    // TODO Auto-generated method stub
                    super.onPostExecute(result);

                    mainApp.setFluorideSwatches();
                    CalibrateListAdapter adapter = (CalibrateListAdapter) getListAdapter();
                    adapter.notifyDataSetChanged();
                }
            }).execute();

        }
    }
}
