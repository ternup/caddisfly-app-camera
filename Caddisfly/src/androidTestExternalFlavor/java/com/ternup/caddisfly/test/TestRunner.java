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

import android.test.InstrumentationTestRunner;
import android.test.InstrumentationTestSuite;

import junit.framework.TestSuite;

@SuppressWarnings("WeakerAccess")
public class TestRunner extends InstrumentationTestRunner {

    @Override
    public TestSuite getAllTests() {
        InstrumentationTestSuite suite = new InstrumentationTestSuite(this);
        //suite.addTestSuite(MainActivityTest.class);
        suite.addTestSuite(RobotiumTest.class);
        //suite.addTestSuite(RobotiumTestSingle.class);
        //suite.addTestSuite(ColorAnalyzerTest.class);
        return suite;
    }

    @Override
    public ClassLoader getLoader() {
        return TestRunner.class.getClassLoader();
    }

}