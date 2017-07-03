package com.example.tjasz.guessphrase;

import android.content.res.AssetManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.widget.LinearLayout;

import java.io.IOException;


public class SelectCategoryActivity extends ActionBarActivity {

    AssetManager am;
    LinearLayout ll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_category);

        // dynamically add a Button for each Category available
        am = getAssets();
        ll = (LinearLayout) findViewById(R.id.categoryButtonsLayout);
        try {
            String[] assets = am.list("category");
            for (int i = 0; i < assets.length; i++) {
                Category cat = new Category();
                cat.readJSONFile(am.open("category/" + assets[i]));
                CategoryButton newButton = new CategoryButton(this, "category/" + assets[i]);
                newButton.setText(cat.getName());
                ll.addView(newButton);
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
