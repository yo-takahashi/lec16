package com.example.simple_assistant;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class Doc2VecAPIResult {

    private List<Doc2VecResult> result;

    public Doc2VecAPIResult() {
        this(new ArrayList<>());
    }

}