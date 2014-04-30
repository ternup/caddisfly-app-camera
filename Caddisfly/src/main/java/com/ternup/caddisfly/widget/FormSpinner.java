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

package com.ternup.caddisfly.widget;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

import java.util.HashMap;
import java.util.Map;

public class FormSpinner extends FormWidget {

    protected final JSONObject _options;

    protected final Spinner _spinner;

    protected final Map<String, String> _propertyMap;

    protected final ArrayAdapter<String> _adapter;


    public FormSpinner(Context context, String property, JSONObject options) {
        super(context, property);

        _options = options;

        _spinner = new Spinner(context, Spinner.MODE_DIALOG);
        //_spinner.setLayoutParams( FormActivity.defaultLayoutParams );

        String p;
        String name;

        _propertyMap = new HashMap<String, String>();
        _adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item);
        _adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        _spinner.setAdapter(_adapter);
        _spinner.setSelection(0);

        try {
            if (options != null) {
                JSONArray propertyNames = options.names();
                for (int i = 0; i < options.length(); i++) {
                    name = propertyNames.getString(i);
                    p = options.getString(name);

                    _adapter.add(p);
                    _propertyMap.put(p, name);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        params1.bottomMargin = 15;
        params1.topMargin = 0;

        _spinner.setLayoutParams(params1);

        _layout.addView(_spinner);
    }

    @Override
    public String getValue() {
        return _propertyMap.get(_adapter.getItem(_spinner.getSelectedItemPosition()));
    }

    @Override
    public void setValue(String value) {
        try {
            String name;
            JSONArray names = _options.names();
            for (int i = 0; i < names.length(); i++) {
                name = names.getString(i);

                if (name.equals(value)) {
                    String item = _options.getString(name);
                    _spinner.setSelection(_adapter.getPosition(item));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Spinner getInputControl() {

        return _spinner;
    }

    /*
            @Override
            public void setToggleHandler( FormActivity.FormWidgetToggleHandler handler )
            {
                    super.setToggleHandler(handler);
                    _spinner.setOnItemSelectedListener( new SelectionHandler( this ) );
            }
    */
    class SelectionHandler implements AdapterView.OnItemSelectedListener {

        protected final FormWidget _widget;

        public SelectionHandler(FormWidget widget) {
            _widget = widget;
        }

        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
/*
                        if( _handler != null ){
				_handler.toggle( _widget );
			}
*/
        }

        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub
        }

    }
}
