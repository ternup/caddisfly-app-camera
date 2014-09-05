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

package com.ternup.caddisfly.util;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

public class ShowCamera extends SurfaceView implements SurfaceHolder.Callback {

    private final Camera.Parameters params;

    private Camera mCamera;

    public ShowCamera(Context context, Camera camera) {
        super(context);

        mCamera = camera;
        params = mCamera.getParameters();
        setCameraDisplayOrientation((Activity) context, 0, mCamera);
        SurfaceHolder holdMe = getHolder();
        assert holdMe != null;
        holdMe.addCallback(this);
    }


    private static void setCameraDisplayOrientation(Activity activity,
                                                    int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }


    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            mCamera.setParameters(params);
            mCamera.setPreviewDisplay(holder);
            mCamera.stopPreview();
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    public void setZoom(int i) {
        //this.setZoom(i);

        params.setZoom(Math.min(i, params.getMaxZoom()));
        mCamera.setParameters(params);
        //mCamera.startSmoothZoom(i);
    }

    public int getMaxZoom() {

        Camera.Parameters parameters = mCamera.getParameters();
        return parameters.getMaxZoom();

    }

    public void setMaxZoom() {
        params.setZoom(params.getMaxZoom());
        mCamera.setParameters(params);

    }
}