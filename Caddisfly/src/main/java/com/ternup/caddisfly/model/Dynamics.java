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

package com.ternup.caddisfly.model;

import android.util.FloatMath;

public class Dynamics {

    /**
     * Used to compare floats, if the difference is smaller than this, they are
     * considered equal
     */
    private static final float TOLERANCE = 0.01f;

    /** The amount of springiness that the dynamics has */
    private final float springiness;

    /** The damping that the dynamics has */
    private final float damping;

    /** The position the dynamics should to be at */
    private float targetPosition;

    /** The current position of the dynamics */
    private float position;

    /** The current velocity of the dynamics */
    private float velocity;

    /** The time the last update happened */
    private long lastTime;

    public Dynamics(float springiness, float dampingRatio) {
        this.springiness = springiness;
        this.damping = dampingRatio * 2 * FloatMath.sqrt(springiness);
    }

    public void setPosition(float position, long now) {
        this.position = position;
        lastTime = now;
    }

    public void setVelocity(float velocity, long now) {
        this.velocity = velocity;
        lastTime = now;
    }

    public void setTargetPosition(float targetPosition, long now) {
        this.targetPosition = targetPosition;
        lastTime = now;
    }

    public void update(long now) {
        float dt = Math.min(now - lastTime, 50) / 1000f;

        float x = position - targetPosition;
        float acceleration = -springiness * x - damping * velocity;

        velocity += acceleration * dt;
        position += velocity * dt;

        lastTime = now;
    }

    public boolean isAtRest() {
        final boolean standingStill = Math.abs(velocity) < TOLERANCE;
        final boolean isAtTarget = (targetPosition - position) < TOLERANCE;
        return standingStill && isAtTarget;
    }

    public float getPosition() {
        return position;
    }

    public float getTargetPos() {
        return targetPosition;
    }

    public float getVelocity() {
        return velocity;
    }

}
