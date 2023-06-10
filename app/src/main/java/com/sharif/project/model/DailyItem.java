package com.sharif.project.model;

import java.util.Date;

public class DailyItem {

    public DailyItemType type;
    public Date startTime;
    public Date endTime;
    public int id;
    public boolean availablilty;

    public DailyItem(DailyItemType type, Date startTime, Date endTime, int id) {
        this.type = type;
        this.startTime = startTime;
        this.endTime = endTime;
        this.id = id;
        this.availablilty = true;
    }
}
