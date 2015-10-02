package com.tasomaniac.openwith.resolver;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;

class ChooserHistory {

    private static final String KEY_HISTORY = "history";

    private static final char SEPARATOR_KEY_VALUE = '|';

    private static final char SEPARATOR_ITEMS = '#';

    HashMap<String, Integer> mHistoryMap;

    public static ChooserHistory fromSettings(Context context) {
        return fromSettings(getPreferences(context).getString(KEY_HISTORY, ""));
    }

    public static ChooserHistory fromSettings(String saveString) {
        ChooserHistory history = new ChooserHistory();
        if (saveString == null || saveString.length() == 0) {
            return history;
        }
        StringBuilder packageName = new StringBuilder();
        StringBuilder number = new StringBuilder();
        int i = 0;
        while (i < saveString.length()) {
            char character = saveString.charAt(i);
            if (character == SEPARATOR_KEY_VALUE) {
                // skip separator icon
                while (++i < saveString.length()) {
                    character = saveString.charAt(i);
                    if (character == SEPARATOR_ITEMS) {
                        break;
                    }
                    number.append(character);
                }
                history.mHistoryMap.put(packageName.toString(), Integer.valueOf(number.toString()));
                packageName.delete(0, packageName.length());
                number.delete(0, number.length());
            } else {
                packageName.append(character);
            }
            ++i;
        }
        return history;
    }

    public ChooserHistory() {
        mHistoryMap = new HashMap<>();
    }

    public void add(String packageName) {
        Integer currentCount = mHistoryMap.get(packageName);
        if (currentCount == null) {
            mHistoryMap.put(packageName, 1);
        } else {
            mHistoryMap.put(packageName, currentCount + 1);
        }
    }

    public void save(Context context) {
        SharedPreferences prefs = getPreferences(context);
        prefs.edit().putString(KEY_HISTORY, getAsSaveString()).apply();
    }

    private String getAsSaveString() {
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

    public int get(String packageName) {
        Integer count = mHistoryMap.get(packageName);
        return count != null ? count : 0;
    }
}
