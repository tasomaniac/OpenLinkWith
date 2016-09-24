package com.tasomaniac.openwith.data;

import android.content.Context;

import com.tasomaniac.openwith.AppComponent;

public final class Injector {
  private static final String INJECTOR_SERVICE = "com.tasomaniac.injector";

  @SuppressWarnings({"ResourceType", "WrongConstant"}) // Explicitly doing a custom service.
  public static AppComponent obtain(Context context) {
    return (AppComponent) context.getApplicationContext().getSystemService(INJECTOR_SERVICE);
  }

  public static boolean matchesService(String name) {
    return INJECTOR_SERVICE.equals(name);
  }

  private Injector() {
    throw new AssertionError("No instances.");
  }
}
