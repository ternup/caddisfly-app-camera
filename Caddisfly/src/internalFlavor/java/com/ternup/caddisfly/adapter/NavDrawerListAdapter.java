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

package com.ternup.caddisfly.adapter;

import com.ternup.caddisfly.R;
import com.ternup.caddisfly.app.MainApp;
import com.ternup.caddisfly.model.NavigationDrawerItem;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class NavDrawerListAdapter extends BaseAdapter {

    private final Context context;

    private final ArrayList<NavigationDrawerItem> navigationDrawerItems;

    public NavDrawerListAdapter(Context context,
            ArrayList<NavigationDrawerItem> navigationDrawerItems) {
        this.context = context;
        this.navigationDrawerItems = navigationDrawerItems;
    }

    @Override
    public int getCount() {
        return navigationDrawerItems.size();
    }

    @Override
    public Object getItem(int position) {
        return navigationDrawerItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.row_drawer, parent, false);
        }

        assert convertView != null;
        ImageView iconImage = (ImageView) convertView.findViewById(R.id.icon);
        TextView titleText = (TextView) convertView.findViewById(R.id.title);
        View lineView = convertView.findViewById(R.id.lineView);

        //TODO : hardcoded position to be fixed
        if (position == 3) {
            lineView.setVisibility(View.VISIBLE);
        }

        assert context.getApplicationContext() != null;
        MainApp mainApp = (MainApp) context.getApplicationContext();

        try {

            TypedArray a = context.getTheme().obtainStyledAttributes(
                    mainApp.CurrentTheme, new int[]{R.attr.drawerText});
            assert a != null;
            int attributeResourceId = a.getResourceId(0, 0);
            titleText.setTextColor(context.getResources().getColor(attributeResourceId));
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }

        try {
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    mainApp.CurrentTheme, new int[]{R.attr.drawerSelector});
            assert a != null;
            int attributeResourceId = a.getResourceId(0, 0);
            convertView.setBackgroundResource(attributeResourceId);

            //convertView.setBackground(context.getResources().getDrawable(attributeResourceId));
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }

        iconImage.setImageResource(navigationDrawerItems.get(position).getIcon());
        titleText.setText(navigationDrawerItems.get(position).getTitle());

        return convertView;
    }

}
