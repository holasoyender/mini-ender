package emoji;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class EmojiTrie {
    final int maxDepth;
    private final Node root = new Node();

    public EmojiTrie(Collection<Emoji> emojis) {
        int maxDepth = 0;
        for (Emoji emoji : emojis) {
            Node tree = root;
            char[] chars = emoji.getUnicode().toCharArray();
            maxDepth = addEmoji(maxDepth, emoji, tree, chars);
            // Add emoji without variation selector as well
            if (emoji.supportsVariation()) {
                chars = emoji.getTrimmedUnicode().toCharArray();
                maxDepth = addEmoji(maxDepth, emoji, tree, chars);
            }
        }
        this.maxDepth = maxDepth;
    }

    private int addEmoji(int maxDepth, Emoji emoji, Node tree, char[] chars) {
        maxDepth = Math.max(maxDepth, chars.length);
        for (char c : chars) {
            if (!tree.hasChild(c)) {
                tree.addChild(c);
            }
            tree = tree.getChild(c);
        }
        tree.setEmoji(emoji);
        return maxDepth;
    }


    /**
     * Checks if sequence of chars contain an emoji.
     *
     * @param sequence
     *        Sequence of char that may contain emoji in full or partially.
     *
     * @return
     * <ul>
     *  <li>{@link Matches#EXACTLY} if char sequence in its entirety is an emoji</li>
     *  <li>{@link Matches#POSSIBLY} if char sequence matches prefix of an emoji</li>
     *  <li>{@link Matches#IMPOSSIBLE} if char sequence matches no emoji or prefix of an emoji</li>
     * </ul>
     */
    @NotNull
    public Matches isEmoji(char[] sequence) {
        return isEmoji(sequence, 0, sequence.length);
    }

    /**
     * Checks if the sequence of chars within the given bound indices contain an emoji.
     *
     * @param sequence
     *        Sequence of char that may contain emoji in full or partially.
     * @param start
     *        The starting index
     * @param end
     *        The end index (exclusive)
     *
     * @throws ArrayIndexOutOfBoundsException
     *         If the provided range is invalid
     *
     * @return
     * <ul>
     *  <li>{@link Matches#EXACTLY} if char sequence in its entirety is an emoji</li>
     *  <li>{@link Matches#POSSIBLY} if char sequence matches prefix of an emoji</li>
     *  <li>{@link Matches#IMPOSSIBLE} if char sequence matches no emoji or prefix of an emoji</li>
     * </ul>
     *
     * @see #isEmoji(char[])
     */
    @NotNull
    public Matches isEmoji(char[] sequence, int start, int end) {
        if (start < 0 || start > end || end > sequence.length) {
            throw new ArrayIndexOutOfBoundsException(
                    "start " + start + ", end " + end + ", length " + sequence.length);
        }

        Node tree = root;
        for (int i = start; i < end; i++) {
            if (!tree.hasChild(sequence[i])) {
                return Matches.IMPOSSIBLE;
            }
            tree = tree.getChild(sequence[i]);
        }

        return tree.isEndOfEmoji() ? Matches.EXACTLY : Matches.POSSIBLY;
    }

    /**
     * Finds Emoji instance from emoji unicode
     *
     * @param  unicode
     *         unicode of emoji to get
     *
     * @return Emoji instance if unicode matches and emoji, null otherwise.
     */
    @Nullable
    public Emoji getEmoji(@NotNull String unicode) {
        return getEmoji(unicode.toCharArray(), 0, unicode.length());
    }

    protected Emoji getEmoji(@NotNull char[] sequence, int start, int end) {
        if (start < 0 || start > end || end > sequence.length) {
            throw new ArrayIndexOutOfBoundsException(
                    "start " + start + ", end " + end + ", length " + sequence.length);
        }

        Node tree = root;
        for (int i = 0; i < end; i++) {
            if (!tree.hasChild(sequence[i])) {
                return null;
            }
            tree = tree.getChild(sequence[i]);
        }
        return tree.getEmoji();
    }

    public enum Matches {
        EXACTLY, POSSIBLY, IMPOSSIBLE;

        public boolean exactMatch() {
            return this == EXACTLY;
        }

        public boolean impossibleMatch() {
            return this == IMPOSSIBLE;
        }
    }

    private static class Node {
        private final Map<Character, Node> children = new HashMap<>();
        private Emoji emoji;

        private Emoji getEmoji() {
            return emoji;
        }

        private void setEmoji(Emoji emoji) {
            this.emoji = emoji;
        }

        private boolean hasChild(char child) {
            return children.containsKey(child);
        }

        private void addChild(char child) {
            children.put(child, new Node());
        }

        private Node getChild(char child) {
            return children.get(child);
        }

        private boolean isEndOfEmoji() {
            return emoji != null;
        }
    }
}
