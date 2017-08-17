package com.example.tjasz.guessphrase;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;


public class SelectCategoryActivity extends ActionBarActivity {
    private static final int CONTEXT_DELETE = 0;
    public static final int IS_ACTIVE = Activity.RESULT_FIRST_USER;
    public static final String CATEGORIES_REFRESH_ACTION = "com.example.tjasz.guessphrase.CATEGORIES_REFRESH_ACTION";

    LinearLayout ll;
    ProgressBar pb;
    CategoryReferenceAdapter adapter;
    ListView listView;
    private BroadcastReceiver refreshReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_category);

        ll = (LinearLayout) findViewById(R.id.categoryButtonsLayout);
        pb = (ProgressBar) findViewById(R.id.loading_wheel);
        adapter = new CategoryReferenceAdapter(this);
        listView = (ListView) findViewById(R.id.category_list_view);
        View footerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.view_footer_add_category, null, false);
        listView.addFooterView(footerView);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                Category cat = adapter.getItem(pos);

                // create an intent to start the GameActivity
                Intent intent = new Intent(SelectCategoryActivity.this, GameActivity.class);
                // indicate the category's file path
                intent.putExtra("isCustomCategory", cat.getIsCustom());
                intent.putExtra("path", cat.getPath());
                // start GameActivity
                intent.putExtra("resumingGame", false);
                startActivity(intent);
            }
        });
        refreshReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // if this is an ordered broadcast
                // let sender know that SelectCategoryActivity received it
                if (refreshReceiver.isOrderedBroadcast()) {
                    setResultCode(IS_ACTIVE);
                    // refresh categories; a new one has been saved
                    // ensure task executes asynchronously
                    if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ) {
                        new LoadCategoriesTask().executeOnExecutor(
                                AsyncTask.THREAD_POOL_EXECUTOR, SelectCategoryActivity.this);
                    } else {
                        new LoadCategoriesTask().execute(SelectCategoryActivity.this);
                    }
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        // load the categories and create buttons for them
        // ensure task executes asynchronously
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ) {
            new LoadCategoriesTask().executeOnExecutor(
                    AsyncTask.THREAD_POOL_EXECUTOR, this);
        } else {
            new LoadCategoriesTask().execute(this);
        }
        // register the receiver to receive a
        // CATEGORIES_REFRESH_ACTION broadcast
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CATEGORIES_REFRESH_ACTION);
        registerReceiver(refreshReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (refreshReceiver != null) {
            unregisterReceiver(refreshReceiver);
        }
    }

    private class LoadCategoriesTask extends AsyncTask<SelectCategoryActivity, Void, ArrayList<Category>> {
        @Override
        protected void onPreExecute() {
            // add a loading wheel to the display while categories load
            listView.setVisibility(View.INVISIBLE);
            pb.setVisibility(View.VISIBLE);
        }

        @Override
        protected ArrayList<Category> doInBackground(SelectCategoryActivity... params) {
            // create a button for each category in the assets folder
            ArrayList<Category> result = new ArrayList<>();
            AssetManager am = getAssets();
            try {
                String[] assets = am.list("category");
                for (int i = 0; i < assets.length; i++) {
                    Category cat = new Category(SelectCategoryActivity.this);
                    cat.setIsCustom(false);
                    cat.setPath(assets[i]);
                    cat.readJSONFile();
                    result.add(cat);
                }
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
            // also create a button for each category in the custom categories folder
            File customCategoryDir = new File(getExternalFilesDir(null), "category");
            if (!customCategoryDir.exists()) {
                customCategoryDir.mkdir();
            }
            String[] customCategoryFiles = customCategoryDir.list();
            for (int i = 0; i < customCategoryFiles.length; i++) {
                Category cat = new Category(SelectCategoryActivity.this);
                cat.setIsCustom(true);
                cat.setPath(customCategoryFiles[i]);
                try {
                    cat.readJSONFile();
                }
                catch (CategoryNotFoundException ex) {
                    // this really shouldn't happen here
                    throw new RuntimeException(ex);
                }
                result.add(cat);
            }
            return result;
        }

        @Override
        protected void onPostExecute(ArrayList<Category> result) {
            // remove the indeterminate ProgressBar
            pb.setVisibility(View.GONE);
            // add the category buttons to the LinearLayout in SelectCategoryActivity
            listView.setVisibility(View.VISIBLE);
            adapter.removeAll();
            adapter.addAll(result);
            adapter.notifyDataSetChanged();
        }
    }

    private boolean hasNetworkConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    private void openAddCategoryActivity() {
        Intent intent = new Intent(SelectCategoryActivity.this, AddCategoryActivity.class);
        startActivity(intent);
    }

    // this one wraps the other for use as an onClick method
    public void openAddCategoryActivity(View v) {
        openAddCategoryActivity();
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
            if (hasNetworkConnection()) {
                openAddCategoryActivity();
            }
            else {
                AlertDialog alertDialog = new AlertDialog.Builder(SelectCategoryActivity.this).create();
                alertDialog.setCancelable(true);
                alertDialog.setTitle(getResources().getString(R.string.error_connection_required_title));
                alertDialog.setMessage(getResources().getString(R.string.error_connection_required_message));
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.okay),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (hasNetworkConnection()) {
                                    dialog.dismiss();
                                    openAddCategoryActivity();
                                }
                                else {
                                    dialog.dismiss();
                                }
                            }
                        });
                alertDialog.show();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
