package com.example.tjasz.guessphrase;

import java.io.FileNotFoundException;

class CategoryNotFoundException extends FileNotFoundException {
    CategoryNotFoundException(String msg) {
        super(msg);
    }
}
