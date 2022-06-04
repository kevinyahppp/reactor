package com.ideas.reactor.app.models;

import lombok.AllArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@ToString
public class UserWithComments {
    private User user;
    private  Comments comments;
}
