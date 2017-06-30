package com.example.tjasz.guessphrase;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.media.AudioManager;
import android.os.Vibrator;
import android.widget.Toast;

import java.io.File;


public class GameActivity extends ActionBarActivity implements GameHandler {

    boolean visible;
    TextView mainText, t1scoreText, t2scoreText, timerText;
    private static final int T1COLOR = Color.rgb(0,128,0);
    private static final int T2COLOR = Color.rgb(0,0,128);
    GameState gameState;
    Vibrator vibrator;
    AudioManager audioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        mainText = (TextView) findViewById(R.id.text_main);
        t1scoreText = (TextView) findViewById(R.id.t1score);
        t2scoreText = (TextView) findViewById(R.id.t2score);
        timerText = (TextView) findViewById(R.id.timer);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // disable game control buttons until items load
        mainText.setClickable(false);
        timerText.setClickable(false);
        mainText.setVisibility(View.INVISIBLE);

        new LoadAndStartGameTask(this, this).execute(getIntent());
    }

    private class LoadAndStartGameTask extends AsyncTask<Intent, Void, GameState> {
        Context myContext;
        GameHandler myGameHandler;

        LoadAndStartGameTask(Context context, GameHandler gameHandler) {
            myContext = context;
            myGameHandler = gameHandler;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected GameState doInBackground(Intent... intents) {
            GameState result = new GameState(myContext, myGameHandler);
            if (intents[0].getBooleanExtra("resumingGame", false)) {
                result.restoreGame();
            }
            else {
                int categoryResourceId = intents[0].getIntExtra("categoryResourceId", R.array.categoryOriginal);
                result.loadNewGame(categoryResourceId);
            }
            return result;
        }

        @Override
        protected void onPostExecute(GameState result) {
            // re-enable game control buttons now that items have loaded
            gameState = result;
            if (visible) {
                gameState.resumeTimer();
            }
        }
    }

    private void updateDisplay() {
        // enable game control buttons
        mainText.setClickable(true);
        timerText.setClickable(true);
        mainText.setVisibility(View.VISIBLE);
        // update scores, item text
        t1scoreText.setText(String.valueOf(gameState.getT1score()));
        t2scoreText.setText(String.valueOf(gameState.getT2score()));
        mainText.setText(gameState.getNextItem());

        if (gameState.hasT1won()) {
            mainText.setClickable(true);
            mainText.setTextColor(T1COLOR);
            timerText.setTextColor(T1COLOR);
            showGameOverDialog(true);
        }
        else if (gameState.hasT2won()) {
            mainText.setClickable(true);
            mainText.setTextColor(T2COLOR);
            timerText.setTextColor(T2COLOR);
            showGameOverDialog(false);
        }
        else {
            gameState.resumeTimer();
        }
    }

    public void onTimerStart() {
        // do quick initial vibrate
        if (!(audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT)) {
            vibrator.vibrate(100);
        }
        // update display
        updateDisplay();
        timerText.setTextColor(Color.BLACK);
        mainText.setTextColor(Color.BLACK);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        nextItem(mainText);
    }

    public void onTimerTick(long millisLeft) {
        // handle vibration pattern
        if (!(audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT)) {
            if (millisLeft > GameState.defaultTime/2 - 50) {
                if (millisLeft % 2000 < 50) {
                    vibrator.vibrate(100);
                }
            } else if (millisLeft > GameState.defaultTime/4 - 50) {
                if (millisLeft % 1000 < 50) {
                    vibrator.vibrate(100);
                }
            } else {
                if (millisLeft % 500 < 50) {
                    vibrator.vibrate(100);
                }
            }
        }
        // update timerText display
        if (millisLeft > GameState.defaultTime/2) {
            timerText.setText(Long.toString(millisLeft/1000));
        } else {
            timerText.setText(millisLeft/1000 + "." + millisLeft/100 % 10);
        }
    }

    public void onTimerFinish() {
        // do long vibrate
        if (!(audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT)) {
            vibrator.vibrate(2000);
        }
        // update display
        timerText.setText("0.0");
        timerText.setTextColor(Color.RED);
        mainText.setTextColor(Color.RED);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        showGetWinnerDialog();
    }

    /* v is unused, but must be an argument in order for this to be an onClick method */
    public void toggleTimerState(View v) {
        if (gameState.getIsTimerRunning()) {
            gameState.pauseTimer();
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            mainText.setVisibility(View.INVISIBLE);
        }
        else {
            gameState.resumeTimer();
        }
    }

    private class SaveGameTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            File game_save_file = getFileStreamPath(getResources().getString(R.string.game_save_file_name));
            game_save_file.delete();
        }

        @Override
        protected Void doInBackground(Void... v) {
            gameState.saveGameToFile();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // TODO tell MenuActivity to enable "Resume Game" button once saved
        }
    }

    @Override
    public void onPause() {
        visible = false;
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mainText.setVisibility(View.INVISIBLE);
        if (gameState != null) {
            gameState.pauseTimer();
            new SaveGameTask().execute();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        visible = true;
    }

    public void nextItem(View v) {
        if (!gameState.getIsTimerRunning()) {
            gameState.resumeTimer();
        }
        else {
            mainText.setText(gameState.getNextItem());
        }
    }

    private void showGetWinnerDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(GameActivity.this).create();
        alertDialog.setCancelable(false);
        alertDialog.setMessage(getResources().getString(R.string.getwinner_prompt));
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getResources().getString(R.string.T1name),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        gameState.incrementT1score();
                        updateDisplay();
                        dialog.dismiss();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.T2name),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        gameState.incrementT2score();
                        updateDisplay();
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private void showGameOverDialog(boolean didT1win) {
        AlertDialog alertDialog = new AlertDialog.Builder(GameActivity.this).create();
        alertDialog.setCancelable(false);
        alertDialog.setTitle(R.string.game_over);
        if (didT1win) {
            alertDialog.setMessage(getResources().getString(R.string.congratsT1));
        }
        else {
            alertDialog.setMessage(getResources().getString(R.string.congratsT2));
        }
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getResources().getString(R.string.play_again),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        gameState.resetGame();
                        updateDisplay();
                        nextItem(mainText);
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

}
