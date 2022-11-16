package emoji;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("unused")
public class EmojiParser {

    @NotNull
    public static String parseToAliases(@NotNull String input) {
        return parseToAliases(input, FitzpatrickAction.PARSE);
    }

    @NotNull
    public static String parseToAliases(
        @NotNull String input,
        @NotNull FitzpatrickAction fitzpatrickAction
    ) {
        EmojiTransformer emojiTransformer = unicodeCandidate -> {
            switch (fitzpatrickAction) {
                default:
                case PARSE:
                    if (unicodeCandidate.hasFitzpatrick()) {
                        return ":" +
                                unicodeCandidate.getEmoji().getAliases().get(0) +
                                "|" +
                                unicodeCandidate.getFitzpatrickType() +
                                ":";
                    }
                case REMOVE:
                    return ":" +
                            unicodeCandidate.getEmoji().getAliases().get(0) +
                            ":";
                case IGNORE:
                    return ":" +
                            unicodeCandidate.getEmoji().getAliases().get(0) +
                            ":" +
                            unicodeCandidate.getFitzpatrickUnicode();
            }
        };

        return parseFromUnicode(input, emojiTransformer);
    }

    @NotNull
    public static String replaceAllEmojis(@NotNull String str, @NotNull String replacementString) {
        EmojiParser.EmojiTransformer emojiTransformer = unicodeCandidate -> replacementString;

        return parseFromUnicode(str, emojiTransformer);
    }

    @NotNull
    public static String parseToUnicode(@NotNull String input) {
        StringBuilder sb = new StringBuilder(input.length());

        for (int last = 0; last < input.length(); last++) {
            AliasCandidate alias = getAliasAt(input, last);
            if (alias == null) {
                alias = getHtmlEncodedEmojiAt(input, last);
            }

            if (alias != null) {
                sb.append(alias.emoji.getUnicode());
                last = alias.endIndex;

                if (alias.fitzpatrick != null) {
                    sb.append(alias.fitzpatrick.unicode);
                }
            } else {
                sb.append(input.charAt(last));
            }
        }

        return sb.toString();
    }

    @Nullable
    protected static AliasCandidate getAliasAt(String input, int start) {
        if (input.length() < start + 2 || input.charAt(start) != ':') return null; // Aliases start with :
        int aliasEnd = input.indexOf(':', start + 2);  // Alias must be at least 1 char in length
        if (aliasEnd == -1) return null; // No alias end found

        int fitzpatrickStart = input.indexOf('|', start + 2);
        if (fitzpatrickStart != -1 && fitzpatrickStart < aliasEnd) {
            Emoji emoji = EmojiManager.getForAlias(input.substring(start, fitzpatrickStart));
            if (emoji == null) return null; // Not a valid alias
            if (!emoji.supportsFitzpatrick())
                return null; // Fitzpatrick was specified, but the emoji does not support it
            Fitzpatrick fitzpatrick = Fitzpatrick.fitzpatrickFromType(input.substring(fitzpatrickStart + 1, aliasEnd));
            return new AliasCandidate(emoji, fitzpatrick, start, aliasEnd);
        }

        Emoji emoji = EmojiManager.getForAlias(input.substring(start, aliasEnd));
        if (emoji == null) return null; // Not a valid alias
        return new AliasCandidate(emoji, null, start, aliasEnd);
    }

    @Nullable
    protected static AliasCandidate getHtmlEncodedEmojiAt(String input, int start) {
        if (input.length() < start + 4 || input.charAt(start) != '&' || input.charAt(start + 1) != '#') return null;

        Emoji longestEmoji = null;
        int longestCodePointEnd = -1;
        char[] chars = new char[EmojiManager.EMOJI_TRIE.maxDepth];
        int charsIndex = 0;
        int codePointStart = start;
        do {
            int codePointEnd = input.indexOf(';', codePointStart + 3);  // Code point must be at least 1 char in length
            if (codePointEnd == -1) break;

            try {
                int radix = input.charAt(codePointStart + 2) == 'x' ? 16 : 10;
                int codePoint = Integer.parseInt(input.substring(codePointStart + 2 + radix / 16, codePointEnd), radix);
                charsIndex += Character.toChars(codePoint, chars, charsIndex);
            } catch (IllegalArgumentException e) {
                break;
            }
            longestEmoji = EmojiManager.EMOJI_TRIE.getEmoji(chars, 0, charsIndex);
            longestCodePointEnd = codePointEnd;
            codePointStart = codePointEnd + 1;
        } while (input.length() > codePointStart + 4 &&
                input.charAt(codePointStart) == '&' &&
                input.charAt(codePointStart + 1) == '#' &&
                charsIndex < chars.length &&
                !EmojiManager.EMOJI_TRIE.isEmoji(chars, 0, charsIndex).impossibleMatch());

        if (longestEmoji == null) return null;
        return new AliasCandidate(longestEmoji, null, start, longestCodePointEnd);
    }

