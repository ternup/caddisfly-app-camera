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

import com.ternup.caddisfly.R;

import org.json.JSONObject;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public abstract class FormWidget {

    protected final String _property;

    protected final LinearLayout _layout;

    protected final TextView _label;

    protected View _view;

    protected String _displayText;

    protected int _priority;
    //protected FormActivity.FormWidgetToggleHandler _handler;

    protected HashMap<String, ArrayList<String>> _toggles;

    public FormWidget(Context context, String name) {
        _layout = new LinearLayout(context);
        //_layout.setLayoutParams( FormActivity.defaultLayoutParams );
        _layout.setOrientation(LinearLayout.VERTICAL);

        _property = name;
        _displayText = name.replace("_", " ");
        _displayText = toTitleCase(_displayText);

        _label = new TextView(context);
        _label.setText(getDisplayText());
        _label.setTextSize(context.getResources().getDimension(R.dimen.labelTextSize));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        params.bottomMargin = 0;
        params.topMargin = 10;

        _label.setLayoutParams(params);
        _label.setAllCaps(true);
        _label.setTextColor(context.getResources().getColor(R.color.labelTextColor));
        _layout.addView(_label);


    }

    // -----------------------------------------------
    //
    // view
    //
    // -----------------------------------------------

    /**
     * return LinearLayout containing this widget's view elements
     */
    public View getView() {
        return _layout;
    }

    /**
     * toggles the visibility of this widget
     */
    public void setVisibility(int value) {
        _layout.setVisibility(value);
    }

    // -----------------------------------------------
    //
    // set / get value
    //
    // -----------------------------------------------

    /**
     * returns value of this widget as String
     */
    public String getValue() {
        return "";
    }

    /**
     * sets value of this widget, method should be overridden in sub-class
     */
    public void setValue(String value) {
        // -- override
    }

    // -----------------------------------------------
    //
    // modifiers
    //
    // -----------------------------------------------

    /**
     * sets the hint for the widget, method should be overridden in sub-class
     */
    public void setHint(String value) {
        // -- override
    }

    /**
     * sets an object that contains keys for special properties on an object
     */
    public void setModifiers(JSONObject modifiers) {
        // -- override
    }

    // -----------------------------------------------
    //
    // set / get priority
    //
    // -----------------------------------------------

    /**
     * returns visual priority
     */
    public int getPriority() {
        return _priority;
    }

    /**
     * sets the visual priority of this widget
     * essentially this means it's physical location in the form
     */
    public void setPriority(int value) {
        _priority = value;
    }

    // -----------------------------------------------
    //
    // property name mods
    //
    // -----------------------------------------------

    /**
     * returns the un-modified name of the property this widget represents
     */
    public String getPropertyName() {
        return _property;
    }

    /**
     * returns a title case version of this property
     */
    public String getDisplayText() {
        return _displayText;
    }

    /**
     * takes a property name and modifies
     */
    public String toTitleCase(String s) {
        char[] chars = s.trim().toLowerCase().toCharArray();
        boolean found = false;

        for (int i = 0; i < chars.length; i++) {
            if (!found && Character.isLetter(chars[i])) {
                chars[i] = Character.toUpperCase(chars[i]);
                found = true;
            } else if (Character.isWhitespace(chars[i])) {
                found = false;
            }
        }

        return String.valueOf(chars);
    }

    // -----------------------------------------------
    //
    // toggles
    //
    // -----------------------------------------------

    /**
     * sets the list of toggles for this widgets
     * the structure of the data looks like this:
     * HashMap<value of property for visibility, ArrayList<list of properties to toggle on>>
     */
    public void setToggles(HashMap<String, ArrayList<String>> toggles) {
        _toggles = toggles;
    }

    /**
     * return list of widgets to toggle on
     */
    public ArrayList<String> getToggledOn() {
        if (_toggles == null) {
            return new ArrayList<String>();
        }

        if (_toggles.get(getValue()) != null) {
            return _toggles.get(getValue());
        } else {
            return new ArrayList<String>();
        }
    }

    /**
     * return list of widgets to toggle off
     */
    public ArrayList<String> getToggledOff() {
        ArrayList<String> result = new ArrayList<String>();
        if (_toggles == null) {
            return result;
        }

        Set<String> set = _toggles.keySet();

        for (String key : set) {
            if (!key.equals(getValue())) {
                ArrayList<String> list = _toggles.get(key);
                if (list == null) {
                    return new ArrayList<String>();
                }
                for (String aList : list) {
                    result.add(aList);
                }
            }
        }

        return result;
    }

/*
        public void setToggleHandler( FormActivity.FormWidgetToggleHandler handler ){
		_handler = handler;
	}
*/
}
