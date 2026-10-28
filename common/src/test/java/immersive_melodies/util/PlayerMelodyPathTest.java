package immersive_melodies.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerMelodyPathTest {
    @Test
    void keepsHistoricalEscapeForOrdinaryNames() {
        assertEquals("steve", Utils.sanitizePlayerName("Steve"));
        assertEquals("playername", Utils.sanitizePlayerName("Player Name"));
        assertEquals("alicebob", Utils.sanitizePlayerName("alice/bob"));
        assertEquals("notch.2", Utils.sanitizePlayerName("Notch.2"));
        assertEquals("foo..bar", Utils.sanitizePlayerName("foo..bar"));
    }

    @Test
    void rejectsEmptyDotAndParentSegments() {
        assertFalse(Utils.sanitizePlayerName("..").contains(".."));
        assertFalse(Utils.sanitizePlayerName(".").equals("."));
        assertFalse(Utils.sanitizePlayerName("").isEmpty());
        assertFalse(Utils.sanitizePlayerName("../../../../mods").contains("/"));
        assertTrue(Utils.sanitizePlayerName("..").matches("p[0-9a-f]{16}"));
        assertNotEquals(Utils.sanitizePlayerName(".."), Utils.sanitizePlayerName("."));
        assertNotEquals(Utils.sanitizePlayerName("玩家"), Utils.sanitizePlayerName("プレイヤー"));
    }

    @Test
    void ownershipIsExactPlayerSegment() {
        assertTrue(Utils.ownsPlayerPath("steve/song", "steve"));
        assertTrue(Utils.ownsPlayerPath("steve/550e8400-e29b-41d4-a716-446655440000", "steve"));
        assertFalse(Utils.ownsPlayerPath("steve/../../outside", "steve"));
        assertFalse(Utils.ownsPlayerPath("steve2/song", "steve"));
        assertFalse(Utils.ownsPlayerPath("alice/bob/uuid", "alice"));
        assertFalse(Utils.ownsPlayerPath("../uuid", ".."));
        assertFalse(Utils.ownsPlayerPath("other/song", "steve"));
    }
}