    @NotNull
    public static String parseToHtmlDecimal(@NotNull String input) {
        return parseToHtmlDecimal(input, FitzpatrickAction.PARSE);
    }

    @NotNull
    public static String parseToHtmlDecimal(
            String input,
            final FitzpatrickAction fitzpatrickAction
    ) {
        EmojiTransformer emojiTransformer = unicodeCandidate -> switch (fitzpatrickAction) {
            case PARSE, REMOVE -> unicodeCandidate.getEmoji().getHtmlDecimal();
            case IGNORE -> unicodeCandidate.getEmoji().getHtmlDecimal() +
                    unicodeCandidate.getFitzpatrickUnicode();
        };

        return parseFromUnicode(input, emojiTransformer);
    }

    @NotNull
    public static String parseToHtmlHexadecimal(@NotNull String input) {
        return parseToHtmlHexadecimal(input, FitzpatrickAction.PARSE);
    }

    @NotNull
    public static String parseToHtmlHexadecimal(
        @NotNull String input,
        @NotNull FitzpatrickAction fitzpatrickAction
    ) {
        EmojiTransformer emojiTransformer = unicodeCandidate -> switch (fitzpatrickAction) {
            case PARSE, REMOVE -> unicodeCandidate.getEmoji().getHtmlHexadecimal();
            case IGNORE -> unicodeCandidate.getEmoji().getHtmlHexadecimal() +
                    unicodeCandidate.getFitzpatrickUnicode();
        };

        return parseFromUnicode(input, emojiTransformer);
    }

    @NotNull
    public static String removeAllEmojis(@NotNull String str) {
        EmojiTransformer emojiTransformer = unicodeCandidate -> "";

        return parseFromUnicode(str, emojiTransformer);
    }

    @NotNull
    public static String removeEmojis(
            @NotNull String str,
            @NotNull Collection<? extends Emoji> emojisToRemove
    ) {
        EmojiTransformer emojiTransformer = unicodeCandidate -> {
            if (!emojisToRemove.contains(unicodeCandidate.getEmoji())) {
                return unicodeCandidate.getUnicode() +
                        unicodeCandidate.getFitzpatrickUnicode();
            }
            return "";
        };

        return parseFromUnicode(str, emojiTransformer);
    }

    /**
     * Removes all the emojis in a String except a provided set
     *
     * @param str
     *        the string to process
     * @param emojisToKeep
     *        the emojis to keep in this string
     *
     * @return the string without the emojis that were removed
     */
    @NotNull
    public static String removeAllEmojisExcept(
        @NotNull String str,
        @NotNull Collection<? extends Emoji> emojisToKeep
    ) {
        EmojiTransformer emojiTransformer = unicodeCandidate -> {
            if (emojisToKeep.contains(unicodeCandidate.getEmoji())) {
                return unicodeCandidate.getUnicode() +
                        unicodeCandidate.getFitzpatrickUnicode();
            }
            return "";
        };

        return parseFromUnicode(str, emojiTransformer);
    }


    /**
     * Detects all unicode emojis in input string and replaces them with the return value of transformer.transform()
     *
     * @param  input
     *         the string to process
     * @param  transformer
     *         emoji transformer to apply to each emoji
     *
     * @return input string with all emojis transformed
     */
    @NotNull
    public static String parseFromUnicode(
        @NotNull String input,
        @NotNull EmojiTransformer transformer
    ) {
        int prev = 0;
        StringBuilder sb = new StringBuilder(input.length());
        List<UnicodeCandidate> replacements = getUnicodeCandidates(input);
        for (UnicodeCandidate candidate : replacements) {
            sb.append(input, prev, candidate.getEmojiStartIndex());

            sb.append(transformer.transform(candidate));
            prev = candidate.getFitzpatrickEndIndex();
        }

        return sb.append(input.substring(prev)).toString();
    }

    /**
     * Parses all emoji by unicode in the given string.
     *
     * @param  input
     *         Input string to parse
     *
     * @return {@link List} of {@link String} unicode emoji
     */
    @NotNull
    public static List<String> extractEmojis(@NotNull String input) {
        List<UnicodeCandidate> emojis = getUnicodeCandidates(input);
        List<String> result = new ArrayList<>();
        for (UnicodeCandidate emoji : emojis) {
            if (emoji.getEmoji().supportsFitzpatrick()) {
                result.add(emoji.getUnicode() + emoji.getFitzpatrickUnicode());
            } else {
                result.add(emoji.getUnicode());
            }
        }
        return result;
    }


