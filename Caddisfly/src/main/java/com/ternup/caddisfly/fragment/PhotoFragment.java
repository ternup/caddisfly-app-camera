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
import com.ternup.caddisfly.activity.PhotoIntentActivity;
import com.ternup.caddisfly.util.FileUtils;
import com.ternup.caddisfly.util.ImageUtils;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

public class PhotoFragment extends BaseFragment {

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final int REQUEST_PHOTO = 6;

    ImageView mPhotoImageView;

    public PhotoFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static PhotoFragment newInstance() {
        return new PhotoFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_photo, container, false);
        Button photoButton = (Button) rootView.findViewById(R.id.photoButton);

        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), PhotoIntentActivity.class);
                intent.putExtra(getString(R.string.currentLocationId), (long) 0);
                startActivityForResult(intent, REQUEST_PHOTO);
            }
        });

        mPhotoImageView = (ImageView) rootView.findViewById(R.id.photoImageView);
        return rootView;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_PHOTO:
                mPhotoImageView
                        .setImageBitmap(
                                ImageUtils.decodeFile(
                                        String.format("%sphoto", FileUtils
                                                .getStoragePath(getActivity(), 0, "", true))
                                )
                        );
                break;
        }
    }

}
