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

import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class AboutFragment extends Fragment {

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);

        assert getActivity() != null;
        assert getActivity().getPackageManager() != null;

        if (view != null) {
            TextView productView = (TextView) view.findViewById(R.id.textVersion);
            try {
                String version = getActivity().getPackageManager()
                        .getPackageInfo(getActivity().getPackageName(), 0).versionName;
                String[] words = version.split("\\s");
                String versionString = "";
                for (String word : words) {
                    try {
                        Double versionNumber = Double.parseDouble(word);
                        versionString += String.format("%.2f", versionNumber);
                    } catch (NumberFormatException e) {
                        int id = getResources()
                                .getIdentifier(word, "string", getActivity().getPackageName());
                        if (id > 0) {
                            versionString += getResources().getString(id);
                        } else {
                            versionString += word;
                        }
                    }
                    versionString += " ";
                }
                productView.setText(versionString.trim());

            } catch (PackageManager.NameNotFoundException e) {
                productView.setText("");
            }

            ImageView organizationView = (ImageView) view.findViewById(R.id.organizationImage);

            productView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    openWebBrowser(Globals.PRODUCT_WEBSITE);
                }
            });

            organizationView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    openWebBrowser(Globals.ORG_WEBSITE);
                }
            });
        }
        assert getActivity().getApplicationContext() != null;
        if (((MainApp) getActivity().getApplicationContext()).CurrentTheme
                == R.style.AppTheme_Dark) {
            assert view != null;
            view.findViewById(R.id.layoutAboutCompany).setAlpha(0.5f);
        }

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        assert getActivity() != null;
        getActivity().setTitle(R.string.about);
    }

    /**
     * Open a web Browser and navigate to given url
     *
     * @param url The url to navigate to
     */
    private void openWebBrowser(String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }
}
