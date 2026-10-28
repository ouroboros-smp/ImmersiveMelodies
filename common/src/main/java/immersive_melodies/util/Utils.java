package immersive_melodies.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.regex.Pattern;

public class Utils {
    private static final Pattern UNSAFE_PLAYER_CHARS = Pattern.compile("[^a-z0-9_.-]");

    public static String toTitle(String string) {
        return StringUtils.capitalize(string.replace("_", " "));
    }

    /**
     * Single path segment used in upload ids ({@code player/<name>/<uuid>}).
     * Keeps the historical {@code [a-z0-9_.-]} escape so existing uploads still match,
     * and never returns an empty segment, {@code .}, or {@code ..}.
     */
    public static String getPlayerName(Player player) {
        String name = player.getGameProfile().getName();
        return sanitizePlayerName(name == null ? "" : name);
    }

    public static String sanitizePlayerName(String name) {
        String normalized = name.toLowerCase(Locale.ROOT);
        String escaped = UNSAFE_PLAYER_CHARS.matcher(normalized).replaceAll("");
        if (escaped.isEmpty() || ".".equals(escaped) || "..".equals(escaped)) {
            return fallbackPlayerSegment(normalized);
        }
        return escaped;
    }

    private static String fallbackPlayerSegment(String normalized) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(normalized.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder("p");
            for (int i = 0; i < 8; i++) {
                hex.append(String.format(Locale.ROOT, "%02x", digest[i]));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public static boolean isPlayerMelody(ResourceLocation identifier) {
        return identifier.getNamespace().equals("player");
    }

    public static boolean ownsMelody(ResourceLocation identifier, Player player) {
        if (!isPlayerMelody(identifier)) {
            return false;
        }
        return ownsPlayerPath(identifier.getPath(), getPlayerName(player));
    }

    /**
     * Uploads are exactly {@code <player>/<melody>}. A prefix match would let a shorter
     * name own nested paths such as {@code alice/../../outside}.
     */
    static boolean ownsPlayerPath(String path, String playerSegment) {
        String[] parts = path.split("/", -1);
        if (parts.length != 2 || parts[0].isEmpty() || parts[1].isEmpty()) {
            return false;
        }
        if (".".equals(parts[0]) || "..".equals(parts[0]) || ".".equals(parts[1]) || "..".equals(parts[1])) {
            return false;
        }
        return parts[0].equals(playerSegment);
    }

    public static boolean canDelete(ResourceLocation identifier, Player player) {
        return ownsMelody(identifier, player) || (isPlayerMelody(identifier) && player.hasPermissions(2));
    }

    public static String removeLastPart(String input, String delimiter) {
        int lastDotIndex = input.lastIndexOf(delimiter);

        if (lastDotIndex != -1) {
            return input.substring(0, lastDotIndex);
        } else {
            return input;
        }
    }

    public static String getLastPart(String input, String delimiter) {
        int lastDotIndex = input.lastIndexOf(delimiter);

        if (lastDotIndex != -1) {
            return input.substring(lastDotIndex + delimiter.length());
        } else {
            return input;
        }
    }
}
