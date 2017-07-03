package com.example.tjasz.guessphrase;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class CategoryButton extends Button {
    Context myContext;
    String assetPath;

    CategoryButton(Context context, String newAssetPath) {
        super(context);
        myContext = context;
        assetPath = newAssetPath;
        // set the default display properties of the CategoryButton
        setEnabled(true);
        setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        // set the onClickListener
        setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // create an intent to start the GameActivity
                Intent intent = new Intent(myContext, GameActivity.class);
                // indicate the category's file path
                intent.putExtra("assetFilename", assetPath);
                // start GameActivity
                intent.putExtra("resumingGame", false);
                myContext.startActivity(intent);
            }
        });
    }
}
