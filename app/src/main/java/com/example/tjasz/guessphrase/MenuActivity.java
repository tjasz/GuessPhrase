package com.example.tjasz.guessphrase;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.io.File;


public class MenuActivity extends ActionBarActivity {

    public static final String GAME_SAVE_COMPLETED_ACTION = "com.example.tjasz.guessphrase.GAME_SAVE_COMPLETED";

    Button resumeGameButton;
    BroadcastReceiver gameSavedReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        resumeGameButton = (Button) findViewById(R.id.resumeGameButton);
        File game_save_file = getFileStreamPath(getResources().getString(R.string.game_save_file_name));
        if (game_save_file.exists()) {
            resumeGameButton.setEnabled(true);
        }
        else {
            resumeGameButton.setEnabled(false);
        }

        // set up a BroadcastReceiver to enable the resumeGameButton when a game is saved
        gameSavedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                resumeGameButton.setEnabled(true);
            }
        };
    }

    public void newGame(View v) {
        Intent intent = new Intent(MenuActivity.this, SelectCategoryActivity.class);
        startActivity(intent);
    }

    public void resumeGame(View v){
        Intent intent = new Intent(MenuActivity.this, GameActivity.class);
        intent.putExtra("resumingGame", true);
        startActivity(intent);
    }

    @Override
    public void onPause() {
        // Unregister the BroadcastReceiver if it has been registered
        // Note: check that gameSavedReceiver is not null before attempting to
        // unregister in order to work around an Instrumentation issue
        if (gameSavedReceiver != null) {
            unregisterReceiver(gameSavedReceiver);
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Register the BroadcastReceiver
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(GAME_SAVE_COMPLETED_ACTION);
        registerReceiver(gameSavedReceiver, intentFilter);
    }

}
