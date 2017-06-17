package com.example.tjasz.guessphrase;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.io.File;


public class MenuActivity extends ActionBarActivity {

    Button resumeGameButton;

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


}
