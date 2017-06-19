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
            intent.putExtra("categoryResourceId", R.array.categoryOriginal);
        }
        else if (vId == R.id.animalCategoryButton) {
            intent.putExtra("categoryResourceId", R.array.categoryAnimals);
        }
        else if (vId == R.id.foodCategoryButton) {
            intent.putExtra("categoryResourceId", R.array.categoryFood);
        }
        else if (vId == R.id.unitedStatesCategoryButton) {
            intent.putExtra("categoryResourceId", R.array.categoryUnitedStates);
        }
        else if (vId == R.id.geographyCategoryButton) {
            intent.putExtra("categoryResourceId", R.array.categoryGeography);
        }
        else if (vId == R.id.sportsCategoryButton) {
            intent.putExtra("categoryResourceId", R.array.categorySports);
        }
        else if (vId == R.id.entertainmentCategoryButton) {
            intent.putExtra("categoryResourceId", R.array.categoryEntertainment);
        }
        else if (vId == R.id.dirtyCategoryButton) {
            intent.putExtra("categoryResourceId", R.array.categoryDirty);
        }
        // start GameActivity
        intent.putExtra("resumingGame", false);
        startActivity(intent);
    }

}
