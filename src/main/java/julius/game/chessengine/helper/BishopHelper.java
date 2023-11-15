package julius.game.chessengine.helper;

import lombok.extern.log4j.Log4j2;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Log4j2
public class BishopHelper {
    public static final int[] BISHOP_POSITIONAL_VALUES = {
            -20, -10, -10, -10, -10, -10, -10, -20,
            -10,   5,   0,   0,   0,   0,   5, -10,
            -10,   5,  10,  10,  10,  10,   5, -10,
            -10,   5,  10,  10,  10,  10,   5, -10,
            -10,   5,  10,  10,  10,  10,   5, -10,
            -10,   5,  10,  10,  10,  10,   5, -10,
            -10,   5,   0,   0,   0,   0,   5, -10,
            -20, -10, -10, -10, -10, -10, -10, -20,
    };

    int[][] directions = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}}; // Bishop directions

    public long[] bishopMasks = new long[64];
    public long[][] bishopAttacks = new long[64][];
    public long[] bishopMagics = new long[64]; // To store the found magic numbers

    boolean[] squareMagicFound = new boolean[64];


    public BishopHelper() {
        loadMagicNumbers();
        // First, generate and store occupancy masks
        for (int square = 0; square < 64; square++) {
            bishopMasks[square] = generateOccupancyMask(square);
        }
        initializeBishopAttacks();
    }

    public void findMagicNumbers() {


        // Then, find magic numbers
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("src/main/resources/magic/bishop_magic_numbers.txt", true))) {

            boolean allFound = false;
            while (!allFound) {
                long magicCandidate = randomMagicNumber();
                allFound = true; // Assume all found, and set to false if any are missing

                for (int square = 0; square < 64; square++) {
                    if (squareMagicFound[square]) {
                        continue; // Skip if magic number already found for this square
                    }

                    long mask = bishopMasks[square]; // Use the pre-stored mask
                    List<Long> occupancies = generateAllOccupancies(mask);

                    Map<Integer, Long> indexToOccupancy = new HashMap<>();
                    boolean isMagic = true;

                    for (long occupancy : occupancies) {
                        int index = transform(occupancy, magicCandidate, mask);

                        if (indexToOccupancy.containsKey(index) &&
                                indexToOccupancy.get(index) != occupancy) {
                            isMagic = false;
                            break;
                        }

                        indexToOccupancy.put(index, occupancy);
                    }

                    if (!isMagic) {
                        allFound = false; // If any magic number is not found, set allFound to false
                    } else {
                        squareMagicFound[square] = true;
                        bishopMagics[square] = magicCandidate;
                        squareMagicFound[square] = true;
                        log.info("Magic number found for square " + square + ": " + magicCandidate);
                        writer.write(square + ":" + magicCandidate + ":" + mask + "\n");
                        writer.flush(); // Ensure it's written immediately
                    }
                }
            }
        } catch (IOException e) {
            log.error("Error writing to file", e);
        }

        // Finally, initialize bishop attacks using the found magic numbers
        initializeBishopAttacks();
    }

    public void initializeBishopAttacks() {
        for (int square = 0; square < 64; square++) {
            long mask = bishopMasks[square];
            List<Long> occupancies = generateAllOccupancies(mask);
            bishopAttacks[square] = new long[occupancies.size()];

            for (long occupancy : occupancies) {
                int index = transform(occupancy, bishopMagics[square], mask);
                bishopAttacks[square][index] = calculateBishopMoves(square, occupancy);
            }
        }
    }

    public List<Long> generateAllOccupancies(long mask) {
        List<Long> occupancies = new ArrayList<>();
        int numberOfBits = Long.bitCount(mask);

        // Generate all possible combinations of bits within the mask
        for (int i = 0; i < (1 << numberOfBits); i++) {
            long occupancy = 0L;
            int bitIndex = 0;
            for (int j = 0; j < 64; j++) {
                if ((mask & (1L << j)) != 0) {
                    if ((i & (1 << bitIndex)) != 0) {
                        occupancy |= (1L << j);
                    }
                    bitIndex++;
                }
            }
            occupancies.add(occupancy);
        }
        return occupancies;
    }

    public long calculateBishopMoves(int square, long occupancy) {
        long moves = 0L;
        int row = square / 8, col = square % 8;

        for (int[] dir : directions) {
            int r = row + dir[0], c = col + dir[1];
            while (r >= 0 && r < 8 && c >= 0 && c < 8) {
                moves |= (1L << (r * 8 + c));
                if ((occupancy & (1L << (r * 8 + c))) != 0) {
                    break; // blocked by another piece
                }
                r += dir[0];
                c += dir[1];
            }
        }
        return moves;
    }

    public long generateOccupancyMask(int square) {
        long mask = 0L;
        int row = square / 8, col = square % 8;

        for (int[] direction : directions) {
            int r = row + direction[0], c = col + direction[1];
            while (r >= 0 && r < 8 && c >= 0 && c < 8) {
                mask |= (1L << (r * 8 + c));
                r += direction[0];
                c += direction[1];
            }
        }
        return mask;
    }

    private long randomMagicNumber() {
        return ThreadLocalRandom.current().nextLong() & ThreadLocalRandom.current().nextLong() & ThreadLocalRandom.current().nextLong();
    }

    public int transform(long occupancy, long magicNumber, long mask) {
        return (int)((occupancy * magicNumber) >>> (64 - Long.bitCount(mask)));
    }

    public void loadMagicNumbers() {
        try (BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/magic/bishop_magic_numbers.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 3) {
                    int square = Integer.parseInt(parts[0]);
                    squareMagicFound[square] = true;

                    long magicNumber = Long.parseLong(parts[1]);
                    bishopMagics[square] = magicNumber;
                }
            }
        } catch (IOException e) {
            log.error("Error reading magic numbers from file", e);
        }
    }

}
