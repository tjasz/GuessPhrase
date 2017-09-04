package com.example.tjasz.guessphrase;

import android.content.Context;
import android.content.res.AssetManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

/**
 * This class contains and tracks data about a category of phrases:
 * its name, whether or not it is user-created, the file path associated with it,
 * and member phrases.
 * The ability to read, write, and delete the JSON files describing categories
 * is also handled by this class.
 */

class Category {
    private Context myContext;
    private File customCategoriesDir;

    private String name;
    private boolean isCustom;
    private String path;
    private ArrayList<String> items;
    private Random generator;

    Category(Context context) {
        generator = new Random();
        myContext = context;
        customCategoriesDir = new File(myContext.getExternalFilesDir(null), "category");
    }

    public void setName(String newName) {
        name = newName;
    }

    void addItems(Collection<String> newItems) {
        if (items == null) {
            items = new ArrayList<>();
        }
        items.addAll(newItems);
    }

    public String getName() {
        return name;
    }

    String getRandomItem() {
        return items.get(generator.nextInt(items.size()));
    }

    void setIsCustom(boolean newIsCustom) {
        isCustom = newIsCustom;
    }

    boolean getIsCustom() {
        return isCustom;
    }

    void setPath(String newPath) {
        path = newPath;
    }

    String getPath() {
        return path;
    }

    boolean isInSavedGame() {
        // return true if game save file exists and uses this category
        if (myContext.getFileStreamPath(myContext.getResources().getString(R.string.game_save_file_name)).exists()) {
            GameState gs = new GameState(myContext, null);
            try {
                gs.restoreGame();
            }
            catch (CategoryNotFoundException ex ) {
                // this is not a problem here
                // deal with this if resume game is attempted
            }
            if (isCustom == gs.getIsCustomCategory() && path.equals(gs.getCategoryPath())) {
                // category matches that in the save file
                return true;
            }
        }
        return false;
    }

    void readJSONFile() throws CategoryNotFoundException {
        // open file stream
        InputStream fis;
        if (isCustom) {
            try {
                fis = new FileInputStream(new File(customCategoriesDir, path));
            }
            catch (FileNotFoundException ex) {
                String message = myContext.getResources().getString(R.string.category_not_found_head);
                message += path;
                message += myContext.getResources().getString(R.string.category_not_found_tail);
                throw new CategoryNotFoundException(message);
            }
        }
        else {
            AssetManager am = myContext.getAssets();
            try {
                fis = am.open("category/" + path);
            }
            catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        // read category from save file
        String saveString = "";
        BufferedReader reader = null;
        try {
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
            JSONObject categoryJSON = new JSONObject(saveString);
            name = categoryJSON.getString("name");
            JSONArray itemsJSON  = categoryJSON.getJSONArray("items");
            items = new ArrayList<>();
            for (int i = 0; i < itemsJSON.length(); i++) {
                items.add(itemsJSON.getString(i));
            }
        }
        catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    void writeJSONFile() {
        // collect category attributes in JSON object
        JSONObject categoryJSON = new JSONObject();
        try {
            categoryJSON.put("name", name);
            JSONArray itemsJSON = new JSONArray(items);
            categoryJSON.put("items", itemsJSON);
        }
        catch (JSONException e) {
            throw new RuntimeException(e);
        }
        // write game state to file
        OutputStream fos;
        PrintWriter writer = null;
        try {
            fos = new FileOutputStream(new File(customCategoriesDir, path));
            writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                    fos)));
            writer.print(categoryJSON.toString());
        }
        catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }
        finally {
            if (null != writer) {
                writer.close();
            }
        }
    }

    void deleteFile() {
        if (isCustom) {
            File file = new File(customCategoriesDir, getPath());
            if (!(file.delete())) {
                throw new RuntimeException("Failed to delete file " + file.getPath());
            }
        }
    }
}
