package com.tasomaniac.openwith.resolver;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class ChooserHistoryTest {

    @Test
    public void fromSettings() {

        String anySaveString = "key1|5#key2|3";
        ChooserHistory history = ChooserHistory.fromSettings(anySaveString);

        assertEquals(anySaveString, history.getAsSaveString());
    }

    @Test(expected = NumberFormatException.class)
    public void shouldThrowExceptionForInvalid() {

        String anySaveString = "key1|5#key2|a";
        ChooserHistory history = ChooserHistory.fromSettings(anySaveString);

        assertEquals(anySaveString, history.getAsSaveString());
    }

    @Test
    public void shouldOmitInvalidParts() {

        String anySaveString = "key1|5#key2#key3|4";
        ChooserHistory history = ChooserHistory.fromSettings(anySaveString);

        assertEquals("key1|5#key3|4", history.getAsSaveString());
    }

    @Test
    public void shouldOmitInvalidLastPart() {

        String anySaveString = "key1|5#key2|5#key3";
        ChooserHistory history = ChooserHistory.fromSettings(anySaveString);

        assertEquals("key1|5#key2|5", history.getAsSaveString());
    }

}
