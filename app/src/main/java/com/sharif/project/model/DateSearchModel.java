package com.sharif.project.model;

import java.util.Date;

import ir.mirrajabi.searchdialog.core.Searchable;

public class DateSearchModel implements Searchable {

    public Date mDate;
    private String mTitle;

    public DateSearchModel(Date mDate, String mTitle) {
        this.mDate = mDate;
        this.mTitle = mTitle;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    public DateSearchModel setTitle(String title) {
        mTitle = title;
        return this;
    }
}