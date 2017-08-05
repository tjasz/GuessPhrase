package com.example.tjasz.guessphrase;

import java.io.FileNotFoundException;

public class CategoryNotFoundException extends FileNotFoundException {
    public CategoryNotFoundException(String msg) {
        super(msg);
    }
}
