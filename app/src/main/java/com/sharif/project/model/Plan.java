package com.sharif.project.model;

public class Plan {

    public Integer id;
    public String name;
    public Long startDateTimeStamp;
    public Long endDateTimeStamp;

    public Plan(Integer id, String name, Long startDateTimeStamp, Long endDateTimeStamp) {
        this.id = id;
        this.name = name;
        this.startDateTimeStamp = startDateTimeStamp;
        this.endDateTimeStamp = endDateTimeStamp;
    }
}
