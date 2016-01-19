package com.tasomaniac.openwith;

public interface Analytics {

    void sendScreenView(String screenName);

    void sendEvent(String category, String action, String label, long value);

    void sendEvent(String category, String action, String label);
}
