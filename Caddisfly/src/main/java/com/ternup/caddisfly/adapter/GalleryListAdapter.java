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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GalleryListAdapter extends BaseAdapter {

    private static final int IMAGE_SIDE_LENGTH = 80;

    private final LayoutInflater mInflater;

    private final Activity mActivity;

    private ArrayList<String> mFilePaths = new ArrayList<String>();

    public GalleryListAdapter(Activity activity, ArrayList<String> filePaths) {
        this.mInflater = activity.getLayoutInflater();
        this.mFilePaths = filePaths;
        this.mActivity = activity;
    }

    /*
     * Resizing image size
     */
    private static Bitmap decodeFile(String filePath) {
        try {

            File f = new File(filePath);

            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);

            int scale = 1;
            while (o.outWidth / scale / 2 >= GalleryListAdapter.IMAGE_SIDE_LENGTH
                    && o.outHeight / scale / 2 >= GalleryListAdapter.IMAGE_SIDE_LENGTH) {
                scale *= 2;
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int getCount() {
        return this.mFilePaths.size();
    }

    @Override
    public Object getItem(int position) {
        return this.mFilePaths.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        final String file = mFilePaths.get(position);

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.row_gallery, parent, false);
            holder = new ViewHolder();
            holder.serialNumber = (TextView) convertView.findViewById(R.id.serialNumberText);
            holder.icon = (ImageView) convertView.findViewById(R.id.photoImageView);
            holder.timestamp = (TextView) convertView.findViewById(R.id.dateText);
            holder.progress = (ProgressBar) convertView.findViewById(R.id.progressBar);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.position = position;

        // Using an AsyncTask to load the slow images in a background thread
        (new AsyncTask<ViewHolder, Void, Bitmap>() {
            private ViewHolder v;

            @Override
            protected Bitmap doInBackground(ViewHolder... params) {
                v = params[0];

                if (!file.isEmpty()) {
                    return decodeFile(file);
                }
                return null;
            }

            @SuppressLint("SimpleDateFormat") // Using SimpleDateFormat to display seconds also
            @Override
            protected void onPostExecute(Bitmap result) {
                super.onPostExecute(result);
                if (v.position == position) {
                    v.serialNumber.setText(String.valueOf(position + 1));
                    v.progress.setVisibility(View.GONE);
                    v.icon.setVisibility(View.VISIBLE);
                    v.icon.setImageBitmap(result);
                    //v.icon.setScaleType(ImageView.ScaleType.CENTER_CROP);

                    Pattern pattern = Pattern
                            .compile("pic-(\\d{4})(\\d{2})(\\d{2})(\\d{2})(\\d{2})(\\d{2})");
                    Matcher matcher = pattern.matcher(file);
                    if (matcher.find()) {
                        int year = Integer.parseInt(matcher.group(1));
                        int month = Integer.parseInt(matcher.group(2)) - 1;
                        int day = Integer.parseInt(matcher.group(3));
                        int hour = Integer.parseInt(matcher.group(4));
                        int minute = Integer.parseInt(matcher.group(5));
                        int second = Integer.parseInt(matcher.group(6));

                        Calendar cal = Calendar.getInstance();

                        //noinspection MagicConstant
                        cal.set(year, month, day, hour, minute, second);
                        boolean is24HourFormat = android.text.format.DateFormat
                                .is24HourFormat(mActivity);
                        String timePattern = mActivity.getString(
                                is24HourFormat ? R.string.twentyFourHourTime
                                        : R.string.twelveHourTime
                        );
                        DateFormat tf = new SimpleDateFormat(timePattern);
                        v.timestamp.setText(tf.format(cal.getTime()));
                    }
                }
            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, holder);
        return convertView;
    }

    static class ViewHolder {

        TextView serialNumber;

        TextView timestamp;

        ImageView icon;

        ProgressBar progress;

        int position;
    }
}
