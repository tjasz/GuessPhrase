package com.example.tjasz.guessphrase;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
    }

    @Override
    protected void onResume() {
        super.onResume();
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
                    CategoryButton newButton = new CategoryButton(params[0], false, assets[i]);
                    newButton.setText(cat.getName());
                    result.add(newButton);
                }
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
            // also create a button for each category in the custom categories folder
            File customCategoryDir = new File(getExternalFilesDir(null), "category");
            String[] customCategoryFiles = customCategoryDir.list();
            for (int i = 0; i < customCategoryFiles.length; i++) {
                Category cat = new Category();
                File file = new File(customCategoryDir, customCategoryFiles[i]);
                try {
                    FileInputStream fis = new FileInputStream(file);
                    cat.readJSONFile(fis);
                }
                catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
                CategoryButton newButton = new CategoryButton(params[0], true, customCategoryFiles[i]);
                newButton.setText(cat.getName());
                result.add(newButton);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_select_category, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add_category) {
            Intent intent = new Intent(SelectCategoryActivity.this, AddCategoryActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
