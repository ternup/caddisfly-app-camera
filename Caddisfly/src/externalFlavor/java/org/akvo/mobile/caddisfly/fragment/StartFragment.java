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

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ternup.caddisfly.R;
import com.ternup.caddisfly.app.MainApp;
import com.ternup.caddisfly.util.DataHelper;

public class StartFragment extends Fragment {

    private static final String EXTERNAL_PARAM = "external";

    private static final String TEST_TYPE_PARAM = "testType";

    private OnStartSurveyListener mOnStartSurveyListener;

    private OnStartTestListener mOnStartTestListener;

    private OnVideoListener mOnVideoListener;

    private boolean mIsExternal = false;

    private int mTestType;

    @SuppressWarnings("WeakerAccess")
    public StartFragment() {
        // Required empty public constructor
    }

    public static StartFragment newInstance(boolean external, int testType) {
        StartFragment fragment = new StartFragment();
        Bundle args = new Bundle();
        args.putBoolean(EXTERNAL_PARAM, external);
        args.putInt(TEST_TYPE_PARAM, testType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mIsExternal = getArguments().getBoolean(EXTERNAL_PARAM);
            mTestType = getArguments().getInt(TEST_TYPE_PARAM);
        }
        //setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_start, container, false);
        getActivity().setTitle(R.string.appName);

        TextView testTypeTextView = (TextView) view.findViewById(R.id.testTypeTextView);
        testTypeTextView.setText(DataHelper.getTestTitle(getActivity(), mTestType));

/*
        if (mOnCalibrateListener != null) {
            mOnCalibrateListener.onCalibrate();
        }
*/
/*

        TextView versionTextView = (TextView) view.findViewById(R.id.versionTextView);

        versionTextView.setText(String.format(getString(R.string.versionStringFormat), getString(R.string.appName),
                MainApp.getVersion(getActivity())));
*/

        MainApp mainApp = (MainApp) getActivity().getApplicationContext();
        int errorCount = mainApp.getCalibrationErrorCount(mTestType);

        if (errorCount > 0) {

        } else {
        }

        final Button startSurveyButton = (Button) view.findViewById(R.id.startSurveyButton);
        startSurveyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnStartSurveyListener != null) {
                    mOnStartSurveyListener.onStartSurvey();
                }
            }
        });

    /*    final Button videoButton = (Button) view.findViewById(R.id.videoButton);
        videoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                File sdDir = getActivity().getExternalFilesDir(null);
                final File videoFile = new File(sdDir, "training.mp4");

                if (!videoFile.exists()) {
                    if(NetworkUtils.checkInternetConnection(getActivity())) {
                        final Intent intent = new Intent(getActivity(), VideoActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                    }
                }else{
                    final Intent intent = new Intent(getActivity(), VideoActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                }
            }
        });*/

        //TextView startTestText = (TextView) view.findViewById(R.id.startTestText);
        //startTestText.setText(getString(R.string.attachFilledCartridge), true);

        LinearLayout surveyLayout = (LinearLayout) view.findViewById(R.id.surveyLayout);
        LinearLayout prepareLayout = (LinearLayout) view.findViewById(R.id.prepareLayout);

        Button videoButton = (Button) view.findViewById(R.id.videoButton);
        videoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnVideoListener != null) {
                    mOnVideoListener.onVideo();
                }
            }
        });

        Button startButton = (Button) view.findViewById(R.id.skipButton);
        if (mIsExternal) {
            startButton.setText(R.string.skip);
            //surveyLayout.setVisibility(View.GONE);
            //prepareLayout.setVisibility(View.VISIBLE);
        } else {
            startButton.setText(R.string.startSurvey);
            ////mOnStartSurveyListener.onStartSurvey();
            //surveyLayout.setVisibility(View.VISIBLE);
            //startSurveyButton.setVisibility(View.VISIBLE);
            //prepareLayout.setVisibility(View.GONE);
        }
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnStartTestListener != null) {
                    if (mIsExternal) {
                        mOnStartTestListener.onStartTest();
                    } else {
                        mOnStartSurveyListener.onStartSurvey();
                    }
                }
            }
        });

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mOnStartTestListener = (OnStartTestListener) activity;
            mOnStartSurveyListener = (OnStartSurveyListener) activity;
            mOnVideoListener = (OnVideoListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        //mOnStartTestListener.onStartTest();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mOnStartSurveyListener = null;
        mOnStartTestListener = null;
        mOnVideoListener = null;
    }

    /**
     * Reference: http://developer.android.com/training/basics/fragments/communicating.html
     */


    public interface OnStartSurveyListener {

        public void onStartSurvey();
    }

    public interface OnStartTestListener {

        public void onStartTest();
    }

    public interface OnVideoListener {

        public void onVideo();
    }
}
