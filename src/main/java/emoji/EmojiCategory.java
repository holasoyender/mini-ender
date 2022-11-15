package emoji;

/**
 * Enum representation of the category for this emoji
 */
public enum EmojiCategory {
    ACTIVITY("Activities"),
    FLAGS("Flags"),
    FOOD("Food & Drink"),
    NATURE("Animals & Nature"),
    OBJECTS("Objects"),
    PEOPLE("People & Body"),
    SYMBOLS("Symbols"),
    TRAVEL("Travel & Places"),
    SMILEYS("Smileys & Emotion"),
    UNKNOWN("");

    private final String displayName;

    EmojiCategory(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Parses the given string to the respective category constant
     *
     * @param  str
     *         The display string
     *
     * @return The category or {@link #UNKNOWN}
     */
    public static EmojiCategory fromString(String str) {
        for (EmojiCategory category : values()) {
            if (category.displayName.equalsIgnoreCase(str))
                return category;
        }

        return UNKNOWN;
    }

    /**
     * The display name of this category
     *
     * @return The display name
     */
    public String getDisplayName() {
        return displayName;
    }
}
