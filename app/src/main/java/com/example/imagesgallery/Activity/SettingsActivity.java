package com.example.imagesgallery.Activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.imagesgallery.R;

public class SettingsActivity extends PreferenceActivity {

    public android.preference.SwitchPreference switchPreferenceDarkMode;
    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from XML resource
        addPreferencesFromResource(R.xml.settings);

        /*switchPreferenceDarkMode = (SwitchPreference) findPreference("darkMode");

        switchPreferenceDarkMode.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                boolean checked = ((SwitchPreference) preference)
                        .isChecked();
                Log.d("checked",String.valueOf(checked));
                if(checked){
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                }
                else{
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
                return true;
            }
        });*/
        preferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals("darkMode")) {
                    // Handle dark mode preference change
                    boolean isDarkModeEnabled = sharedPreferences.getBoolean(key, false);
                    // Apply dark mode theme or update UI accordingly
                    setDarkMode(isDarkModeEnabled);
                }
            }
        };
    }

    // Optional: Define a preference change listener
   /* private Preference.OnPreferenceChangeListener preferenceChangeListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (preference.getKey().equals("darkMode")) {
                // Handle the switch preference change
                boolean isChecked = (boolean) newValue;
                // Perform any desired actions based on the new value
            }
            return true;
        }
    };*/
    @Override
    protected void onResume() {
        super.onResume();
        // Register the preference change listener
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener((SharedPreferences.OnSharedPreferenceChangeListener) preferenceChangeListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the preference change listener
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener((SharedPreferences.OnSharedPreferenceChangeListener) preferenceChangeListener);
    }

    private void setDarkMode(boolean enabled) {
        /*if(enabled){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }*/
        int mode = enabled ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
        AppCompatDelegate.setDefaultNightMode(mode);
        recreate(); // Recreate the activity to apply the new theme
    }
}
