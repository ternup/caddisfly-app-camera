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

package com.ternup.caddisfly.test;

import com.ternup.caddisfly.app.MainApp;
import com.ternup.caddisfly.util.ColorUtils;
import com.ternup.caddisfly.util.FileUtils;

import junit.framework.TestCase;

import android.os.Bundle;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class ColorUtilsTest extends TestCase {

    private static final DecimalFormat doubleFormat = new DecimalFormat("0.0");

    final int SAMPLE_LENGTH = 200;

    private ArrayList<Integer> colorRange;

    private String pictureFileDir;

    public void setUp() {
        MainApp mainApp = new MainApp();
        colorRange = mainApp.colorList;
        pictureFileDir = FileUtils.getStoragePath(mainApp, -1, "", false);
    }

    public void testFor_0_0_ppm() throws Exception {
        double expectedValue = 0.0;
        String filename = pictureFileDir + "cad-" + doubleFormat.format(expectedValue) + ".jpg";
        assertTrue("File not found : " + filename, new File(filename).exists());
        Bundle bundle = ColorUtils.getPpmValue(filename, colorRange, -1, -1, SAMPLE_LENGTH);
        assertNotNull(bundle);
        assertEquals("ppm value is wrong", expectedValue, bundle.getDouble("ppm"));
    }

    public void testFor_0_5_ppm() throws Exception {
        double expectedValue = 0.5;
        String filename = pictureFileDir + "cad-" + doubleFormat.format(expectedValue) + ".jpg";
        assertTrue("File not found : " + filename, new File(filename).exists());
        Bundle bundle = ColorUtils.getPpmValue(filename, colorRange, -1, -1, SAMPLE_LENGTH);
        assertNotNull(bundle);
        assertEquals("ppm value is wrong", expectedValue, bundle.getDouble("ppm"));
    }

    public void testFor_1_0_ppm() throws Exception {
        double expectedValue = 1.0;
        String filename = pictureFileDir + "cad-" + doubleFormat.format(expectedValue) + ".jpg";
        assertTrue("File not found : " + filename, new File(filename).exists());
        Bundle bundle = ColorUtils.getPpmValue(filename, colorRange, -1, -1, SAMPLE_LENGTH);
        assertNotNull(bundle);
        assertEquals("ppm value is wrong", expectedValue, bundle.getDouble("ppm"));
    }

    public void testFor_1_5_ppm() throws Exception {
        double expectedValue = 1.5;
        String filename = pictureFileDir + "cad-" + doubleFormat.format(expectedValue) + ".jpg";
        assertTrue("File not found : " + filename, new File(filename).exists());
        Bundle bundle = ColorUtils.getPpmValue(filename, colorRange, -1, -1, SAMPLE_LENGTH);
        assertNotNull(bundle);
        assertEquals("ppm value is wrong", expectedValue, bundle.getDouble("ppm"));
    }

    public void testFor_2_0_ppm() throws Exception {
        double expectedValue = 2.0;
        String filename = pictureFileDir + "cad-" + doubleFormat.format(expectedValue) + ".jpg";
        assertTrue("File not found : " + filename, new File(filename).exists());
        Bundle bundle = ColorUtils.getPpmValue(filename, colorRange, -1, -1, SAMPLE_LENGTH);
        assertNotNull(bundle);
        assertEquals("ppm value is wrong", expectedValue, bundle.getDouble("ppm"));
    }

    public void testFor_2_5_ppm() throws Exception {
        double expectedValue = 2.5;
        String filename = pictureFileDir + "cad-" + doubleFormat.format(expectedValue) + ".jpg";
        assertTrue("File not found : " + filename, new File(filename).exists());
        Bundle bundle = ColorUtils.getPpmValue(filename, colorRange, -1, -1, SAMPLE_LENGTH);
        assertNotNull(bundle);
        assertEquals("ppm value is wrong", expectedValue, bundle.getDouble("ppm"));
    }

    public void testFor_3_0_ppm() throws Exception {
        double expectedValue = 3.0;
        String filename = pictureFileDir + "cad-" + doubleFormat.format(expectedValue) + ".jpg";
        assertTrue("File not found : " + filename, new File(filename).exists());
        Bundle bundle = ColorUtils.getPpmValue(filename, colorRange, -1, -1, SAMPLE_LENGTH);
        assertNotNull(bundle);
        assertEquals("ppm value is wrong", expectedValue, bundle.getDouble("ppm"));
    }

}