    /**
     * Generates a list UnicodeCandidates found in input string.
     * A UnicodeCandidate is created for every unicode emoticon found in input string,
     * additionally if Fitzpatrick modifier follows the emoji,
     * it is included in UnicodeCandidate.
     * Finally, it contains start and end index of unicode emoji itself
     * (WITHOUT Fitzpatrick modifier whether it is there or not!).
     *
     * @param  input
     *         String to find all unicode emojis in
     *
     * @return List of UnicodeCandidates for each unicode emote in text
     */
    @NotNull
    protected static List<UnicodeCandidate> getUnicodeCandidates(@NotNull String input) {
        char[] inputCharArray = input.toCharArray();
        List<UnicodeCandidate> candidates = new ArrayList<>();
        UnicodeCandidate next;
        for (int i = 0; (next = getNextUnicodeCandidate(inputCharArray, i)) != null; i = next.getFitzpatrickEndIndex()) {
            candidates.add(next);
        }

        return candidates;
    }

    /**
     * Finds the next UnicodeCandidate after a given starting index
     *
     * @param  chars
     *         char array to find UnicodeCandidate in
     * @param  start
     *         starting index for search
     *
     * @return the next UnicodeCandidate or null if no UnicodeCandidate is found after start index
     */
    @Nullable
    protected static UnicodeCandidate getNextUnicodeCandidate(char[] chars, int start) {
        for (int i = start; i < chars.length; i++) {
            int emojiEnd = getEmojiEndPos(chars, i);

            if (emojiEnd != -1) {
                String unicode = new String(chars, i, emojiEnd - i);
                Emoji emoji = EmojiManager.getByUnicode(unicode);
                String fitzpatrickString = (emojiEnd + 2 <= chars.length) ?
                        new String(chars, emojiEnd, 2) :
                        null;
                return new UnicodeCandidate(
                        emoji,
                        fitzpatrickString,
                        unicode,
                        i
                );
            }
        }

        return null;
    }


    /**
     * Returns end index of a unicode emoji if it is found in text starting at
     * index startPos, -1 if not found.
     *
     * <p>This returns the longest matching emoji, for example, in
     * {@code "\uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC66"}
     * it will find {@code alias:family_man_woman_boy}, NOT {@code alias:man}
     *
     * @param  text
     *         the current text where we are looking for an emoji
     * @param  startPos
     *         the position in the text where we should start looking for an emoji end
     *
     * @return the end index of the unicode emoji starting at startPos. -1 if not found
     */
    protected static int getEmojiEndPos(char[] text, int startPos) {
        int best = -1;
        for (int j = startPos + 1; j <= text.length; j++) {
            EmojiTrie.Matches status = EmojiManager.EMOJI_TRIE.isEmoji(text, startPos, j);

            if (status.exactMatch()) {
                best = j;
            } else if (status.impossibleMatch()) {
                return best;
            }
        }

        return best;
    }


    /**
     * Enum used to indicate what should be done when a Fitzpatrick modifier is found.
     */
    public enum FitzpatrickAction {
        /**
         * Tries to match the Fitzpatrick modifier with the previous emoji
         */
        PARSE,

        /**
         * Removes the Fitzpatrick modifier from the string
         */
        REMOVE,

        /**
         * Ignores the Fitzpatrick modifier (it will stay in the string)
         */
        IGNORE
    }


    @FunctionalInterface
    public interface EmojiTransformer {
        String transform(UnicodeCandidate unicodeCandidate);
    }

    public static class UnicodeCandidate {
        private final Emoji emoji;
        private final Fitzpatrick fitzpatrick;
        private final int startIndex;
        private final boolean hasVariation;
        private final String unicode;

        private UnicodeCandidate(Emoji emoji, String fitzpatrick, String unicode, int startIndex) {
            this.emoji = emoji;
            this.fitzpatrick = Fitzpatrick.fitzpatrickFromUnicode(fitzpatrick);
            this.hasVariation = unicode.contains("\uFE0F");
            this.startIndex = startIndex;
            this.unicode = unicode;
        }

        public String getUnicode() {
            return unicode;
        }

        public Emoji getEmoji() {
            return emoji;
        }

        public boolean hasFitzpatrick() {
            return getFitzpatrick() != null;
        }

        public boolean hasVariation() {
            return hasVariation;
        }

        public Fitzpatrick getFitzpatrick() {
            return fitzpatrick;
        }

        public String getFitzpatrickType() {
            return hasFitzpatrick() ? fitzpatrick.name().toLowerCase() : "";
        }

        public String getFitzpatrickUnicode() {
            return hasFitzpatrick() ? fitzpatrick.unicode : "";
        }

        public int getEmojiStartIndex() {
            return startIndex;
        }

        public int getEmojiEndIndex() {
            return startIndex + emoji.getUnicode().length();
        }

        public int getFitzpatrickEndIndex() {
            return getEmojiEndIndex() + (fitzpatrick != null ? 2 : 0);
        }
    }

    protected static class AliasCandidate {
        public final Emoji emoji;
        public final Fitzpatrick fitzpatrick;
        public final int startIndex;
        public final int endIndex;

        private AliasCandidate(Emoji emoji, Fitzpatrick fitzpatrick, int startIndex, int endIndex) {
            this.emoji = emoji;
            this.fitzpatrick = fitzpatrick;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }
    }
}
