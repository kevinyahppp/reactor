package com.ideas.reactor.app.models;

import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@ToString
public class Comments {
    private List<String> comments;

    public Comments() {
        this.comments = new ArrayList<>();
    }

    public void addComment(String comment) {
        this.comments.add(comment);
    }
}
