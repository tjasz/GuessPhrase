package com.example.tjasz.guessphrase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

public class Category {
    private String name;
    private ArrayList<String> items;
    Random generator;

    Category() {
        generator = new Random();
    }

    public String getName() {
        return name;
    }

    public String getRandomItem() {
        return items.get(generator.nextInt(items.size()));
    }

    public void readJSONFile(InputStream fis) {
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

    public void writeJSONFile(OutputStream fos) {
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
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                    fos)));
            writer.print(categoryJSON.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (null != writer) {
                writer.close();
            }
        }
    }
}
