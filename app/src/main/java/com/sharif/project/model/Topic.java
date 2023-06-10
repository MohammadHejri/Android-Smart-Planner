package com.sharif.project.model;

public class Topic {

    public Integer id;
    public String name;
    public Integer parentId;

    public Topic(Integer id, String name, Integer parentId) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
    }
}
