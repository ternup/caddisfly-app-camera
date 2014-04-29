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
import com.ternup.caddisfly.activity.MainActivity;
import com.ternup.caddisfly.activity.SurveyActivity;
import com.ternup.caddisfly.app.Globals;
import com.ternup.caddisfly.app.MainApp;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

@SuppressWarnings("WeakerAccess")
public class HomeFragment extends Fragment {

    private static final int REQUEST_LOCATION = 2;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        assert view != null;
        Button selectLocationButton = (Button) view.findViewById(R.id.selectLocationButton);
        selectLocationButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity())
                        .displayView(Globals.LOCATION_LIST_SCREEN_INDEX, true);
            }
        });

        Button addLocationButton = (Button) view.findViewById(R.id.addLocationButton);

        addLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addLocation();
            }
        });

        Button calibrateButton = (Button) view.findViewById(R.id.calibrateButton);
        calibrateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                assert getActivity() != null;
                ((MainActivity) getActivity()).displayView(Globals.CALIBRATE_SCREEN_INDEX, true);
            }
        });

        Button helpButton = (Button) view.findViewById(R.id.helpButton);
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                assert getActivity() != null;
                ((MainActivity) getActivity()).displayView(Globals.HELP_SCREEN_INDEX, true);
            }
        });

        assert getActivity() != null;
        assert getActivity().getApplicationContext() != null;
        if (((MainApp) getActivity().getApplicationContext()).CurrentTheme
                == R.style.AppTheme_Dark) {
            selectLocationButton.setAlpha(0.6f);
            addLocationButton.setAlpha(0.6f);
            calibrateButton.setAlpha(0.6f);
            helpButton.setAlpha(0.6f);
        } else {
            selectLocationButton.setAlpha(1f);
            addLocationButton.setAlpha(1f);
            calibrateButton.setAlpha(1f);
            helpButton.setAlpha(1f);
        }
        return view;
    }

    private void addLocation() {
        Intent intent = new Intent(getActivity(), SurveyActivity.class);
        startActivityForResult(intent, REQUEST_LOCATION);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_LOCATION) {
            if (resultCode == Activity.RESULT_OK) {
                long id = data.getLongExtra(getString(R.string.currentLocationId), -1);
                if (id > -1) {
                    displayLocation(id);
                }
            }
        }
    }

    private void displayLocation(long id) {
/*
        Fragment fragment = new LocationDetailsFragment();
        if (fragment != null) {
            Bundle args = new Bundle();
            args.putLong(getString(R.string.currentLocationId, id);
            fragment.setArguments(args);

            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.executePendingTransactions();
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.replace(R.id.container, fragment, Globals.RESULT_SCREEN_TAG);
            ft.addToBackStack(null);
            ft.commit();
            fragmentManager.executePendingTransactions();
        }
*/
    }


    @Override
    public void onStart() {
        super.onStart();
        assert getActivity() != null;
        getActivity().setTitle(R.string.home);
    }
}
