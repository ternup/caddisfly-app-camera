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

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.ternup.caddisfly.R;
import com.ternup.caddisfly.adapter.GalleryListAdapter;
import com.ternup.caddisfly.app.Globals;
import com.ternup.caddisfly.app.MainApp;
import com.ternup.caddisfly.fragment.CalibrateItemFragmentBase;
import com.ternup.caddisfly.fragment.SwatchFragment;
import com.ternup.caddisfly.util.AlertUtils;
import com.ternup.caddisfly.util.ColorUtils;
import com.ternup.caddisfly.util.FileUtils;

import java.io.File;
import java.util.ArrayList;

@SuppressWarnings("WeakerAccess")
public class CalibrateItemFragment extends CalibrateItemFragmentBase {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void updateListView(int position) {

        // override and show only headers by not providing data to list adapter

        ArrayList<String> files = new ArrayList<String>();
        mAdapter = new GalleryListAdapter(getActivity(), mTestType, position, files, false);
        setListAdapter(mAdapter);

    }

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
    public boolean onOptionsItemSelected(MenuItem item) {
        final MainApp mainApp = ((MainApp) getActivity().getApplicationContext());
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
            case R.id.menu_load:
                try {

                    AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());
                    builderSingle.setIcon(R.drawable.ic_launcher);
                    builderSingle.setTitle(R.string.loadCalibration);

                    final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(),
                            android.R.layout.select_dialog_singlechoice);

                    File external = Environment.getExternalStorageDirectory();
                    final String folderName = "/com.ternup.caddisfly/calibrate/";
                    String path = external.getPath() + folderName;
                    File folder = new File(path);
                    if (folder.exists()) {
                        final File[] listFiles = folder.listFiles();
                        for (int j = 0; j < listFiles.length; j++) {
                            arrayAdapter.add(listFiles[j].getName());
                        }

                        builderSingle.setNegativeButton(R.string.cancel,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }
                        );

                        builderSingle.setAdapter(arrayAdapter,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String fileName = listFiles[which].getName();
                                        final ArrayList<Integer> swatchList = new ArrayList<Integer>();

                                        final ArrayList<String> rgbList = FileUtils.loadFromFile(getActivity(), fileName);
                                        if (rgbList != null) {

                                            for (String rgb : rgbList) {
                                                swatchList.add(ColorUtils.getColorFromRgb(rgb));
                                            }

                                            (new AsyncTask<Void, Void, Void>() {

                                                @Override
                                                protected Void doInBackground(Void... params) {
                                                    mainApp.saveCalibratedSwatches(mainApp.currentTestType, swatchList);

                                                    mainApp.setSwatches(mainApp.currentTestType);

                                                    SharedPreferences sharedPreferences = PreferenceManager
                                                            .getDefaultSharedPreferences(getActivity());
                                                    SharedPreferences.Editor editor = sharedPreferences.edit();

                                                    for (int i = 0; i < mainApp.rangeIntervals.size(); i++) {
                                                        int index = i * mainApp.rangeIncrementStep;

                                                        ColorUtils.autoGenerateColors(
                                                                index,
                                                                mainApp.currentTestType,
                                                                mainApp.colorList,
                                                                mainApp.rangeIncrementStep, editor);
                                                    }
                                                    editor.commit();


                                                    return null;
                                                }

                                                @Override
                                                protected void onPostExecute(Void result) {
                                                    // TODO Auto-generated method stub
                                                    super.onPostExecute(result);
                                                }
                                            }).execute();
                                        }
                                    }
                                }
                        );
                        //builderSingle.show();

                        final AlertDialog alert = builderSingle.create();
                        alert.setOnShowListener(new DialogInterface.OnShowListener() {
                            @Override
                            public void onShow(DialogInterface dialogInterface) {
                                final ListView listView = alert.getListView();
                                listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                                    @Override
                                    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                                        final int position = i;

                                        AlertUtils.askQuestion(getActivity(), R.string.delete, R.string.selectedWillBeDeleted, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                String fileName = listFiles[position].getName();
                                                FileUtils.deleteFile(folderName, fileName);
                                                ArrayAdapter listAdapter = (ArrayAdapter) listView.getAdapter();
                                                listAdapter.remove(listAdapter.getItem(position));
                                            }
                                        }, null);
                                        return true;
                                    }
                                });

                            }
                        });

                        alert.show();


                    } else {
                        AlertUtils.showMessage(getActivity(), R.string.notFound, R.string.noSavedCalibrations);
                    }

                } catch (ActivityNotFoundException e) {
                    AlertUtils.showMessage(getActivity(), R.string.error,
                            R.string.updateRequired);

                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
