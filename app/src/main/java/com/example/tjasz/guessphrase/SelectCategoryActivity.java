package com.example.tjasz.guessphrase;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.ContextMenu;
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

    LinearLayout ll;
    ProgressBar pb;
    CategoryReferenceAdapter adapter;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_category);

        ll = (LinearLayout) findViewById(R.id.categoryButtonsLayout);
        pb = (ProgressBar) findViewById(R.id.loading_wheel);
        adapter = new CategoryReferenceAdapter(this);
        listView = (ListView) findViewById(R.id.category_list_view);
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
        registerForContextMenu(listView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // load the categories and create buttons for them
        new LoadCategoriesTask().execute(this);
    }

    private class LoadCategoriesTask extends AsyncTask<SelectCategoryActivity, Void, ArrayList<Category>> {
        @Override
        protected void onPreExecute() {
            adapter.removeAll();
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
            String[] customCategoryFiles = customCategoryDir.list();
            for (int i = 0; i < customCategoryFiles.length; i++) {
                Category cat = new Category(SelectCategoryActivity.this);
                cat.setIsCustom(true);
                cat.setPath(customCategoryFiles[i]);
                cat.readJSONFile();
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

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == listView.getId()) {
            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) menuInfo;
            Category cat = (Category) listView.getItemAtPosition(acmi.position);
            if (cat.getIsCustom()) {
                menu.add(Menu.NONE, CONTEXT_DELETE, 0,
                        getResources().getString(R.string.delete) + " \"" + cat.getName() + "\"");
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // get category corresponding to selected item
        AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Category cat = (Category) listView.getItemAtPosition(acmi.position);
        // handle selected menu action
        if (item.getItemId() == CONTEXT_DELETE) {
            cat.deleteFile();
            adapter.removeAtPosition(acmi.position);
            adapter.notifyDataSetChanged();
            return true;
        }
        return super.onContextItemSelected(item);
    }
}
