package emoji;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class EmojiLoader {
    private EmojiLoader() {
    }

    @NotNull
    public static List<Emoji> loadEmojis(@NotNull InputStream stream) throws IOException {
        Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
        JSONArray emojisJSON = new JSONArray(new JSONTokener(reader));
        List<Emoji> emojis = new ArrayList<>(emojisJSON.length());
        for (int i = 0; i < emojisJSON.length(); i++) {
            Emoji emoji = buildEmojiFromJSON(emojisJSON.getJSONObject(i));
            if (emoji != null) {
                emojis.add(emoji);
            }
        }
        return emojis;
    }

    @NotNull
    public static Map<String, Emoji> loadEmojiBundle() throws IOException {
        try (Reader reader = new InputStreamReader(Objects.requireNonNull(EmojiLoader.class.getResourceAsStream("/emoji-definitions.json")), StandardCharsets.UTF_8)) {
            JSONObject file = new JSONObject(new JSONTokener(reader));
            JSONArray definitions = file.getJSONArray("emojiDefinitions");
            Map<String, Emoji> map = new HashMap<>(definitions.length()+1);
            for (int i = 0; i < definitions.length(); i++) {
                JSONObject json = definitions.getJSONObject(i);
                if (!json.has("category")) continue;

                String key = json.getString("surrogates");
                String primaryName = json.getString("primaryName");
                boolean supportsFitzpatrick = primaryName.contains("_tone");
                if (supportsFitzpatrick) {
                    key = key.substring(0, key.length() - 2);
                    if (map.containsKey(key)) {
                        map.put(key, map.get(key).setFitzpatrick(true));
                        continue;
                    }
                }

                byte[] bytes = key.getBytes(StandardCharsets.UTF_8);
                List<String> aliases = jsonArrayToStringList(json.getJSONArray("names"));
                List<String> tags = Collections.emptyList();
                EmojiCategory category = convertCategory(json.getString("category"));
                Emoji emoji = new Emoji("", supportsFitzpatrick, category, aliases, tags, bytes);
                map.put(key, emoji);
            }
            return map;
        }
    }

    private static EmojiCategory convertCategory(String raw) {
        for (EmojiCategory category : EmojiCategory.values()) {
            if (category.name().equalsIgnoreCase(raw))
                return category;
        }
        return EmojiCategory.UNKNOWN;
    }

    protected static Emoji buildEmojiFromJSON(JSONObject json) {
        if (!json.has("emoji")) {
            return null;
        }

        byte[] bytes = json.getString("emoji").getBytes(StandardCharsets.UTF_8);
        boolean supportsFitzpatrick = json.optBoolean("skin_tones", false);
        List<String> aliases = jsonArrayToStringList(json.getJSONArray("aliases"));
        List<String> tags = jsonArrayToStringList(json.getJSONArray("tags"));
        String description = json.getString("description");
        EmojiCategory category = EmojiCategory.fromString(json.optString("category", "UNKNOWN"));
        return new Emoji(description, supportsFitzpatrick, category, aliases, tags, bytes);
    }

    private static List<String> jsonArrayToStringList(JSONArray array) {
        List<String> strings = new ArrayList<>(array.length());
        for (int i = 0; i < array.length(); i++) {
            strings.add(array.getString(i));
        }
        return strings;
    }
}
