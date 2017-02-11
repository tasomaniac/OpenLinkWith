package com.tasomaniac.openwith.resolver;

import android.content.SharedPreferences;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static junit.framework.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

public class ChooserHistoryTest {

    @Rule public MockitoRule rule = MockitoJUnit.rule();

    @Mock SharedPreferences preferences;

    @Test
    public void fromSettings() {
        String anySaveString = "key1|5#key2|3";
        givenSaveString(anySaveString);

        ChooserHistory history = ChooserHistory.fromSettings(preferences);

        assertEquals(anySaveString, history.getAsSaveString());
    }

    @Test(expected = NumberFormatException.class)
    public void shouldThrowExceptionForInvalid() {
        String anySaveString = "key1|5#key2|a";
        givenSaveString(anySaveString);

        ChooserHistory history = ChooserHistory.fromSettings(preferences);

        assertEquals(anySaveString, history.getAsSaveString());
    }

    @Test
    public void shouldOmitInvalidParts() {
        givenSaveString("key1|5#key2#key3|4");

        ChooserHistory history = ChooserHistory.fromSettings(preferences);

        assertEquals("key1|5#key3|4", history.getAsSaveString());
    }

    @Test
    public void shouldOmitInvalidLastPart() {
        givenSaveString("key1|5#key2|5#key3");

        ChooserHistory history = ChooserHistory.fromSettings(preferences);

        assertEquals("key1|5#key2|5", history.getAsSaveString());
    }

    private void givenSaveString(String anySaveString) {
        given(preferences.getString(anyString(), anyString())).willReturn(anySaveString);
    }

}
