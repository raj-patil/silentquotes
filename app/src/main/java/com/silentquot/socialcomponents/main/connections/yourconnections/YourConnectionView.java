/*
 * Copyright (c) 2020. Created By Raj Patil
 */

package com.silentquot.socialcomponents.main.connections.yourconnections;

import com.silentquot.socialcomponents.main.base.BaseFragmentView;
import com.silentquot.socialcomponents.model.Connection;

import java.util.List;

public interface YourConnectionView extends BaseFragmentView {
    void onSearchResultsReady(List<Connection> profiles);

    void showLocalProgress();

    void hideLocalProgress();

    void showEmptyListLayout();

    void updateSelectedItem();
}
