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

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.ternup.caddisfly.app.Globals;

import org.apache.http.entity.StringEntity;

import android.content.Context;
import android.util.Base64;

public class WebClient {

    final static int DEFAULT_TIMEOUT = 60 * 2000;

    private static final AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String url, RequestParams params,
            AsyncHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void put(String url, RequestParams params,
            AsyncHttpResponseHandler responseHandler) {
        addCredentials(client);
        client.put(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post(String url, RequestParams params,
            AsyncHttpResponseHandler responseHandler) {
        addCredentials(client);
        client.setTimeout(DEFAULT_TIMEOUT);
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void postJson(Context context, String url, StringEntity entity,
            AsyncHttpResponseHandler responseHandler) {
        addCredentials(client);
        client.post(context, getAbsoluteUrl(url), entity, "application/json", responseHandler);
    }

    private static void addCredentials(AsyncHttpClient client) {
        String credentials = Globals.CONNECT;
        String base64EncodedCredentials = Base64
                .encodeToString(credentials.getBytes(), Base64.NO_WRAP);

        client.addHeader("Authorization", "Basic " + base64EncodedCredentials);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return Globals.SERVER_BASE_URL + relativeUrl + "/";
    }
}