package com.sharif.project.model;

import java.util.Date;

public class Limitation {

    public Integer id;
    public String name;
    public Long offset;
    public Long period;
    public Long length;

    public Limitation(Integer id, String name, Long offset, Long period, Long length) {
        this.id = id;
        this.name = name;
        this.offset = offset;
        this.period = period;
        this.length = length;
    }

    public Date getStartTimeFromDate(Date date) {
        if (period == -1L)
            return new Date(offset);
        long time = offset + period * (long) Math.ceil((double) (date.getTime() - offset) / period);
        return new Date(time);
    }
}
