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

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

public class ShakeDetector implements SensorEventListener {

    // Minimum acceleration needed to count as a shake movement
    private static final int MIN_SHAKE_ACCELERATION = 5;

    // Max to determine if the phone is not moving
    private static final float MAX_SHAKE_ACCELERATION = 0.05f;

    // Minimum number of movements to register a shake
    private static final int MIN_MOVEMENTS = 10;

    // Maximum time (in milliseconds) for the whole shake to occur
    private static final int MAX_SHAKE_DURATION = 2000;

    // Indexes for x, y, and z values
    private static final int X = 0;

    private static final int Y = 1;

    private static final int Z = 2;

    // Arrays to store gravity and linear acceleration values
    private final float[] mGravity = {0.0f, 0.0f, 0.0f};

    private final float[] mLinearAcceleration = {0.0f, 0.0f, 0.0f};

    // OnShakeListener that will be notified when the shake is detected
    private final OnShakeListener mShakeListener;

    // OnShakeListener that will be notified when the no shake is detected
    private final OnNoShakeListener mNoShakeListener;

    // Start time for the shake detection
    private long startTime = 0;

    private long noShakeStartTime = 0;

    // Counter for shake movements
    private int moveCount = 0;

    private long previousNoShake = 0;

    // Constructor that sets the shake listener
    public ShakeDetector(OnShakeListener shakeListener, OnNoShakeListener noShakeListener) {
        mShakeListener = shakeListener;
        mNoShakeListener = noShakeListener;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // This method will be called when the accelerometer detects a change.

        setCurrentAcceleration(event);

        // Get the max linear acceleration in any direction
        float maxLinearAcceleration = getMaxCurrentLinearAcceleration();

        synchronized (this) {

            long nowNoShake = System.currentTimeMillis();
            if (Math.abs(maxLinearAcceleration) < MAX_SHAKE_ACCELERATION) {
                long elapsedNoShakeTime = nowNoShake - noShakeStartTime;

                if (elapsedNoShakeTime > MAX_SHAKE_DURATION) {
                    elapsedNoShakeTime = 0;
                    noShakeStartTime = nowNoShake;

                    if (System.currentTimeMillis() - previousNoShake > 400) {
                        previousNoShake = System.currentTimeMillis();
                        mNoShakeListener.onNoShake();
                    }
                }
            } else {
                noShakeStartTime = nowNoShake;
            }
        }

        synchronized (this) {

            // Check if the acceleration is greater than our minimum threshold
            if (maxLinearAcceleration > MIN_SHAKE_ACCELERATION) {
                long now = System.currentTimeMillis();

                // Set the startTime if it was reset to zero
                if (startTime == 0) {
                    startTime = now;
                }

                long elapsedTime = now - startTime;

                // Check if we're still in the shake window we defined
                if (elapsedTime > MAX_SHAKE_DURATION) {
                    // Too much time has passed. Start over!
                    resetShakeDetection();
                } else {
                    // Keep track of all the movements
                    moveCount++;

                    // Check if enough movements have been made to qualify as a shake
                    if (moveCount > MIN_MOVEMENTS) {
                        // Reset for the next one!
                        resetShakeDetection();

                        // It's a shake! Notify the listener.
                        mShakeListener.onShake();
                    }
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void setCurrentAcceleration(SensorEvent event) {
           /*
         *  BEGIN SECTION from Android developer site. This code accounts for
    	 *  gravity using a high-pass filter
    	 */

        final float alpha = 0.8f;

        // Gravity components of x, y, and z acceleration
        mGravity[X] = alpha * mGravity[X] + (1 - alpha) * event.values[X];
        mGravity[Y] = alpha * mGravity[Y] + (1 - alpha) * event.values[Y];
        mGravity[Z] = alpha * mGravity[Z] + (1 - alpha) * event.values[Z];

        // Linear acceleration along the x, y, and z axes (gravity effects removed)
        mLinearAcceleration[X] = event.values[X] - mGravity[X];
        mLinearAcceleration[Y] = event.values[Y] - mGravity[Y];
        mLinearAcceleration[Z] = event.values[Z] - mGravity[Z];

        /*
         *  END SECTION from Android developer site
         */
    }

    private float getMaxCurrentLinearAcceleration() {
        // Start by setting the value to the x value
        float maxLinearAcceleration = mLinearAcceleration[X];

        // Check if the y value is greater
        if (mLinearAcceleration[Y] > maxLinearAcceleration) {
            maxLinearAcceleration = mLinearAcceleration[Y];
        }

        // Check if the z value is greater
        if (mLinearAcceleration[Z] > maxLinearAcceleration) {
            maxLinearAcceleration = mLinearAcceleration[Z];
        }

        // Return the greatest value
        return maxLinearAcceleration;
    }

    private void resetShakeDetection() {
        startTime = 0;
        moveCount = 0;
    }

    public interface OnShakeListener {

        public void onShake();
    }

    public interface OnNoShakeListener {

        public void onNoShake();
    }

}
