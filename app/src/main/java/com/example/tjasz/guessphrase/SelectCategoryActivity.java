package com.example.tjasz.guessphrase;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;


public class SelectCategoryActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_category);
    }

    public void onCategorySelected(View v) {
        // create an intent to start the GameActivity
        Intent intent = new Intent(SelectCategoryActivity.this, GameActivity.class);
        // indicate the category resource ID based on the ID of the button pressed
        int vId = v.getId();
        if (vId == R.id.generalCategoryButton) {
            intent.putExtra("assetFilename", "category/general.json");
        }
        else if (vId == R.id.animalCategoryButton) {
            intent.putExtra("assetFilename", "category/animals.json");
        }
        else if (vId == R.id.foodCategoryButton) {
            intent.putExtra("assetFilename", "category/food.json");
        }
        else if (vId == R.id.unitedStatesCategoryButton) {
            intent.putExtra("assetFilename", "category/unitedStates.json");
        }
        else if (vId == R.id.geographyCategoryButton) {
            intent.putExtra("assetFilename", "category/geography.json");
        }
        else if (vId == R.id.sportsCategoryButton) {
            intent.putExtra("assetFilename", "category/sports.json");
        }
        else if (vId == R.id.entertainmentCategoryButton) {
            intent.putExtra("assetFilename", "category/entertainment.json");
        }
        else if (vId == R.id.dirtyCategoryButton) {
            intent.putExtra("assetFilename", "category/dirty.json");
        }
        // start GameActivity
        intent.putExtra("resumingGame", false);
        startActivity(intent);
    }

}
