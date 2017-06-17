package com.example.tjasz.guessphrase;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;


public class SelectCategoryActivity extends ActionBarActivity {
    String[] generalItems;
    String[] animalItems;
    String[] foodItems;
    String[] unitedStatesItems;
    String[] geographyItems;
    String[] sportsItems;
    String[] entertainmentItems;
    String[] dirtyItems;

    CheckBox generalCategoryCheckbox;
    CheckBox animalCategoryCheckbox;
    CheckBox foodCategoryCheckbox;
    CheckBox unitedStatesCategoryCheckbox;
    CheckBox geographyCategoryCheckbox;
    CheckBox sportsCategoryCheckbox;
    CheckBox entertainmentCategoryCheckbox;
    CheckBox dirtyCategoryCheckbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_category);

        generalCategoryCheckbox = (CheckBox) findViewById(R.id.generalCategoryCheckbox);
        animalCategoryCheckbox = (CheckBox) findViewById(R.id.animalCategoryCheckbox);
        foodCategoryCheckbox = (CheckBox) findViewById(R.id.foodCategoryCheckbox);
        unitedStatesCategoryCheckbox = (CheckBox) findViewById(R.id.unitedStatesCategoryCheckbox);
        geographyCategoryCheckbox = (CheckBox) findViewById(R.id.geographyCategoryCheckbox);
        sportsCategoryCheckbox = (CheckBox) findViewById(R.id.sportsCategoryCheckbox);
        entertainmentCategoryCheckbox = (CheckBox) findViewById(R.id.entertainmentCategoryCheckbox);
        dirtyCategoryCheckbox = (CheckBox) findViewById(R.id.dirtyCategoryCheckbox);

        generalItems = getResources().getStringArray(R.array.categoryOriginal);
        animalItems = getResources().getStringArray(R.array.categoryAnimals);
        foodItems = getResources().getStringArray(R.array.categoryFood);
        unitedStatesItems = getResources().getStringArray(R.array.categoryUnitedStates);
        geographyItems = getResources().getStringArray(R.array.categoryGeography);
        sportsItems = getResources().getStringArray(R.array.categorySports);
        entertainmentItems = getResources().getStringArray(R.array.categoryEntertainment);
        dirtyItems = getResources().getStringArray(R.array.categoryDirty);
    }

    public void onSelectCategoryDonePressed(View v) {
        if (!generalCategoryCheckbox.isChecked() &&
            !animalCategoryCheckbox.isChecked() &&
            !foodCategoryCheckbox.isChecked() &&
            !unitedStatesCategoryCheckbox.isChecked() &&
            !geographyCategoryCheckbox.isChecked() &&
            !sportsCategoryCheckbox.isChecked() &&
            !entertainmentCategoryCheckbox.isChecked() &&
            !dirtyCategoryCheckbox.isChecked()
                ) {
                AlertDialog alertDialog = new AlertDialog.Builder(SelectCategoryActivity.this).create();
                alertDialog.setCancelable(true);
                alertDialog.setMessage(getResources().getString(R.string.noCategoriesWarning));
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getResources().getString(R.string.okay),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
        } else {
            Intent intent = new Intent(SelectCategoryActivity.this, GameActivity.class);
            boolean[] categoryBools = {
                    generalCategoryCheckbox.isChecked(),
                    animalCategoryCheckbox.isChecked(),
                    foodCategoryCheckbox.isChecked(),
                    unitedStatesCategoryCheckbox.isChecked(),
                    geographyCategoryCheckbox.isChecked(),
                    sportsCategoryCheckbox.isChecked(),
                    entertainmentCategoryCheckbox.isChecked(),
                    dirtyCategoryCheckbox.isChecked()
            };
            intent.putExtra("categoryBools", categoryBools);
            intent.putExtra("resumingGame", false);
            startActivity(intent);
        }
    }
}
