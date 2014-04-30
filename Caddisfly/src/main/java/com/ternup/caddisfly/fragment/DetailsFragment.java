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
import com.ternup.caddisfly.adapter.GalleryListAdapter;
import com.ternup.caddisfly.util.FileUtils;
import com.ternup.caddisfly.util.PreferencesHelper;
import com.ternup.caddisfly.view.LineChartView;

import android.app.ListFragment;
import android.content.SharedPreferences;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

public class DetailsFragment extends ListFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gallery, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle(R.string.details);

        ListView listView = getListView();

        String folderName = getArguments().getString(PreferencesHelper.FOLDER_NAME_KEY);
        String title = getArguments().getString("title");

        View header = getActivity().getLayoutInflater().inflate(R.layout.list_header, null, false);

        assert listView != null;
        listView.addHeaderView(header);

        // Gradient shading for title
        assert header != null;
        TextView titleTextView = (TextView) header.findViewById(R.id.titleText);
        TextView subtitleTextView = (TextView) header.findViewById(R.id.subtitleText);

        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getActivity());

        long locationId = sharedPreferences.getLong(getString(R.string.currentLocationId), -1);

        ArrayList<String> imagePaths = FileUtils
                .getFilePaths(getActivity(), folderName, "/small/", locationId);

        titleTextView.setText(title);
        subtitleTextView.setText(
                String.valueOf(String.format("%s: %d", getString(R.string.analysisCount),
                        imagePaths.size()))
        );

        Shader textShader = new LinearGradient(0, 0, 0, titleTextView.getPaint().getTextSize(),
                new int[]{getResources().getColor(R.color.textGradientStart),
                        getResources().getColor(R.color.textGradientEnd)},
                new float[]{0, 1}, Shader.TileMode.CLAMP
        );
        titleTextView.getPaint().setShader(textShader);

        Collections.sort(imagePaths);

        LineChartView lineChart = (LineChartView) header.findViewById(R.id.lineChart);

        long testId = sharedPreferences.getLong(getString(R.string.currentTestId), -1);
        ArrayList files = FileUtils.getFilePaths(getActivity(), folderName, locationId);
        float[] valueArray = new float[files.size()];
        int value = 0;
        int counter = 0;
        while (value != -1) {
            value = sharedPreferences.getInt(String.format("result_%d_%d", testId, counter), -1);
            if (value > -1) {
                valueArray[counter] = value;
                counter++;
            }
        }

        if (counter > 1) {
            lineChart.setChartData(valueArray);
        } else {
            lineChart.setVisibility(View.GONE);
        }
        GalleryListAdapter adapter = new GalleryListAdapter(getActivity(), imagePaths);
        this.setListAdapter(adapter);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

        //to prevent error: 'Cannot add header view to list — setAdapter has already been called.'
        setListAdapter(null);
    }
}
