package com.example.tjasz.guessphrase;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.io.File;

/**
 * This is the main menu activity.
 * User options available are: new game, resume game, how to play, and change preferences.
 * The "How to play" button will show a dialog, but the other three options start another activity.
 */

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

    public void showInstructions(View v) {
        AlertDialog alertDialog = new AlertDialog.Builder(MenuActivity.this).create();
        alertDialog.setCancelable(true);
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.setTitle(getResources().getString(R.string.how_to_play_button));
        alertDialog.setMessage(getResources().getString(R.string.how_to_play));
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.okay),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
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

    // this one wraps the other for use as an onClick method
    public void openSettingsActivity(View v) {
        openSettingsActivity();
    }

    private void openSettingsActivity() {
        Intent intent = new Intent(MenuActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_game, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_edit_settings) {
            openSettingsActivity();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
