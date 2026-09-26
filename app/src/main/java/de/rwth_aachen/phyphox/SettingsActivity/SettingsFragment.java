package de.rwth_aachen.phyphox.SettingsActivity;


import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SeekBarPreference;

import de.rwth_aachen.phyphox.R;
import de.rwth_aachen.phyphox.device.LanguageHelper;
import de.rwth_aachen.phyphox.helper.FileNameFormat;

public class SettingsFragment extends PreferenceFragmentCompat {

    public static final String GRAPH_SIZE_KEY = "graph_size_dialog";

    public static final String DARK_MODE_ON = "1";
    public static final String DARK_MODE_OFF = "2";
    public static final String DARK_MODE_SYSTEM = "3";


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        PreferenceManager.setDefaultValues(requireContext(), R.xml.settings, false);
        setPreferencesFromResource(R.xml.settings, rootKey);

        setupPortEditText();
        setupFileNameFormatEditText();
        prepareLanguageList();
        updateCurrentLanguage();
        updateTheme();
        updateGraphSize();
    }

    private void setupPortEditText() {
        EditTextPreference editTextPreference = findPreference("remoteAccessPort");
        if (editTextPreference != null) {
            editTextPreference.setOnBindEditTextListener(editText -> editText.setInputType(InputType.TYPE_CLASS_NUMBER));
            editTextPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                int v = Integer.parseInt(newValue.toString());
                if ((v >= 1024) && (v < 65536)) {
                    return true;
                } else {
                    Toast.makeText(getContext(), "Allowed range: 1024-65535", Toast.LENGTH_LONG).show();
                    return false;
                }
            });
        }
    }

    private void setupFileNameFormatEditText() {
        EditTextPreference editTextPreference = findPreference(FileNameFormat.PREF_KEY);
        if (editTextPreference != null) {
            editTextPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                //An empty template makes no sense, so clearing the text resets it to the default
                if (newValue.toString().trim().isEmpty()) {
                    editTextPreference.setText(FileNameFormat.DEFAULT_FORMAT);
                    return false;
                }
                return true;
            });
        }
    }

    private void updateCurrentLanguage() {
        ListPreference lp = findPreference("language");
        if(lp != null){
            lp.setValue(LanguageHelper.currentPreference());
        }
    }

    // This engine turns the rawValues from locale eg: ["en", "cs","de","el","es"]
    // to the actula name of language like : Czech, Dutch, English, German and so on

    private void prepareLanguageList() {
        ListPreference lp = findPreference("language");

        String[] values = {LanguageHelper.SYSTEM, LanguageHelper.ZH, LanguageHelper.EN};
        String[] names = {
                getString(R.string.phyerma_lang_system),
                getString(R.string.phyerma_lang_zh),
                getString(R.string.phyerma_lang_en)
        };

        lp.setEntries(names);
        lp.setEntryValues(values);

        lp.setOnPreferenceChangeListener((preference, newValue) -> {
            LanguageHelper.apply(newValue.toString());
            updateCurrentLanguage();
            return true;
        });
    }

    private void updateTheme() {
        ListPreference lp = findPreference(getString(R.string.setting_dark_mode_key));
        CharSequence[] entries;
        CharSequence[] entryValues;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            entries = new CharSequence[]{
                    getString(R.string.settings_mode_dark),
                    getString(R.string.settings_mode_no_dark)};
            entryValues = new CharSequence[]{DARK_MODE_ON, DARK_MODE_OFF};
        } else {
            entries = new CharSequence[]{
                    getString(R.string.settings_mode_dark),
                    getString(R.string.settings_mode_no_dark),
                    getString(R.string.settings_mode_dark_system)};
            entryValues = new CharSequence[]{DARK_MODE_ON, DARK_MODE_OFF, DARK_MODE_SYSTEM};
        }
        if(lp != null){
            lp.setEntries(entries);
            lp.setEntryValues(entryValues);
            lp.setOnPreferenceChangeListener((preference, newValue) -> {
                lp.setValue(newValue.toString());
                setApplicationTheme(newValue.toString());
                return true;
            });
        }
    }



    private void updateGraphSize(){
        SeekBarPreference lp = findPreference(GRAPH_SIZE_KEY);
        assert lp != null;
        lp.setOnPreferenceChangeListener((preference, newValue) -> {
            lp.setValue((Integer) newValue);
            return true;
        });
    }

    public static void setApplicationTheme(String themePreference){
        if(themePreference.equals(DARK_MODE_SYSTEM)){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        } else if(themePreference.equals(DARK_MODE_OFF)){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
    }


}
