package com.example.tjasz.guessphrase;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.CheckBox;

public class SettingsActivity extends ActionBarActivity {

    public static final String GAME_PREFERENCES = "gamePrefs";
    public static final String VIBRATION_PREFERENCE_KEY = "vibrate";

    CheckBox vibrationCheckbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        vibrationCheckbox = (CheckBox) findViewById(R.id.vibration_checkbox);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // load preferences
        SharedPreferences preferences = getSharedPreferences(GAME_PREFERENCES, MODE_PRIVATE);
        vibrationCheckbox.setChecked(preferences.getBoolean(VIBRATION_PREFERENCE_KEY, true));
    }

    @Override
    protected void onPause() {
        // save preferences
        SharedPreferences preferences = getSharedPreferences(GAME_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(VIBRATION_PREFERENCE_KEY, vibrationCheckbox.isChecked());
        editor.commit();
        super.onPause();
    }

    public void finishOverride(View v) {
        finish();
    }

}
