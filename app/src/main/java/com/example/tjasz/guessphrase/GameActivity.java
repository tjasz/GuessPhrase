package com.example.tjasz.guessphrase;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.media.AudioManager;
import android.os.Vibrator;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

/**
 * This activity is where the main game play occurs.
 * It allows users to interact with a game state,
 * and updates them on the current game state with text and vibrations.
 * Games are loaded if necessary and resumed when the activity resumes.
 * Games are saved to the game save file when the activity is paused.
 * Dialogs ask for the winner of a round, or inform the users when a game is over.
 */

public class GameActivity extends ActionBarActivity implements GameHandler {

    boolean visible;
    RelativeLayout loadingWheel;
    TextView categoryNameText, mainText, t1scoreText, t2scoreText, timerText;
    private static final int T1COLOR = Color.rgb(0,128,0);
    private static final int T2COLOR = Color.rgb(0,0,128);
    GameState gameState;
    Vibrator vibrator;
    AudioManager audioManager;
    boolean shouldVibrate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        loadingWheel = (RelativeLayout) findViewById(R.id.loadingPanel);
        categoryNameText = (TextView) findViewById(R.id.category_name);
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
        loadingWheel.setVisibility(View.VISIBLE);
    }

    private class LoadAndStartGameTask extends AsyncTask<Intent, Void, GameState> {
        Context myContext;
        GameHandler myGameHandler;
        ArrayList<Exception> exceptionList = new ArrayList<>();

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
                try {
                    result.restoreGame();
                }
                catch (CategoryNotFoundException ex) {
                    exceptionList.add(ex);
                }
            }
            else {
                String assetFilename = intents[0].getStringExtra("path");
                boolean isCustom = intents[0].getBooleanExtra("isCustomCategory", false);
                result.loadNewGame(isCustom, assetFilename);
            }
            return result;
        }

        @Override
        protected void onPostExecute(GameState result) {
            // check for errors
            if (exceptionList.size() > 0) {
                // show exception message in a dialog
                AlertDialog alertDialog = new AlertDialog.Builder(myContext).create();
                alertDialog.setCancelable(false);
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.setTitle(R.string.game_load_error);
                String message = "";
                for (int i = 0; i < exceptionList.size(); i++) {
                    message += exceptionList.get(i).getMessage();
                    if (i < exceptionList.size() - 1) {
                        message += "\n\n";
                    }
                }
                alertDialog.setMessage(message);
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, myContext.getResources().getString(R.string.okay),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                // finish the activity; we couldn't load the game
                                finish();
                            }
                        });
                alertDialog.show();
            }
            else {
                // re-enable game control buttons now that items have loaded
                gameState = result;
                if (visible) {
                    gameState.resumeTimer();
                    loadingWheel.setVisibility(View.GONE);
                }
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
        if (!(audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT) && shouldVibrate) {
            vibrator.vibrate(100);
        }
        // update display
        updateDisplay();
        categoryNameText.setText(gameState.getCategoryName());
        timerText.setTextColor(Color.BLACK);
        mainText.setTextColor(Color.BLACK);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        nextItem(mainText);
    }

    public void onTimerTick(long millisLeft) {
        // handle vibration pattern
        if (!(audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT) && shouldVibrate) {
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
            timerText.setText(String.format(Locale.US, "%d", millisLeft/1000));
        } else {
            timerText.setText(String.format(Locale.US, "%d.%d", millisLeft/1000, millisLeft/100 % 10));
        }
    }

    public void onTimerFinish() {
        // do long vibrate
        if (!(audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT) && shouldVibrate) {
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

    private class SaveGameTask extends AsyncTask<Void, Void, Void> {
        Context myContext;

        SaveGameTask(Context context) {
            myContext = context;
        }

        @Override
        protected Void doInBackground(Void... v) {
            gameState.saveGameToFile();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Toast.makeText(myContext, R.string.gameSaved, Toast.LENGTH_SHORT).show();
            // tell MenuActivity to enable "Resume Game" button once save is complete
            Intent intent = new Intent();
            intent.setAction(MenuActivity.GAME_SAVE_COMPLETED_ACTION);
            sendBroadcast(intent);
        }
    }

    @Override
    public void onPause() {
        visible = false;
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mainText.setText("");
        if (gameState != null) {
            gameState.pauseTimer();
            new SaveGameTask(this).execute();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        visible = true;
        SharedPreferences preferences = getSharedPreferences(SettingsActivity.GAME_PREFERENCES, MODE_PRIVATE);
        shouldVibrate = preferences.getBoolean(SettingsActivity.VIBRATION_PREFERENCE_KEY, true);
        if (gameState == null) {
            new LoadAndStartGameTask(this, this).executeOnExecutor(
                        AsyncTask.THREAD_POOL_EXECUTOR, getIntent());
        }
        else {
            gameState.resumeTimer();
        }
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
            Intent intent = new Intent(GameActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
