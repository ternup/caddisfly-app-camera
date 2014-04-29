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

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class HelpFragment extends Fragment {

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_help, container, false);

        if (view != null) {
            WebView webView = (WebView) view.findViewById(R.id.helpView);
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);
                    return true;
                }
            });

            webView.setBackgroundColor(0x00000000);
            webView.getSettings().setDefaultFontSize(17);

            //String helpText = readRawTextFile(R.raw.help);
            assert getResources().getConfiguration().locale != null;
            String language = getResources().getConfiguration().locale.getLanguage();
            int id = getActivity().getResources()
                    .getIdentifier(String.format("help_%s", language), "raw",
                            getActivity().getPackageName());
            String helpText;
            if (id > 0) {
                helpText = readRawTextFile(id);
            } else {
                helpText = readRawTextFile(R.raw.help);
            }

            assert getActivity().getApplicationContext() != null;
            if (((MainApp) getActivity().getApplicationContext()).CurrentTheme
                    == R.style.AppTheme_Light) {
                helpText = helpText.replace("#bbb", "#111");
            }
            webView.loadData(helpText, "text/html; charset=utf-8", "utf-8");
        }

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle(R.string.help);
    }

    /**
     * Load the raw html file
     *
     * @param id The id of the file
     * @return The text from the file
     */
    private String readRawTextFile(int id) {
        InputStream inputStream = getActivity().getResources().openRawResource(id);
        InputStreamReader in = new InputStreamReader(inputStream);
        BufferedReader buf = new BufferedReader(in);
        String line;
        StringBuilder text = new StringBuilder();
        try {
            while ((line = buf.readLine()) != null) {
                text.append(line);
            }
        } catch (IOException e) {
            return null;
        }
        return text.toString();
    }
}


