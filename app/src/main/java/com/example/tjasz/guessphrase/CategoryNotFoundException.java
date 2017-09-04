package com.example.tjasz.guessphrase;

import java.io.FileNotFoundException;

/**
 * This exception is used when a category file that is to be loaded
 * is not found in the file system.
 */

class CategoryNotFoundException extends FileNotFoundException {
    CategoryNotFoundException(String msg) {
        super(msg);
    }
}
