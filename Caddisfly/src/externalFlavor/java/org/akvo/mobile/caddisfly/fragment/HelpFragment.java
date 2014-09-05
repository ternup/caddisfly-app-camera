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

import com.ternup.caddisfly.R;


public class HelpFragment extends Fragment {

    private OnChecklistListener mOnChecklistListener;
    private OnHelpListener mOnHelpListener;

    public HelpFragment() {
        // Required empty public constructor
    }

    public static HelpFragment newInstance() {
        HelpFragment fragment = new HelpFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
/*
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
*/
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_help, container, false);

        Button checklistButton = (Button) view.findViewById(R.id.checklistButton);
        checklistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnChecklistListener != null) {
                    mOnChecklistListener.onChecklist();
                }
            }
        });


        Button trainingButton = (Button) view.findViewById(R.id.trainingVideo);
        trainingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnHelpListener != null) {
                    mOnHelpListener.onHelp();
                }
            }
        });

        return view;
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mOnChecklistListener = null;
        mOnHelpListener = null;

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mOnChecklistListener = (OnChecklistListener) activity;
            mOnHelpListener = (OnHelpListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }


    public interface OnHelpListener {

        public void onHelp();
    }

    public interface OnChecklistListener {

        public void onChecklist();
    }

}
