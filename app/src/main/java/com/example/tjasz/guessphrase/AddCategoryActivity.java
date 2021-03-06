package com.example.tjasz.guessphrase;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * This activity allows users to create a custom category
 * based on page links in related Wikipedia pages.
 * This is accomplished by allowing users to search for Wikipedia pages
 * in a DelayAutoCompleteTextView through a WikiPageAutoCompleteAdapter.
 * Selected items are then shown in a ListView through a WikiBaseAdapter.
 * Category files are saved asynchronously when the user taps "Save".
 * When the download is completed: if SelectCategoryActivity is active,
 * the category list is refershed; if SelectCategoryActivity is not active,
 * a notification is shown to the user with an action that reopens SelectCategoryActivity.
 */

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
            finish();
            new SaveCategoryTask(titleEditText.getText().toString()).executeOnExecutor(
                        AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private class SaveCategoryTask extends AsyncTask<Void, Void, Category> {
        private String catName;

        SaveCategoryTask(String name) {
            catName = name;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Category doInBackground(Void... params) {
            // build a category object from the values of the edit texts
            Category cat = new Category(AddCategoryActivity.this);
            cat.setName(catName);
            cat.setIsCustom(true);
            // generate a filename based on category name and current datetime
            String filename = catName.replaceAll("[^A-Za-z0-9]", "_");
            DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
            Date date = new Date();
            String dateString = dateFormat.format(date);
            filename += "_" + dateString + ".json";
            // set the filename to the category
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
            notifyCategorySaved(result);
        }
    }

    private void notifyCategorySaved(final Category result) {
        sendOrderedBroadcast(
                new Intent(SelectCategoryActivity.CATEGORIES_REFRESH_ACTION),
                null,
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        // if SelectCategoryActivity is not active, send notification
                        if (getResultCode() != SelectCategoryActivity.IS_ACTIVE) {
                            sendSystemNotification(result);
                        }
                    }
                },
                null,
                RESULT_OK,
                null,
                null);
    }

    private void sendSystemNotification(Category result) {
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_category, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add_category_help) {
            AlertDialog alertDialog = new AlertDialog.Builder(AddCategoryActivity.this).create();
            alertDialog.setCancelable(true);
            alertDialog.setCanceledOnTouchOutside(true);
            alertDialog.setTitle(getResources().getString(R.string.action_add_category_help));
            alertDialog.setMessage(getResources().getString(R.string.add_category_help_message));
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.okay),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
