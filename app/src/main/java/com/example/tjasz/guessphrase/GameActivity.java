package com.example.tjasz.guessphrase;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.media.AudioManager;
import android.os.Vibrator;


public class GameActivity extends ActionBarActivity implements GameHandler {

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

        gameState = new GameState(this, this);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        Intent intent = getIntent();
        if (intent.getBooleanExtra("resumingGame", false)) {
            gameState.restoreGame();
        }
        else {
            boolean[] categoryBools = intent.getBooleanArrayExtra("categoryBools");
            gameState.loadNewGame(categoryBools);
        }
        updateDisplay();
        gameState.resumeTimer();
    }

    private void updateDisplay() {
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
            mainText.setText("");
        }
        else {
            gameState.resumeTimer();
        }
    }

    @Override
    public void onPause() {
        gameState.pauseTimer();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mainText.setText("");
        gameState.saveGameToFile();
        super.onPause();
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
