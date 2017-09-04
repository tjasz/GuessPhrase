package com.example.tjasz.guessphrase;

/**
 *  This interface defines methods that must any activity
 *  meant to update the user about the game state must implement.
 *  These are intended to run on the UI thread and handle the
 *  start, tick, and completion of a game timer.
 */

interface GameHandler {
    void onTimerStart();
    void onTimerTick(long millisLeft);
    void onTimerFinish();
}