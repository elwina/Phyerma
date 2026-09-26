package de.rwth_aachen.phyphox.device;

import android.app.Activity;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import java.util.Locale;

import de.rwth_aachen.phyphox.R;

public final class LanguageHelper {
    public static final String SYSTEM = "*";
    public static final String ZH = "zh-CN";
    public static final String EN = "en";

    private LanguageHelper() {}

    public static String currentPreference() {
        Locale locale = AppCompatDelegate.getApplicationLocales().get(0);
        if (locale == null)
            return SYSTEM;
        if ("zh".equalsIgnoreCase(locale.getLanguage()))
            return ZH;
        return EN;
    }

    public static void apply(String preference) {
        if (preference == null || SYSTEM.equals(preference))
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList());
        else
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(preference));
    }

    public static void bind(Activity activity) {
        TextView chip = activity.findViewById(R.id.languageToggle);
        if (chip == null)
            return;
        String pref = currentPreference();
        if (SYSTEM.equals(pref))
            chip.setText(R.string.phyerma_lang_chip_auto);
        else if (ZH.equals(pref))
            chip.setText(R.string.phyerma_lang_chip_zh);
        else
            chip.setText(R.string.phyerma_lang_chip_en);
        chip.setOnClickListener(v -> showPicker(activity));
    }

    public static void showPicker(Activity activity) {
        String[] values = {SYSTEM, ZH, EN};
        String[] names = {
                activity.getString(R.string.phyerma_lang_system),
                activity.getString(R.string.phyerma_lang_zh),
                activity.getString(R.string.phyerma_lang_en)
        };
        new AlertDialog.Builder(activity)
                .setTitle(R.string.phyerma_lang_title)
                .setItems(names, (dialog, which) -> apply(values[which]))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}
