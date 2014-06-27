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

package com.ternup.caddisfly.widget;


import com.ternup.caddisfly.util.TextJustifyUtils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.util.AttributeSet;
import android.widget.TextView;

/*
 * 
 * TextViewEx.java
 * @author Mathew Kurian
 * 
 * !-- Requires -- !
 * TextJustifyUtils.java
 * 
 * From TextJustify-Android Library v1.0.2
 * https://github.com/bluejamesbond/TextJustify-Android
 *
 * Please report any issues
 * https://github.com/bluejamesbond/TextJustify-Android/issues
 * 
 * Date: 12/13/2013 12:28:16 PM
 * 
 */


public class TextViewEx extends TextView {

    private final Paint paint = new Paint();

    private boolean wrapEnabled = false;

    private Align _align = Align.LEFT;

    private Bitmap cache = null;

    private boolean cacheEnabled = false;

    public TextViewEx(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        //set a minimum of left and right padding so that the texts are not too close to the side screen
        //this.setPadding(10, 0, 10, 0);
    }

    public TextViewEx(Context context, AttributeSet attrs) {
        super(context, attrs);
        //this.setPadding(10, 0, 10, 0);
    }

    public TextViewEx(Context context) {
        super(context);
        //this.setPadding(10, 0, 10, 0);
    }

    @Override
    public void setDrawingCacheEnabled(boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
    }

    public void setText(String st, boolean wrap) {
        wrapEnabled = wrap;
        super.setText(st);
    }

    @SuppressLint("NewApi")
    @Override
    protected void onDraw(Canvas canvas) {
        // If wrap is disabled then,
        // request original onDraw
        if (!wrapEnabled) {
            super.onDraw(canvas);
            return;
        }

        // Active canvas needs to be set
        // based on cacheEnabled
        Canvas activeCanvas;

        // Set the active canvas based on
        // whether cache is enabled
        if (cacheEnabled) {

            if (cache != null) {
                // Draw to the OS provided canvas
                // if the cache is not empty
                canvas.drawBitmap(cache, 0, 0, paint);
                return;
            } else {
                // Create a bitmap and set the activeCanvas
                // to the one derived from the bitmap
                cache = Bitmap.createBitmap(getWidth(), getHeight(), Config.ARGB_8888);
                activeCanvas = new Canvas(cache);
            }
        } else {
            // Active canvas is the OS
            // provided canvas
            activeCanvas = canvas;
        }

        // Pull widget properties
        paint.setColor(getCurrentTextColor());
        paint.setTypeface(getTypeface());
        paint.setTextSize(getTextSize());
        paint.setTextAlign(_align);
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);

        //exclude the padding
        float dirtyRegionWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        int maxLines = Integer.MAX_VALUE;
        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentApiVersion >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            maxLines = getMaxLines();
        }
        int lines = 1;
        String[] blocks = getText().toString().split("((?<=\n)|(?=\n))");
        float horizontalFontOffset;
        float verticalOffset = horizontalFontOffset = getLineHeight() - 0.5f;
        float spaceOffset = paint.measureText(" ");

        for (int i = 0; i < blocks.length && lines <= maxLines; i++) {
            String block = blocks[i];
            float horizontalOffset = 0;

            if (block.length() == 0) {
                continue;
            } else if (block.equals("\n")) {
                verticalOffset += horizontalFontOffset;
                continue;
            }

            block = block.trim();

            if (block.length() == 0) {
                continue;
            }

            Object[] wrappedObj = TextJustifyUtils
                    .createWrappedLine(block, paint, spaceOffset, dirtyRegionWidth);

            String wrappedLine = ((String) wrappedObj[0]);
            float wrappedEdgeSpace = (Float) wrappedObj[1];
            String[] lineAsWords = wrappedLine.split(" ");
            float stretchOffset = wrappedEdgeSpace != Float.MIN_VALUE ? wrappedEdgeSpace / (
                    lineAsWords.length - 1) : 0;

            for (int j = 0; j < lineAsWords.length; j++) {
                String word = lineAsWords[j];
                if (lines == maxLines && j == lineAsWords.length - 1) {
                    activeCanvas.drawText("...", horizontalOffset, verticalOffset, paint);


                } else if (j == 0) {
                    //if it is the first word of the line, text will be drawn starting from right edge of textView
                    if (_align == Align.RIGHT) {
                        activeCanvas
                                .drawText(word, getWidth() - (getPaddingRight()), verticalOffset,
                                        paint);
                        // add in the padding to the horizontalOffset
                        horizontalOffset += getWidth() - (getPaddingRight());
                    } else {
                        activeCanvas.drawText(word, getPaddingLeft(), verticalOffset, paint);
                        horizontalOffset += getPaddingLeft();
                    }

                } else {
                    activeCanvas.drawText(word, horizontalOffset, verticalOffset, paint);
                }
                if (_align == Align.RIGHT) {
                    horizontalOffset -= paint.measureText(word) + spaceOffset + stretchOffset;
                } else {
                    horizontalOffset += paint.measureText(word) + spaceOffset + stretchOffset;
                }
            }

            lines++;

            if (blocks[i].length() > 0) {
                blocks[i] = blocks[i].substring(wrappedLine.length());
                verticalOffset += blocks[i].length() > 0 ? horizontalFontOffset : 0;
                i--;
            }
        }

        if (cacheEnabled) {
            // Draw the cache onto the OS provided
            // canvas.
            canvas.drawBitmap(cache, 0, 0, paint);
        }
    }
}
