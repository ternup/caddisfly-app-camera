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

import com.ternup.caddisfly.R;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;

public class CameraUtils {

    private CameraUtils() {
    }

    public static Camera getCamera(final Context context) {

        // do we have a camera?
        //if (!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA))
        // {
        //  Toast.makeText(getActivity(), "No camera on this device", Toast.LENGTH_LONG).show();
        //} else {
        int cameraId = findBackFacingCamera();
        if (cameraId < 0) {
            //Toast.makeText(getActivity(), "No front facing camera found.",
            //      Toast.LENGTH_LONG).show();
            return null;
        } else {

            Camera camera = Camera.open(cameraId);

            /*if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
                camera.enableShutterSound(true);
            }*/

            Camera.Parameters parameters = camera.getParameters();

            if (PreferencesUtils.getBoolean(context, R.string.cameraTorchKey, false)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            } else {
                parameters.setFlashMode("on"); //NON-NLS
            }
            if (PreferencesUtils.getBoolean(context, R.string.cameraCloudyKey, true)) {
                parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_CLOUDY_DAYLIGHT);
            } else {
                parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_DAYLIGHT);
            }

            parameters.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
            parameters.setColorEffect(Camera.Parameters.EFFECT_NONE);

            if (PreferencesUtils.getBoolean(context, R.string.cameraInfinityKey, false)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
            }

            parameters.setPictureFormat(ImageFormat.JPEG);
            parameters.setJpegQuality(100);

            //parameters.setPictureSize(500, 500);

            camera.setParameters(parameters);

            return camera;
        }
        //}
    }

    private static int findBackFacingCamera() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                //Log.d(DEBUG_TAG, "Camera found");
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }
}
