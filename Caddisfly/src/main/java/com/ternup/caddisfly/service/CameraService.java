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

package com.ternup.caddisfly.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class CameraService extends Service {

    public static final String NOTIFICATION = "com.ternup.caddisfly"; //NON-NLS

    private final IBinder mBinder = new MyBinder();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        startApp();

        return Service.START_NOT_STICKY;
    }

    private void startApp() {
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra("alarm", true);
        sendBroadcast(intent);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    private class MyBinder extends Binder {

    }
}
