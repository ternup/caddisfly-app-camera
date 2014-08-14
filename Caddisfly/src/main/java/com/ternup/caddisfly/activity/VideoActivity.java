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

package com.ternup.caddisfly.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.MediaController;
import android.widget.VideoView;

import com.ternup.caddisfly.R;
import com.ternup.caddisfly.activity.util.SystemUiHider;

import java.io.File;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class VideoActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_video);
        File sdDir = this.getExternalFilesDir(null);
        File videoFile = new File(sdDir, "training.mp4");
        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int height = dm.heightPixels;
        int width = dm.widthPixels;

        final VideoView videoHolder = (VideoView) this.findViewById(R.id.video_player_view);
        videoHolder.setMinimumWidth(width);
        videoHolder.setMinimumHeight(height);

        //getWindow().setFormat(PixelFormat.TRANSLUCENT);
        videoHolder.setMediaController(new MediaController(this));
        videoHolder.setVideoPath(videoFile.getAbsolutePath());
        videoHolder.requestFocus();
        videoHolder.start();


    }

}
