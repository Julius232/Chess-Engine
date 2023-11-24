package julius.game.chessengine.ai;

import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class OpeningBook {
    private static final String OPENINGS_FILE_PATH = "/opening/openings.txt";
    private final Map<Long, List<Integer>> openings = new HashMap<>();

    public OpeningBook() {
        loadOpenings();
    }

    private void loadOpenings() {
        try (InputStream is = getClass().getResourceAsStream(OPENINGS_FILE_PATH);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    int move = Integer.parseInt(parts[0].trim());
                    long boardStateHash = Long.parseLong(parts[1].trim());
                    openings.computeIfAbsent(boardStateHash, k -> new ArrayList<>()).add(move);
                }
            }
        } catch (IOException | NullPointerException e) {
            // Handle exceptions or log errors
        }
    }

    public void addOpening(int move, long boardStateHash) {
        List<Integer> existingMoves = openings.getOrDefault(boardStateHash, Collections.emptyList());
        if (!existingMoves.contains(move)) {
            openings.computeIfAbsent(boardStateHash, k -> new ArrayList<>()).add(move);
            writeOpening(move, boardStateHash); // Writes to the file
        }
    }


    public void writeOpening(int move, long boardStateHash) {
        // This method writes a new opening move to the file
        File file = new File("src/main/resources" + OPENINGS_FILE_PATH);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            writer.write(move + "," + boardStateHash + "\n");
        } catch (IOException e) {
            // Handle exceptions or log errors
        }
    }

    public List<Integer> getMovesForBoardStateHash(long boardStateHash) {
        return openings.getOrDefault(boardStateHash, Collections.emptyList());
    }

    public int getRandomMoveForBoardStateHash(long boardStateHash) {
        List<Integer> moves = getMovesForBoardStateHash(boardStateHash);

        if (moves.isEmpty()) {
            return -1; // or a default move, depending on how you want to handle this scenario
        }
        Random random = new Random();
        return moves.get(random.nextInt(moves.size()));
    }
}