package immersive_melodies.resources;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MelodyFilePathTest {
    @Test
    void melodyFilesStayInsideTheMelodyDirectory(@TempDir Path root) throws Exception {
        Path file = ServerMelodyManager.resolveMelodyFile(root.toFile(), "player:steve/song");
        assertTrue(file.startsWith(root.toAbsolutePath().normalize()));
        assertTrue(file.endsWith(Path.of("player", "steve", "song.bin")));
    }

    @Test
    void melodyFilesRejectTraversal(@TempDir Path root) {
        assertThrows(IOException.class, () -> ServerMelodyManager.resolveMelodyFile(root.toFile(), "player:../../../../mods/uuid"));
        assertThrows(IOException.class, () -> ServerMelodyManager.resolveMelodyFile(root.toFile(), "player:../uuid"));
        assertThrows(IOException.class, () -> ServerMelodyManager.resolveMelodyFile(root.toFile(), "player:steve/../../outside"));
        assertThrows(IOException.class, () -> ServerMelodyManager.resolveMelodyFile(root.toFile(), "player:steve\\..\\outside"));
        assertThrows(IOException.class, () -> ServerMelodyManager.resolveMelodyFile(root.toFile(), null));
    }
}
