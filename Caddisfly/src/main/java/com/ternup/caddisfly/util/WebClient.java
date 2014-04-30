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

/*
    This file is part of Caddisfly

    Caddisfly is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Caddisfly is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Caddisfly.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.ternup.caddisfly.util;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.entity.StringEntity;

import android.content.Context;
import android.util.Base64;

public class WebClient {

    final static int DEFAULT_TIMEOUT = 60 * 6000;

    private static final String BASE_URL = "http://labyrinth-punter.rhcloud.com/testlog/api/";

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
        String credentials = "admin:<password>";
        String base64EncodedCredentials = Base64
                .encodeToString(credentials.getBytes(), Base64.NO_WRAP);

        client.addHeader("Authorization", "Basic " + base64EncodedCredentials);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl + "/";
    }
}