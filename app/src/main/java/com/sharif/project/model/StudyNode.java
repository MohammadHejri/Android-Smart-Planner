package com.sharif.project.model;

import java.util.ArrayList;
import java.util.Date;

public class StudyNode {

    private static ArrayList<StudyNode> all = new ArrayList<>();

    public int id;
    public int planId;
    public int topicId;
    public int boxId;
    public String name;
    public Date start;
    public Date end;

    public StudyNode(int planId, int topicId, int boxId, String name, Date start, Date end) {
        this.id = all.size();
        this.planId = planId;
        this.topicId = topicId;
        this.boxId = boxId;
        this.name = name;
        this.start = start;
        this.end = end;
        all.add(this);
    }

    public boolean isTopic() {
        return topicId != -1 && boxId == -1;
    }

    public boolean isBox() {
        return topicId == -1 && boxId != -1;
    }
}
