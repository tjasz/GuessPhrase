package com.example.tjasz.guessphrase;

import android.content.Context;
import android.os.CountDownTimer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * Track the state of a game
 * and define actions related to changing
 * and accessing the state.
 */
public class GameState {
    public static final int defaultTime = 60000;
    private static final int winningScore = 7;

    Boolean isTimerRunning;
    int t1score, t2score;
    long millisLeft;

    ArrayList<String> items;
    Random generator;
    CountDownTimer timer;
    Context myContext;
    GameHandler myGameHandler;

    private static final int[] categoryIDs = {
            R.array.categoryOriginal,
            R.array.categoryAnimals,
            R.array.categoryFood,
            R.array.categoryUnitedStates,
            R.array.categoryGeography,
            R.array.categorySports,
            R.array.categoryEntertainment,
            R.array.categoryDirty
    };
    boolean[] categoryBools;

    public GameState(Context context, GameHandler gameHandler) {
        myContext = context;
        myGameHandler = gameHandler;
        generator = new Random();
        isTimerRunning = false;
    }

    public boolean getIsTimerRunning() {
        return isTimerRunning;
    }

    public int getT1score() {
        return t1score;
    }

    public int getT2score() {
        return t2score;
    }

    public boolean hasT1won() {
        return (t1score >= winningScore);
    }

    public boolean hasT2won() {
        return (t2score >= winningScore);
    }

    // get a random item from the items list
    public String getNextItem() {
        return items.get(generator.nextInt(items.size()));
    }

    // populate the items ArrayList based on the values of categoryBools
    private void populateItems() {
        items = new ArrayList<>();
        for (int i = 0; i < categoryBools.length; i++) {
            if (categoryBools[i]) {
                String[] candidates = myContext.getResources().getStringArray(categoryIDs[i]);
                items.addAll(Arrays.asList(candidates));
            }
        }
    }

    // restore a game from the save file
    public void restoreGame() {
        // read game state from save file
        String saveString = "";
        BufferedReader reader = null;
        try {
            String filename = myContext.getResources().getString(R.string.game_save_file_name);
            FileInputStream fis = myContext.openFileInput(filename);
            reader = new BufferedReader(new InputStreamReader(fis));

            String line;
            while ((line = reader.readLine()) != null) {
                saveString += line;
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != reader) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        // translate string to JSON object
        try {
            JSONObject gameState = new JSONObject(saveString);
            t1score = gameState.getInt("t1score");
            t2score = gameState.getInt("t2score");
            millisLeft = gameState.getLong("millisLeft");
            JSONArray json_category_bools = gameState.getJSONArray("categoryBools");
            categoryBools = new boolean[json_category_bools.length()];
            for (int i = 0; i < json_category_bools.length(); i++) {
                categoryBools[i] = json_category_bools.getBoolean(i);
            }
            populateItems();
        }
        catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    // load a new game with categoryBools as given
    public void loadNewGame(boolean[] newCategoryBools) {
        t1score = 0;
        t2score = 0;
        millisLeft = defaultTime;
        categoryBools = newCategoryBools;
        populateItems();
    }

    // save the game state to the save file
    public void saveGameToFile() {
        // collect game state in JSON object
        JSONObject gameState = new JSONObject();
        try {
            gameState.put("t1score", t1score);
            gameState.put("t2score", t2score);
            gameState.put("millisLeft", millisLeft);
            JSONArray json_category_bools = new JSONArray();
            for (boolean categoryBool : categoryBools) {
                json_category_bools.put(categoryBool);
            }
            gameState.put("categoryBools", json_category_bools);
        }
        catch (JSONException e) {
            throw new RuntimeException(e);
        }
        // write game state to file
        PrintWriter writer = null;
        try {
            FileOutputStream fos = myContext.openFileOutput(
                    myContext.getResources().getString(R.string.game_save_file_name),
                    Context.MODE_PRIVATE);
            writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                    fos)));
            writer.print(gameState.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (null != writer) {
                writer.close();
            }
        }
    }

    // reset the scores and timer to begin a new game with the same items
    public void resetGame() {
        t1score = 0;
        t2score = 0;
        millisLeft = defaultTime;
    }

    public int incrementT1score() {
        t1score++;
        return t1score;
    }

    public int incrementT2score() {
        t2score++;
        return t1score;
    }

    // resume the timer based on millisLeft
    public void resumeTimer() {
        if (!isTimerRunning) {
            timer = new CountDownTimer(millisLeft, 50) {
                public void onTick(long millisUntilFinished) {
                    millisLeft = millisUntilFinished;
                    myGameHandler.onTimerTick(millisLeft);
                }

                public void onFinish() {
                    isTimerRunning = Boolean.FALSE;
                    myGameHandler.onTimerFinish();
                    millisLeft = defaultTime;
                }
            };
            timer.start();
            isTimerRunning = Boolean.TRUE;
            myGameHandler.onTimerStart();
        }
    }

    // pause the timer
    public void pauseTimer() {
        if (isTimerRunning) {
            timer.cancel();
            isTimerRunning = Boolean.FALSE;
        }
    }

}
