package com.example.tjasz.guessphrase;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import java.util.ArrayList;


public class AddCategoryActivity extends ActionBarActivity {
    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_category);
        tv = (TextView) findViewById(R.id.add_category_textview);
        new QueryWikipediaTask().execute("United%20States");
    }

    private class QueryWikipediaTask extends AsyncTask<String, Void, ArrayList<String>> {
        @Override
        protected void onPreExecute() {

        }

        @Override
        protected ArrayList<String> doInBackground(String... params) {
            return Wikipedia.getLinks(params[0]);
        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {
            tv.setText(TextUtils.join("\n", result));
        }
    }
}
