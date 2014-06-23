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

public class StartFragment extends Fragment {

    private OnCalibrateListener mOnCalibrateListener;

    private OnStartTestListener mOnStartTestListener;

    private boolean mIsExternal = false;

    private static final String EXTERNAL_PARAM = "external";

    private String mParam1;

    public static StartFragment newInstance(boolean external) {
        StartFragment fragment = new StartFragment();
        Bundle args = new Bundle();
        args.putBoolean(EXTERNAL_PARAM, external);
        fragment.setArguments(args);
        return fragment;
    }

    public StartFragment() {
        // Required empty public constructor
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

        Button calibrateButton = (Button) view.findViewById(R.id.calibrateButton);
        calibrateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnCalibrateListener != null) {
                    mOnCalibrateListener.onCalibrate(Globals.CALIBRATE_SCREEN_INDEX);
                }
            }
        });

        Button startButton = (Button) view.findViewById(R.id.startButton);
        if (mIsExternal) {
            startButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnStartTestListener != null) {
                        mOnStartTestListener.onStartTest(Globals.CALIBRATE_SCREEN_INDEX);
                    }
                }
            });
        } else {
            startButton.setVisibility(View.GONE);
        }

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mOnCalibrateListener = (OnCalibrateListener) activity;
            mOnStartTestListener = (OnStartTestListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mOnCalibrateListener = null;

        mOnStartTestListener = null;
    }

    /**
     * Reference: http://developer.android.com/training/basics/fragments/communicating.html
     */
    public interface OnCalibrateListener {

        public void onCalibrate(int index);
    }

    public interface OnStartTestListener {

        public void onStartTest(int index);
    }

}
