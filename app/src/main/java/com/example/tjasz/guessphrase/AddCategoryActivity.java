package com.example.tjasz.guessphrase;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar


public class AddCategoryActivity extends ActionBarActivity {
    private static final int MINIMUM_SEARCH_TERM_LENGTH = 3;
    private static final int MY_NOTIFICATION_ID = 2017072719;

    EditText titleEditText;
    ListView wikiBaseListView;
    WikiBaseAdapter adapter;
    DelayAutoCompleteTextView wikiBaseSearcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_category);
        titleEditText = (EditText) findViewById(R.id.category_title_edit_text);
        adapter = new WikiBaseAdapter(this);
        wikiBaseListView = (ListView) findViewById(R.id.wiki_bases_list_view);
        wikiBaseListView.setAdapter(adapter);
        wikiBaseSearcher = (DelayAutoCompleteTextView) findViewById(R.id.wiki_base_searcher);
        wikiBaseSearcher.setThreshold(MINIMUM_SEARCH_TERM_LENGTH);
        wikiBaseSearcher.setAdapter(new WikiPageAutoCompleteAdapter(this));
        wikiBaseSearcher.setLoadingIndicator(
                (ProgressBar) findViewById(R.id.wiki_base_loading_wheel));
        wikiBaseSearcher.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String suggestion = (String) adapterView.getItemAtPosition(position);
                adapter.add(suggestion);
                wikiBaseSearcher.setText("");
            }
        });
    }

    public void cancel(View v) {
        finish();
    }

    public void saveCategory(View v) {
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
        else if (adapter.getCount() <= 0) {
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

    private class SaveCategoryTask extends AsyncTask<Void, Void, Category> {
        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Category doInBackground(Void... params) {
            // build a category object from the values of the edit texts
            Category cat = new Category(AddCategoryActivity.this);
            cat.setName(titleEditText.getText().toString());
            cat.setIsCustom(true);
            String filename = cat.getName().replaceAll("[^A-Za-z0-9]", "_") + ".json";
            cat.setPath(filename);
            for (int i = 0; i < adapter.getCount(); i++) {
                String title = adapter.getItem(i);
                if (title.length() > 0) {
                    cat.addItems(Wikipedia.getLinks(title));
                }
            }
            // save the category to a file
            cat.writeJSONFile();
            return cat;
        }

        @Override
        protected void onPostExecute(Category result) {
            // create an intent to restart the SelectCategoryActivity
            final Intent restartSelectCategoryActivityIntent =
                    new Intent(AddCategoryActivity.this, SelectCategoryActivity.class);
            // create a pending intent to encapsulate the restartSelectCategoryActivity intent
            PendingIntent pi = PendingIntent.getActivity(
                AddCategoryActivity.this,
                MY_NOTIFICATION_ID,
                restartSelectCategoryActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            );
            // build a notification to say the download has been completed
            Notification.Builder notificationBuilder = new Notification.Builder(AddCategoryActivity.this)
                    .setSmallIcon(android.R.drawable.stat_sys_download_done)
                    .setAutoCancel(true)
                    .setContentTitle(getResources().getString(R.string.category_saved))
                    .setContentText("\"" + result.getName() + "\" " +
                            getResources().getString(R.string.category_saved_detail))
                    .setContentIntent(pi);
            // send the notification
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(MY_NOTIFICATION_ID, notificationBuilder.build());
            finish();
        }
    }
}
