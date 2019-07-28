package com.example.simple_assistant;

import lombok.Value;

@Value
public class Doc2VecResult {
    private String title;
    private double accuracy;

    public Doc2VecResult() {
        this("", -1);
    }

    private Doc2VecResult(String title, Integer accuracy){
        this.title = title;
        this.accuracy = accuracy;
    }

}
