package com.example.tjasz.guessphrase;

import android.content.Context;
import android.os.CountDownTimer;

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

/**
 * This class tracks the state of a game,
 * including score, timer state, and the category.
 * Methods allow for incrementing either score,
 * pausing and resuming the timer,
 * and resetting the game.
 * Methods to load a previously saved state,
 * create a new game, or save a game state to a file also exist.
 */

class GameState {
    static final int defaultTime = 60000;
    private static final int winningScore = 7;

    private Boolean isTimerRunning;
    private int t1score, t2score;
    private long millisLeft;

    private Category category;
    private CountDownTimer timer;
    private Context myContext;
    private GameHandler myGameHandler;

    private boolean isCustomCategory;
    private String categoryPath;

    GameState(Context context, GameHandler gameHandler) {
        myContext = context;
        myGameHandler = gameHandler;
        isTimerRunning = false;
    }

    boolean getIsTimerRunning() {
        return isTimerRunning;
    }

    int getT1score() {
        return t1score;
    }

    int getT2score() {
        return t2score;
    }

    boolean hasT1won() {
        return (t1score >= winningScore);
    }

    boolean hasT2won() {
        return (t2score >= winningScore);
    }

    // get a random item from the items list
    String getNextItem() {
        return category.getRandomItem();
    }

    String getCategoryName() {
        return category.getName();
    }

    String getCategoryPath() {
        return category.getPath();
    }

    boolean getIsCustomCategory() {
        return isCustomCategory;
    }

    // restore a game from the save file
    void restoreGame() throws CategoryNotFoundException {
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
            isCustomCategory = gameState.getBoolean("isCustomCategory");
            categoryPath = gameState.getString("categoryPath");
        }
        catch (JSONException e) {
            throw new RuntimeException(e);
        }
        // load category from the asset file
        category = new Category(myContext);
        category.setIsCustom(isCustomCategory);
        category.setPath(categoryPath);
        category.readJSONFile();
    }

    // load a new game with categoryBools as given
    void loadNewGame(boolean newIsCustom, String newCategoryPath) {
        resetGame();
        isCustomCategory = newIsCustom;
        categoryPath = newCategoryPath;
        // load category from the asset file
        category = new Category(myContext);
        category.setIsCustom(isCustomCategory);
        category.setPath(categoryPath);
        try {
            category.readJSONFile();
        }
        catch (CategoryNotFoundException ex) {
            // this shouldn't happen with new games
            throw new RuntimeException(ex);
        }
    }

    // save the game state to the save file
    void saveGameToFile() {
        // collect game state in JSON object
        JSONObject gameState = new JSONObject();
        try {
            gameState.put("t1score", t1score);
            gameState.put("t2score", t2score);
            gameState.put("millisLeft", millisLeft);
            gameState.put("isCustomCategory", isCustomCategory);
            gameState.put("categoryPath", categoryPath);
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
    void resetGame() {
        t1score = 0;
        t2score = 0;
        millisLeft = defaultTime;
    }

    int incrementT1score() {
        t1score++;
        return t1score;
    }

    int incrementT2score() {
        t2score++;
        return t1score;
    }

    // resume the timer based on millisLeft
    void resumeTimer() {
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
    void pauseTimer() {
        if (isTimerRunning) {
            timer.cancel();
            isTimerRunning = Boolean.FALSE;
        }
    }

}
