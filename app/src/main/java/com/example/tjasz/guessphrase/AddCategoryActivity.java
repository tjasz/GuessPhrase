package com.example.tjasz.guessphrase;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;


public class AddCategoryActivity extends ActionBarActivity {
    RelativeLayout wikiBaseContainer;
    EditText lastWikiBase;
    Button addWikiBaseButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_category);
        wikiBaseContainer = (RelativeLayout) findViewById(R.id.wiki_bases_container);
        lastWikiBase = (EditText) findViewById(R.id.first_wiki_base);
        addWikiBaseButton = (Button) findViewById(R.id.add_wiki_base_button);
    }

    public void addNewWikiBaseEditText(View v) {
        if (v.getId() == R.id.add_wiki_base_button) {
            EditText newEditText = new EditText(this);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            lp.addRule(RelativeLayout.BELOW, lastWikiBase.getId());
            newEditText.setLayoutParams(lp);
            newEditText.setId(View.generateViewId());
            wikiBaseContainer.addView(newEditText);
            RelativeLayout.LayoutParams button_lp = (RelativeLayout.LayoutParams) addWikiBaseButton.getLayoutParams();
            button_lp.addRule(RelativeLayout.BELOW, newEditText.getId());
            addWikiBaseButton.setLayoutParams(button_lp);
            lastWikiBase = newEditText;
        }
    }

    public void cancel(View v) {
        finish();
    }

    public void saveCategory(View v) {
        new SaveCategoryTask().execute();
    }

    private class SaveCategoryTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(Void... params) {
            // build a category object from the values of the edit texts
            Category cat = new Category();
            EditText titleEditText = (EditText) findViewById(R.id.category_title_edit_text);
            cat.setName(titleEditText.getText().toString());
            for (int i = 0; i < wikiBaseContainer.getChildCount(); i++) {
                View child = wikiBaseContainer.getChildAt(i);
                if (child.getId() != addWikiBaseButton.getId()) {
                    EditText et = (EditText) child;
                    cat.addItems(Wikipedia.getLinks(et.getText().toString()));
                }
            }
            // save the category to a file
            try {
                String filename = cat.getName().replaceAll("[^A-Za-z0-9]", "_") + ".json";
                File file = new File(getExternalFilesDir(null), filename);
                FileOutputStream fos = new FileOutputStream(file);
                cat.writeJSONFile(fos);
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Toast.makeText(
                    AddCategoryActivity.this,
                    getResources().getString(R.string.category_saved),
                    Toast.LENGTH_LONG).show();
            finish();
        }
    }
}
