package immersive_melodies.resources;

import net.minecraft.network.FriendlyByteBuf;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Track {
    private final List<Note> notes;
    private final String name;
    private int cachedLength = -1;

    public Track(String name, List<Note> notes) {
        this.name = name;
        this.notes = notes;
    }

    public Track(FriendlyByteBuf b) {
        name = b.readUtf();

        int noteCount = b.readInt();
        notes = new LinkedList<>();
        for (int i = 0; i < noteCount; i++) {
            notes.add(new Note(b));
        }
    }

    public List<Note> getNotes() {
        return Collections.unmodifiableList(notes);
    }

    public String getName() {
        return name;
    }

    public void encode(FriendlyByteBuf b) {
        b.writeUtf(name);

        b.writeInt(notes.size());
        for (Note note : notes) {
            note.encode(b);
        }
    }

    public int getLength() {
        if (cachedLength >= 0) {
            return cachedLength;
        }
        cachedLength = notes.stream()
                .mapToInt(note -> note.getTime() + note.getLength())
                .max()
                .orElse(0);
        return cachedLength;
    }

    public void setNotes(List<Note> notes) {
        this.notes.clear();
        this.notes.addAll(notes);
        this.cachedLength = -1;
    }
}
