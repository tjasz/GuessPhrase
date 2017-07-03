package com.example.tjasz.guessphrase;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.widget.LinearLayout;

import java.io.IOException;
import java.util.ArrayList;


public class SelectCategoryActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_category);

        new LoadCategoriesTask().execute(this);
    }

    private class LoadCategoriesTask extends AsyncTask<Activity, Void, ArrayList<CategoryButton>> {
        Activity myActivity;

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected ArrayList<CategoryButton> doInBackground(Activity... params) {
            // create a button for each category in the assets folder
            myActivity = params[0];
            ArrayList<CategoryButton> result = new ArrayList<>();
            AssetManager am = myActivity.getAssets();
            try {
                String[] assets = am.list("category");
                for (int i = 0; i < assets.length; i++) {
                    Category cat = new Category();
                    cat.readJSONFile(am.open("category/" + assets[i]));
                    CategoryButton newButton = new CategoryButton(myActivity, "category/" + assets[i]);
                    newButton.setText(cat.getName());
                    result.add(newButton);
                }
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
            return result;
        }

        @Override
        protected void onPostExecute(ArrayList<CategoryButton> result) {
            // add the category buttons to the LinearLayout in SelectCategoryActivity
            LinearLayout ll = (LinearLayout) myActivity.findViewById(R.id.categoryButtonsLayout);
            for (int i = 0; i < result.size(); i++) {
                ll.addView(result.get(i));
            }
        }
    }
}
