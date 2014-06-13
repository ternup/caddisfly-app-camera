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

package com.ternup.caddisfly.view;

import com.ternup.caddisfly.model.Dynamics;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.View;
import android.view.animation.AnimationUtils;

public class LineChartView extends View {

    private static final int MIN_LINES = 3;

    private static final int MAX_LINES = 8;

    private static final int[] DISTANCES = {1, 2, 5};

    private static final float GRAPH_SMOOTHNESS = 0.15f;

    private static final float RATIO = 4f / 4f;

    private final Paint paint = new Paint();

    private Dynamics[] dataPoints;

    private final Runnable animator = new Runnable() {
        @Override
        public void run() {
            boolean needNewFrame = false;
            long now = AnimationUtils.currentAnimationTimeMillis();
            for (Dynamics dynamics : dataPoints) {
                dynamics.update(now);
                if (!dynamics.isAtRest()) {
                    needNewFrame = true;
                }
            }
            if (needNewFrame) {
                postDelayed(this, 20);
            }
            invalidate();
        }
    };

    public LineChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Sets the y data points of the line chart. The data points are assumed to
     * be positive and equally spaced on the x-axis. The line chart will be
     * scaled so that the entire height of the view is used.
     *
     * @param newDataPoints y values of the line chart
     */
    public void setChartData(float[] newDataPoints) {
        long now = AnimationUtils.currentAnimationTimeMillis();
        if (dataPoints == null || dataPoints.length != newDataPoints.length) {
            dataPoints = new Dynamics[newDataPoints.length];
            for (int i = 0; i < newDataPoints.length; i++) {
                dataPoints[i] = new Dynamics(1.1f, 0.30f);
                dataPoints[i].setPosition(newDataPoints[i], now);
                dataPoints[i].setTargetPosition(newDataPoints[i], now);
            }
            invalidate();
        } else {
            for (int i = 0; i < newDataPoints.length; i++) {
                dataPoints[i].setTargetPosition(newDataPoints[i], now);
            }
            removeCallbacks(animator);
            post(animator);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int widthWithoutPadding = width - getPaddingLeft() - getPaddingRight();
        int heightWithoutPadding = height - getPaddingTop() - getPaddingBottom();

        int maxWidth = (int) (heightWithoutPadding * RATIO);
        int maxHeight = (int) (widthWithoutPadding / RATIO);

        if (widthWithoutPadding > maxWidth) {
            width = maxWidth + getPaddingLeft() + getPaddingRight();
        } else {
            height = maxHeight + getPaddingTop() + getPaddingBottom();
        }

        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY) {
            width = getMeasuredWidth();
        }
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) {
            height = getMeasuredHeight();
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float maxValue = getMax(dataPoints);
        if (dataPoints != null && dataPoints.length > 0) {
            drawBackground(canvas, maxValue);
            drawLineChart(canvas, maxValue);
        }
    }

    private void drawBackground(Canvas canvas, float maxValue) {
        int range = getLineDistance(maxValue);
        paint.setStyle(Style.FILL);
        paint.setColor(Color.GRAY);
        paint.setTextAlign(Align.LEFT);
        paint.setTextSize(16);
        paint.setStrokeWidth(1);
        for (int y = 0; y < maxValue; y += range) {
            final int yPos = (int) getYPos(y, maxValue);

            // turn off anti alias for lines, they get crisper then
            paint.setAntiAlias(false);
            canvas.drawLine(0, yPos, getWidth(), yPos, paint);

            // turn on anti alias again for the text
            paint.setAntiAlias(true);
            canvas.drawText(String.valueOf(y), getPaddingLeft(), yPos - 2, paint);
        }
    }

    private int getLineDistance(float maxValue) {
        long distance;
        int distanceIndex = 0;
        long distanceMultiplier = 1;
        int numberOfLines;

        do {
            distance = DISTANCES[distanceIndex] * distanceMultiplier;
            numberOfLines = (int) FloatMath.ceil(maxValue / distance);

            distanceIndex++;
            if (distanceIndex == DISTANCES.length) {
                distanceIndex = 0;
                distanceMultiplier *= 10;

            }
        } while (numberOfLines < MIN_LINES || numberOfLines > MAX_LINES);

        return (int) distance;
    }

    private void drawLineChart(Canvas canvas, float maxValue) {
        Path path = createSmoothPath(maxValue);

        paint.setStyle(Style.STROKE);
        paint.setStrokeWidth(4);
        paint.setColor(0xFF33B5E5);
        paint.setAntiAlias(true);
        paint.setShadowLayer(4, 2, 2, 0x81000000);
        canvas.drawPath(path, paint);
        paint.setShadowLayer(0, 0, 0, 0);
    }

    private Path createSmoothPath(float maxValue) {

        Path path = new Path();
        path.moveTo(getXPos(0), getYPos(dataPoints[0].getPosition(), maxValue));
        for (int i = 0; i < dataPoints.length - 1; i++) {
            float thisPointX = getXPos(i);
            float thisPointY = getYPos(dataPoints[i].getPosition(), maxValue);
            float nextPointX = getXPos(i + 1);
            float nextPointY = getYPos(dataPoints[si(i + 1)].getPosition(), maxValue);

            float startdiffX = (nextPointX - getXPos(si(i - 1)));
            float startdiffY = (nextPointY - getYPos(dataPoints[si(i - 1)].getPosition(),
                    maxValue));
            float endDiffX = (getXPos(si(i + 2)) - thisPointX);
            float endDiffY = (getYPos(dataPoints[si(i + 2)].getPosition(), maxValue) - thisPointY);

            float firstControlX = thisPointX + (GRAPH_SMOOTHNESS * startdiffX);
            float firstControlY = thisPointY + (GRAPH_SMOOTHNESS * startdiffY);
            float secondControlX = nextPointX - (GRAPH_SMOOTHNESS * endDiffX);
            float secondControlY = nextPointY - (GRAPH_SMOOTHNESS * endDiffY);

            path.cubicTo(firstControlX, firstControlY, secondControlX, secondControlY, nextPointX,
                    nextPointY);
        }
        return path;
    }

    /**
     * Given an index in dataPoints, it will make sure the the returned index is
     * within the array
     */
    private int si(int i) {
        if (i > dataPoints.length - 1) {
            return dataPoints.length - 1;
        } else if (i < 0) {
            return 0;
        }
        return i;
    }

    private float getMax(Dynamics[] array) {
        float max = 0;
        if (array != null) {
            max = array[0].getPosition();
            for (int i = 1; i < array.length; i++) {
                if (array[i].getPosition() > max) {
                    max = array[i].getPosition();
                }
            }
        }
        return max;
    }

    private float getYPos(float value, float maxValue) {
        float height = getHeight() - getPaddingTop() - getPaddingBottom();

        // scale it to the view size
        value = (value / maxValue) * height;

        // invert it so that higher values have lower y
        value = height - value;

        // offset it to adjust for padding
        value += getPaddingTop();

        return value;
    }

    private float getXPos(float value) {
        float width = getWidth() - getPaddingLeft() - getPaddingRight();
        float maxValue = dataPoints.length - 1;

        // scale it to the view size
        value = (value / maxValue) * width;

        // offset it to adjust for padding
        value += getPaddingLeft();

        return value;
    }

}
