package julius.game.chessengine.pgn;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PgnParser {

    private static final Pattern TAG_PAIR_PATTERN = Pattern.compile("\\[([A-Za-z0-9]+) \"(.*)\"\\]");
    private static final Pattern MOVE_TEXT_PATTERN = Pattern.compile("\\d+\\.\\s*(\\S+)(?:\\s+(\\S+))?");

    public Game parsePgnFile(String filePath) throws IOException {
        Game game = new Game();
        List<String> moves = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean readingMoves = false;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("[")) {
                    Matcher matcher = TAG_PAIR_PATTERN.matcher(line);
                    if (matcher.find()) {
                        game.addTagPair(matcher.group(1), matcher.group(2));
                    }
                } else if (!line.isEmpty()) {
                    readingMoves = true;
                }

                if (readingMoves) {
                    Matcher matcher = MOVE_TEXT_PATTERN.matcher(line);
                    while (matcher.find()) {
                        if (matcher.group(1) != null) {
                            moves.add(matcher.group(1));
                        }
                        if (matcher.group(2) != null) {
                            moves.add(matcher.group(2));
                        }
                    }
                }
            }
        }

        game.setMoves(moves);
        return game;
    }

    public static class Game {
        private final List<String> tags = new ArrayList<>();
        private final List<String> moves = new ArrayList<>();

        public void addTagPair(String tag, String value) {
            tags.add(tag + ": " + value);
        }

        public void setMoves(List<String> moves) {
            this.moves.addAll(moves);
        }

        public List<String> getTags() {
            return tags;
        }

        public List<String> getMoves() {
            return moves;
        }

        @Override
        public String toString() {
            return "Game{" +
                    "tags=" + tags +
                    ", moves=" + moves +
                    '}';
        }
    }
}
