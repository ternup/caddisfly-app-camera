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

import com.ternup.caddisfly.R;
import com.ternup.caddisfly.app.Globals;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class StartFragment extends Fragment {

    private static final String EXTERNAL_PARAM = "external";

    private OnCalibrateListener mOnCalibrateListener;

    private OnHelpListener mOnHelpListener;

    private OnStartSurveyListener mOnStartSurveyListener;

    private OnStartTestListener mOnStartTestListener;

    private boolean mIsExternal = false;

    public StartFragment() {
        // Required empty public constructor
    }

    public static StartFragment newInstance(boolean external) {
        StartFragment fragment = new StartFragment();
        Bundle args = new Bundle();
        args.putBoolean(EXTERNAL_PARAM, external);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mIsExternal = getArguments().getBoolean(EXTERNAL_PARAM);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_start, container, false);
        getActivity().setTitle(R.string.appName);

        TextView startTestText = (TextView) view.findViewById(R.id.startTestText);
        final TextView startSurveyText = (TextView) view.findViewById(R.id.startSurveyText);

        Button calibrateButton = (Button) view.findViewById(R.id.calibrateButton);
        calibrateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnCalibrateListener != null) {
                    mOnCalibrateListener.onCalibrate(Globals.CALIBRATE_SCREEN_INDEX);
                }
            }
        });

        Button helpButton = (Button) view.findViewById(R.id.helpButton);
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnHelpListener != null) {
                    mOnHelpListener.onHelp(Globals.HELP_SCREEN_INDEX);
                }
            }
        });

        final Button startSurveyButton = (Button) view.findViewById(R.id.startSurveyButton);
        startSurveyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnStartSurveyListener != null) {
                    mOnStartSurveyListener.onStartSurvey();
                }
            }
        });

        Button startButton = (Button) view.findViewById(R.id.startButton);
        if (mIsExternal) {
            startSurveyText.setVisibility(View.GONE);
            startSurveyButton.setVisibility(View.GONE);
            startTestText.setVisibility(View.VISIBLE);
            startButton.setVisibility(View.VISIBLE);
            startButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnStartTestListener != null) {
                        mOnStartTestListener.onStartTest();

                    }
                }
            });
        } else {
            startSurveyText.setVisibility(View.GONE);
            startSurveyButton.setVisibility(View.VISIBLE);
            startTestText.setVisibility(View.GONE);
            startButton.setVisibility(View.GONE);
        }

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mOnCalibrateListener = (OnCalibrateListener) activity;
            mOnHelpListener = (OnHelpListener) activity;
            mOnStartTestListener = (OnStartTestListener) activity;
            mOnStartSurveyListener = (OnStartSurveyListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mOnCalibrateListener = null;
        mOnHelpListener = null;
        mOnStartSurveyListener = null;
        mOnStartTestListener = null;
    }

    /**
     * Reference: http://developer.android.com/training/basics/fragments/communicating.html
     */
    public interface OnCalibrateListener {

        public void onCalibrate(int index);
    }

    public interface OnHelpListener {

        public void onHelp(int index);
    }

    public interface OnStartSurveyListener {

        public void onStartSurvey();
    }

    public interface OnStartTestListener {

        public void onStartTest();
    }

}
