package com.sharif.project.model;

import ir.mirrajabi.searchdialog.core.Searchable;

public class TopicSearchModel implements Searchable {

    public int mTopicId;
    private String mTitle;

    public TopicSearchModel(int topicId, String title) {
        mTopicId = topicId;
        mTitle = title;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    public TopicSearchModel setTitle(String title) {
        mTitle = title;
        return this;
    }
}