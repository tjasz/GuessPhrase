package com.example.tjasz.guessphrase;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;


public class AddCategoryActivity extends ActionBarActivity {
    private static final int MINIMUM_SEARCH_TERM_LENGTH = 3;

    EditText titleEditText;
    RelativeLayout wikiBaseContainer;
    DelayAutoCompleteTextView lastWikiBase;
    Button addWikiBaseButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_category);
        titleEditText = (EditText) findViewById(R.id.category_title_edit_text);
        wikiBaseContainer = (RelativeLayout) findViewById(R.id.wiki_bases_container);
        lastWikiBase = (DelayAutoCompleteTextView) findViewById(R.id.first_wiki_base);
        setupWikiBase(lastWikiBase);
        addWikiBaseButton = (Button) findViewById(R.id.add_wiki_base_button);
    }

    private void setupWikiBase(final DelayAutoCompleteTextView wikiBase) {
        wikiBase.setThreshold(MINIMUM_SEARCH_TERM_LENGTH);
        wikiBase.setAdapter(new WikiPageAutoCompleteAdapter(this));
        wikiBase.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String title = (String) adapterView.getItemAtPosition(position);
                wikiBase.setText(title);
            }
        });
    }

    public void addNewWikiBaseEditText(View v) {
        if (v.getId() == R.id.add_wiki_base_button) {
            DelayAutoCompleteTextView newEditText = new DelayAutoCompleteTextView(this);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            lp.addRule(RelativeLayout.BELOW, lastWikiBase.getId());
            newEditText.setLayoutParams(lp);
            newEditText.setId(View.generateViewId());
            wikiBaseContainer.addView(newEditText);
            newEditText.setFocusableInTouchMode(true);
            newEditText.requestFocus();
            lastWikiBase = newEditText;
            setupWikiBase(lastWikiBase);
        }
    }

    public void cancel(View v) {
        finish();
    }

    public void saveCategory(View v) {
        DelayAutoCompleteTextView firstWikiBase = (DelayAutoCompleteTextView) findViewById(R.id.first_wiki_base);
        if (titleEditText.getText().length() <= 0) {
            AlertDialog alertDialog = new AlertDialog.Builder(AddCategoryActivity.this).create();
            alertDialog.setCancelable(true);
            alertDialog.setTitle(getResources().getString(R.string.error_invalid_category));
            alertDialog.setMessage(getResources().getString(R.string.error_no_category_name));
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.okay),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }
        else if (firstWikiBase.getText().toString().trim().length() <= 0) {
            AlertDialog alertDialog = new AlertDialog.Builder(AddCategoryActivity.this).create();
            alertDialog.setCancelable(true);
            alertDialog.setTitle(getResources().getString(R.string.error_invalid_category));
            alertDialog.setMessage(getResources().getString(R.string.error_no_wiki_pages_selected));
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.okay),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }
        else {
            new SaveCategoryTask().execute();
        }
    }

    private class SaveCategoryTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(Void... params) {
            // build a category object from the values of the edit texts
            Category cat = new Category(AddCategoryActivity.this);
            cat.setName(titleEditText.getText().toString());
            cat.setIsCustom(true);
            String filename = cat.getName().replaceAll("[^A-Za-z0-9]", "_") + ".json";
            cat.setPath(filename);
            for (int i = 0; i < wikiBaseContainer.getChildCount(); i++) {
                View child = wikiBaseContainer.getChildAt(i);
                if (child.getId() != addWikiBaseButton.getId()) {
                    EditText et = (EditText) child;
                    String title = et.getText().toString().trim();
                    if (title.length() > 0) {
                        cat.addItems(Wikipedia.getLinks(title));
                    }
                }
            }
            // save the category to a file
            cat.writeJSONFile();
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
