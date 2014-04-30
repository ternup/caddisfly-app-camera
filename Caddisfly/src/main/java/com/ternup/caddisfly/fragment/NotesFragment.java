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
import com.ternup.caddisfly.component.NothingSelectedSpinnerAdapter;
import com.ternup.caddisfly.widget.FormSpinner;
import com.ternup.caddisfly.widget.MultiLineEditText;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;

public class NotesFragment extends BaseFragment {

    FormSpinner mSourceSpinner;

    MultiLineEditText mMultiLineEditText;

    public NotesFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static NotesFragment newInstance() {
        return new NotesFragment();
    }

    @Override
    public void onStart() {
        super.onStart();
        //notesText.requestFocus();
        //getActivity().getWindow().setSoftInputMode(
        //        WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_notes, container, false);

        LinearLayout linearLayout = (LinearLayout) rootView.findViewById(R.id.rootLayout);

        mSourceSpinner = new FormSpinner(getActivity(), getString(R.string.sourceType), null);
        mSourceSpinner.getInputControl().requestFocus();
        linearLayout.addView(mSourceSpinner.getView());
        ArrayAdapter<CharSequence> sourceAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.source_types, R.layout.spinner_item);
        sourceAdapter.setDropDownViewResource(R.layout.spinner_layout);
        //mSourceSpinner.getInputControl().setAdapter(sourceAdapter);

        mSourceSpinner.getInputControl().setAdapter(
                new NothingSelectedSpinnerAdapter(
                        sourceAdapter,
                        R.layout.spinner_unselected,
                        // R.layout.contact_spinner_nothing_selected_dropdown, // Optional
                        getActivity())
        );

        mSourceSpinner.getInputControl().setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i,
                            long l) {

                        MainApp mainApp = (MainApp) getActivity().getApplicationContext();
                        //String[] sourceArray = getResources().getStringArray(R.array.source_types);
                        Bundle bundle = mainApp.address.getExtras();
                        if (bundle == null) {
                            bundle = new Bundle();
                        }
                        bundle.putInt("sourceType", i - 1);

                        mainApp.address.setExtras(bundle);

                        //if (mMultiLineEditText.getEditText().getText().toString().isEmpty()) {
                        //showSoftKeyboard(mMultiLineEditText.getEditText());
                        //}
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                }
        );

        mMultiLineEditText = new MultiLineEditText(getActivity(), "Notes");
        //notesText = (EditText) rootView.findViewById(R.id.notesText);
        //mMultiLineEditText.getEditText().setImeActionLabel("Done", EditorInfo.IME_ACTION_DONE);
        mMultiLineEditText.getEditText().setOnEditorActionListener(this);
        linearLayout.addView(mMultiLineEditText.getView());


        /*Button mLocationButton = (Button) rootView.findViewById(R.id.mLocationButton);

        mLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onPageComplete();
            }
        });*/
/*
        TextView textView = (TextView) rootView.findViewById(R.id.section_label);
        textView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
*/
        return rootView;
    }

    public String getNotes() {
        return mMultiLineEditText.getEditText().getText().toString();

    }

    public void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mMultiLineEditText.getEditText(), InputMethodManager.SHOW_FORCED);
    }


}
