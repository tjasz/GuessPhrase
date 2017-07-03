package com.example.tjasz.guessphrase;

import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import java.io.IOException;
import java.util.ArrayList;


public class SelectCategoryActivity extends ActionBarActivity {
    LinearLayout ll;
    ProgressBar pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_category);
        // add a loading wheel to the display while categories load
        ll = (LinearLayout) findViewById(R.id.categoryButtonsLayout);
        pb = new ProgressBar(this);
        pb.setIndeterminate(true);
        ll.addView(pb);
        // load the categories and create buttons for them
        new LoadCategoriesTask().execute(this);
    }

    private class LoadCategoriesTask extends AsyncTask<SelectCategoryActivity, Void, ArrayList<CategoryButton>> {
        @Override
        protected void onPreExecute() {

        }

        @Override
        protected ArrayList<CategoryButton> doInBackground(SelectCategoryActivity... params) {
            // create a button for each category in the assets folder
            ArrayList<CategoryButton> result = new ArrayList<>();
            AssetManager am = getAssets();
            try {
                String[] assets = am.list("category");
                for (int i = 0; i < assets.length; i++) {
                    Category cat = new Category();
                    cat.readJSONFile(am.open("category/" + assets[i]));
                    CategoryButton newButton = new CategoryButton(params[0], "category/" + assets[i]);
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
            // remove the indeterminate ProgressBar
            ll.removeView(pb);
            // add the category buttons to the LinearLayout in SelectCategoryActivity
            for (int i = 0; i < result.size(); i++) {
                ll.addView(result.get(i));
            }
        }
    }
}
