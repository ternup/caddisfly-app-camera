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

package org.akvo.mobile.caddisfly.fragment;

import com.ternup.caddisfly.adapter.GalleryListAdapter;
import com.ternup.caddisfly.fragment.CalibrateItemFragmentBase;

import java.util.ArrayList;

@SuppressWarnings("WeakerAccess")
public class CalibrateItemFragment extends CalibrateItemFragmentBase {

    @Override
    protected void updateListView(int position) {

        // override and show only headers by not providing data to list adapter

        ArrayList<String> files = new ArrayList<String>();
        mAdapter = new GalleryListAdapter(getActivity(), mTestType, position, files, false);
        setListAdapter(mAdapter);

    }

}
