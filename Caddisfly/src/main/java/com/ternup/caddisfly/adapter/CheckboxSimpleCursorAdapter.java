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

import android.content.Context;
import android.database.Cursor;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;

public class CheckboxSimpleCursorAdapter extends SimpleCursorAdapter {

    public final SparseIntArray deleteList = new SparseIntArray();

    public boolean showCheckBox = false;

    public CheckboxSimpleCursorAdapter(Context context, int layout, Cursor c, String[] from,
            int[] to, int flags) {
        super(context, layout, c, from, to, flags);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        //if (convertView == null) {
        convertView = super.getView(position, convertView, parent);
        holder = new ViewHolder();
        holder.checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);
        holder.moreArrow = (ImageView) convertView.findViewById(R.id.moreArrow);

        convertView.setTag(holder);

        //} else {
        //   holder = (ViewHolder) convertView.getTag();
        //}

        if (holder.checkBox != null) {
            holder.checkBox.setOnCheckedChangeListener(null);
            holder.checkBox.setChecked(deleteList.indexOfKey(position) > -1);
            holder.checkBox
                    .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            if (b) {
                                deleteList.append(position, position);
                            } else {
                                deleteList.delete(position);
                            }
                        }
                    });

            if (showCheckBox) {
                holder.checkBox.setVisibility(View.VISIBLE);
                holder.moreArrow.setVisibility(View.GONE);
            } else {
                holder.checkBox.setVisibility(View.GONE);
                holder.moreArrow.setVisibility(View.VISIBLE);
            }
        }
        return convertView;
    }

    static class ViewHolder {

        CheckBox checkBox;

        ImageView moreArrow;
    }
}
