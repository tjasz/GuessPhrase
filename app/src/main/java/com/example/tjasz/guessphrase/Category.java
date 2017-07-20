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

public class Category {
    Context myContext;
    File customCategoriesDir;

    private String name;
    boolean isCustom;
    String path;
    private ArrayList<String> items;
    Random generator;

    Category(Context context) {
        generator = new Random();
        myContext = context;
        customCategoriesDir = new File(myContext.getExternalFilesDir(null), "category");
    }

    public void setName(String newName) {
        name = newName;
    }

    public void addItems(Collection<String> newItems) {
        if (items == null) {
            items = new ArrayList<>();
        }
        items.addAll(newItems);
    }

    public String getName() {
        return name;
    }

    public String getRandomItem() {
        return items.get(generator.nextInt(items.size()));
    }

    public void setIsCustom(boolean newIsCustom) {
        isCustom = newIsCustom;
    }

    public boolean getIsCustom() {
        return isCustom;
    }

    public void setPath(String newPath) {
        path = newPath;
    }

    public String getPath() {
        return path;
    }

    public void readJSONFile() {
        // open file stream
        InputStream fis;
        if (isCustom) {
            try {
                fis = new FileInputStream(new File(customCategoriesDir, path));
            }
            catch (FileNotFoundException ex) {
                throw new RuntimeException(ex);
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

    public void writeJSONFile() {
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
            fos = new FileOutputStream(new File(customCategoriesDir, name));
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

    public void deleteFile() {
        if (isCustom) {
            File file = new File(customCategoriesDir, getPath());
            file.delete();
        }
    }
}
