package com.example.tjasz.guessphrase;

interface GameHandler {
    public void onTimerStart();
    public void onTimerTick(long millisLeft);
    public void onTimerFinish();
}