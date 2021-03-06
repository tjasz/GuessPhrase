package com.example.tjasz.guessphrase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Iterator;

/**
 * This class wraps the WikiMedia API for use in collecting data from Wikipedia.
 * Package-private methods include one for getting search results
 * and one for getting the page links on a Wikipedia page.
 * Private methods handle the actual querying process.
 */

class Wikipedia {
    private static final String BASE_URI = "https://en.wikipedia.org/w/api.php?format=json";

    static HashSet<String> getLinks(String pageName) {
        boolean hasMore = true;
        String nextPage = null;
        HashSet<String> linksList = new HashSet<>();
        try {
            while (hasMore) {
                // form query string
                String qString = "&action=query&prop=links&pllimit=500&plnamespace=0";
                qString += "&titles=" + URLEncoder.encode(pageName, "UTF-8");
                if (nextPage != null && nextPage.length() > 0) {
                    qString += "&plcontinue=" + nextPage;
                }
                JSONObject qResult = new JSONObject(query(qString));
                // get list of links from this query
                JSONObject pages = qResult.getJSONObject("query").getJSONObject("pages");
                Iterator<String> keys = pages.keys();
                while (keys.hasNext()) {
                    String keyValue = keys.next();
                    JSONObject page = pages.getJSONObject(keyValue);
                    JSONArray links = page.getJSONArray("links");
                    for (int i = 0; i < links.length(); i++) {
                        JSONObject link = links.getJSONObject(i);
                        String linkTitle = link.getString("title");
                        if (linkTitle.split("\\s").length <= 3) {
                            linksList.add(link.getString("title"));
                        }
                    }
                }
                // continue query if necessary
                if (qResult.has("continue")) {
                    nextPage = qResult.getJSONObject("continue").getString("plcontinue");
                } else {
                    hasMore = false;
                }
            }
        }
        catch (UnsupportedEncodingException|JSONException e) {
            throw new RuntimeException(e);
        }
        return linksList;
    }

    static HashSet<String> search(String pageName, int numResults) {
        HashSet<String> resultTitles = new HashSet<>();
        try {
            // form query string
            String qString = "&action=query&list=search";
            qString += "&srlimit=" + Integer.toString(numResults);
            qString += "&srsearch=" + URLEncoder.encode(pageName, "UTF-8");
            // get raw result from query
            JSONObject qResult = new JSONObject(query(qString));
            // get list of page titles from this search query
            JSONArray pages = qResult.getJSONObject("query").getJSONArray("search");
            for (int i = 0; i < pages.length(); i++) {
                resultTitles.add(pages.getJSONObject(i).getString("title"));
            }
        }
        catch (UnsupportedEncodingException|JSONException e) {
            throw new RuntimeException(e);
        }
        return resultTitles;
    }

    private static String query(String URI) {
        String data = "";
        HttpURLConnection huc = null;
        try {
            huc = (HttpURLConnection) new URL(BASE_URI + URI).openConnection();
            InputStream in = new BufferedInputStream(huc.getInputStream());
            data = readStream(in);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        finally {
            if (huc != null) {
                huc.disconnect();
            }
        }
        return data;
    }

    private static String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuilder data = new StringBuilder("");
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                data.append(line);
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return data.toString();
    }
}
