package com.example.tjasz.guessphrase;

interface GameHandler {
    void onTimerStart();
    void onTimerTick(long millisLeft);
    void onTimerFinish();
}