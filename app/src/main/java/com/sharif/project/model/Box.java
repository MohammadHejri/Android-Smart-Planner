package com.sharif.project.model;

public class Box {

    public Integer id;
    public Integer planId;
    public Integer topicId;
    public Integer duration;
    public Integer timeSpent;
    public Long startTime;
    public String note;
    public boolean finished;

    public Box(Integer id, Integer planId, Integer topicId, Integer duration, Integer timeSpent, Long startTime, String note, boolean finished) {
        this.id = id;
        this.planId = planId;
        this.topicId = topicId;
        this.duration = duration;
        this.timeSpent = timeSpent;
        this.startTime = startTime;
        this.note = note;
        this.finished = finished;
    }

    @Override
    public String toString() {
        return "Box{" +
                "id=" + id +
                ", planId=" + planId +
                ", topicId=" + topicId +
                ", duration=" + duration +
                '}';
    }

    public Box(Box other) {
        this.id = other.id;
        this.planId = other.planId;
        this.topicId = other.topicId;
        this.duration = other.duration;
        this.timeSpent = other.timeSpent;
        this.startTime = other.startTime;
        this.note = other.note;
        this.finished = other.finished;
    }
}
