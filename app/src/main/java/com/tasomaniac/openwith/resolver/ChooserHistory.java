package com.tasomaniac.openwith.resolver;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;

class ChooserHistory {

    private static final String KEY_HISTORY = "history";

    private static final char SEPARATOR_KEY_VALUE = '|';
    private static final String SEPARATOR_KEY_VALUE_ESCAPED = "\\|";
    private static final char SEPARATOR_ITEMS = '#';

    private final SharedPreferences preferences;
    private final HashMap<String, Integer> mHistoryMap = new HashMap<>();

    static ChooserHistory fromSettings(Context context) {
        return fromSettings(getPreferences(context));
    }

    static ChooserHistory fromSettings(SharedPreferences preferences) {
        ChooserHistory history = new ChooserHistory(preferences);
        String saveString = preferences.getString(KEY_HISTORY, "");
        if (saveString.isEmpty()) {
            return history;
        }

        String[] items = saveString.split(String.valueOf(SEPARATOR_ITEMS));
        for (String item : items) {
            String[] split = item.split(SEPARATOR_KEY_VALUE_ESCAPED);
            if (split.length == 2) {
                history.mHistoryMap.put(split[0], Integer.valueOf(split[1]));
            }
        }
        return history;
    }

    private ChooserHistory(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    int get(String packageName) {
        Integer count = mHistoryMap.get(packageName);
        return count != null ? count : 0;
    }

    void add(String packageName) {
        Integer currentCount = mHistoryMap.get(packageName);
        if (currentCount == null) {
            mHistoryMap.put(packageName, 1);
        } else {
            mHistoryMap.put(packageName, currentCount + 1);
        }
    }

    void save() {
        preferences.edit().putString(KEY_HISTORY, getAsSaveString()).apply();
    }

    String getAsSaveString() {
        StringBuilder saveString = new StringBuilder();
        for (Map.Entry<String, Integer> entry : mHistoryMap.entrySet()) {
            saveString.append(entry.getKey());
            saveString.append(SEPARATOR_KEY_VALUE);
            saveString.append(entry.getValue().toString());
            saveString.append(SEPARATOR_ITEMS);
        }
        if (saveString.length() > 0) {
            saveString.deleteCharAt(saveString.length() - 1);
        }
        return saveString.toString();
    }

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences("bs_chooser", Context.MODE_PRIVATE);
    }
}
