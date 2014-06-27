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
import com.ternup.caddisfly.app.Globals;
import com.ternup.caddisfly.util.PreferencesUtils;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Reference: Take a picture directly from inside the app - Rex St.John
 * Reference: http://developer.android.com/guide/topics/media/camera.html#custom-camera
 * Reference: http://stackoverflow.com/questions/7942378/android-camera-will-not-work-startPreview-fails
 */

/**
 * Camera Preview Fragment
 */
public class CameraFragment extends DialogFragment {

    private final Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        public void onShutter() {
            AudioManager mgr = (AudioManager) getActivity()
                    .getSystemService(Context.AUDIO_SERVICE);
            mgr.playSoundEffect(AudioManager.FLAG_PLAY_SOUND);
        }
    };

    public Boolean makeShutterSound = false;

    public Camera.PictureCallback pictureCallback;

    private AlertDialog progressDialog;

    private Camera mCamera;

    // View to display the camera output.
    private CameraPreview mPreview;

    public CameraFragment() {
        super();
    }

    public static CameraFragment newInstance() {
        return new CameraFragment();
    }

    private static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        if (PreferencesUtils.getBoolean(getActivity(), R.string.autoAnalyzeKey, true)) {
            getDialog().setTitle(R.string.analysisInProgress);
        } else {
            getDialog().setTitle(R.string.clickAnalyze);
        }

        final View view = inflater.inflate(R.layout.fragment_camera, container, false);

        // Create preview and set it as the content
        boolean opened = safeCameraOpenInView(view);

        if (!opened) {
            return view;
        }

        Button captureButton = (Button) view.findViewById(R.id.button_capture);
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startTakingPictures();
                    }
                }
        );
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (PreferencesUtils.getBoolean(getActivity(), R.string.autoAnalyzeKey, true)) {
            startTakingPictures();
        }
    }

    private void startTakingPictures() {

        Context context = getActivity();
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(getString(R.string.analyzingWait));
        progressDialog.setCancelable(false);

        //ProgressDialog.Builder builder = new ProgressDialog.Builder(context);
        //builder.setTitle(R.string.working);
        //builder.setMessage(R.string.analyzingWait);

        //progressDialog = builder.create();
        progressDialog.getWindow().setGravity(Gravity.BOTTOM);
        progressDialog.show();
        //progressDialog.setCancelable(false);

        Camera.Parameters parameters = mCamera.getParameters();

        if (PreferencesUtils.getBoolean(getActivity(), R.string.cameraTorchKey, false)) {
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        } else {
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
        }

        mCamera.setParameters(parameters);

        takePicture();
    }

    private void takePicture() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                final boolean shutterSound = PreferencesUtils
                        .getBoolean(getActivity(), R.string.cameraSoundKey, true);
                mPreview.startCameraPreview();
                PictureCallback localCallback = new PictureCallback();
                try {
                    if (shutterSound && (makeShutterSound || hasTestCompleted(getActivity()))) {
                        mCamera.takePicture(shutterCallback, null, localCallback);
                    } else {
                        mCamera.takePicture(null, null, localCallback);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, Globals.INITIAL_DELAY);

    }

    private boolean hasTestCompleted(Context context) {
        //TODO remove this code
        if (makeShutterSound) {
            return true;
        }

        int currentSamplingCount = PreferencesUtils
                .getInt(context, R.string.currentSamplingCountKey, 0);
        int samplingCount = PreferencesUtils
                .getInt(context, R.string.samplingCountKey, Globals.SAMPLING_COUNT_DEFAULT);
        return currentSamplingCount >= (samplingCount - 1);
    }

    private boolean safeCameraOpenInView(View view) {
        boolean qOpened;
        releaseCameraAndPreview();
        mCamera = getCameraInstance();
        qOpened = (mCamera != null);

        if (qOpened) {
            mPreview = new CameraPreview(getActivity().getBaseContext(), mCamera);
            FrameLayout preview = (FrameLayout) view.findViewById(R.id.camera_preview);
            preview.addView(mPreview);
            mPreview.startCameraPreview();
        }
        return qOpened;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseCameraAndPreview();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        if (getActivity() != null && getActivity() instanceof Cancelled) {
            ((Cancelled) getActivity()).dialogCancelled();
        }
        super.onCancel(dialog);
    }

    private void releaseCameraAndPreview() {

        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        if (mPreview != null) {
            mPreview.destroyDrawingCache();
            mPreview.mCamera = null;
        }
    }

    public interface Cancelled {

        public void dialogCancelled();
    }

    static class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

        private final SurfaceHolder mHolder;

        private Camera mCamera;

        private Camera.Size mPreviewSize;

        private List<Camera.Size> mSupportedPreviewSizes;

        private List<String> mSupportedFlashModes;

        public CameraPreview(Context context, Camera camera) {
            super(context);

            setCamera(context, camera);

            mHolder = getHolder();
            mHolder.addCallback(this);
            mHolder.setKeepScreenOn(true);
        }

        public void startCameraPreview() {
            try {
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void setCamera(Context context, Camera camera) {
            mCamera = camera;
            mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
            mSupportedFlashModes = mCamera.getParameters().getSupportedFlashModes();
            Camera.Parameters parameters = mCamera.getParameters();

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
            parameters.setZoom(0);

            if (parameters.getMaxNumMeteringAreas() > 0) {
                List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();
                Rect areaRect1 = new Rect(-100, -100, 100, 100);
                meteringAreas.add(new Camera.Area(areaRect1, 1000));
                parameters.setMeteringAreas(meteringAreas);
            }

            if (!PreferencesUtils.getBoolean(context, R.string.autoAnalyzeKey, false)) {
                if (mSupportedFlashModes != null && mSupportedFlashModes
                        .contains(Camera.Parameters.FLASH_MODE_ON)) {
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                }
            }

            mCamera.setDisplayOrientation(90);
            //if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            //  mCamera.enableShutterSound(false);
            //}
            mCamera.setParameters(parameters);

            requestLayout();
        }

        public void surfaceCreated(SurfaceHolder holder) {
            try {
                mCamera.setPreviewDisplay(holder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            if (mCamera != null) {
                mCamera.stopPreview();
            }
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {

            if (mHolder.getSurface() == null) {
                return;
            }

            try {
                Camera.Parameters parameters = mCamera.getParameters();

                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

                if (mPreviewSize != null) {
                    Camera.Size previewSize = mPreviewSize;
                    parameters.setPreviewSize(previewSize.width, previewSize.height);
                }

                mCamera.setParameters(parameters);
                mCamera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
            final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);

            //final int width = Globals.IMAGE_SAMPLE_LENGTH;
            //final int height = Globals.IMAGE_SAMPLE_LENGTH;
            //final int length = Math.min(width, height);

            setMeasuredDimension(width, height);

            if (mSupportedPreviewSizes != null) {
                mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
            }
        }

        private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int width, int height) {
            Camera.Size optimalSize = null;

            final double ASPECT_TOLERANCE = 0.1;
            double targetRatio = (double) height / width;

            for (Camera.Size size : sizes) {

                if (size.height != width) {
                    continue;
                }
                double ratio = (double) size.width / size.height;
                if (ratio <= targetRatio + ASPECT_TOLERANCE
                        && ratio >= targetRatio - ASPECT_TOLERANCE) {
                    optimalSize = size;
                }
            }

            return optimalSize;
        }
    }

    private class PictureCallback implements Camera.PictureCallback {

        @Override
        public void onPictureTaken(byte[] bytes, Camera camera) {
            if (!hasTestCompleted(getActivity())) {
                pictureCallback.onPictureTaken(bytes, camera);
                takePicture();
            } else {
                pictureCallback.onPictureTaken(bytes, camera);
            }
        }
    }
}
