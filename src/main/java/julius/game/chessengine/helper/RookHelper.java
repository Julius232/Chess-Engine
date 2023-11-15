package julius.game.chessengine.helper;

import lombok.extern.log4j.Log4j2;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Log4j2
public class RookHelper {

    int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}}; // Rook directions

    public long[] rookMasks = new long[64];
    public long[][] rookAttacks = new long[64][];
    public long[] rookMagics = new long[64]; // To store the found magic numbers

    boolean[] squareMagicFound = new boolean[64];

    public RookHelper() {
        loadMagicNumbers();
        // First, generate and store occupancy masks
        for (int square = 0; square < 64; square++) {
            rookMasks[square] = generateOccupancyMask(square);
        }
        initializeRookAttacks();
    }



    public void findMagicNumbersParallel() {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        ConcurrentHashMap<Integer, Long> magicNumbers = new ConcurrentHashMap<>();

        for (int square = 0; square < 64; square++) {
            final int finalSquare = square;
            executor.submit(() -> findMagicNumberForSquare(finalSquare, magicNumbers));
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.MINUTES)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }

        writeMagicNumbersToFile(magicNumbers);
    }

    private void findMagicNumberForSquare(int square, ConcurrentHashMap<Integer, Long> magicNumbers) {
        if (squareMagicFound[square]) {
            return; // Early return if magic number already found
        }
        log.info("Searching magic number for square: " + square);

        long mask = rookMasks[square];
        List<Long> occupancies = generateAllOccupancies(mask);

        while (true) {
            long magicCandidate = randomMagicNumber();
            Map<Integer, Long> indexToOccupancy = new HashMap<>();
            boolean isMagic = true;

            for (long occupancy : occupancies) {
                int index = transform(occupancy, magicCandidate, mask);
                Long existingOccupancy = indexToOccupancy.get(index);

                if (existingOccupancy != null && existingOccupancy != occupancy) {
                    isMagic = false;
                    break;
                }

                indexToOccupancy.put(index, occupancy);
            }

            if (isMagic) {
                squareMagicFound[square] = true;
                rookMagics[square] = magicCandidate;
                log.info("Magic number found for square " + square + ": " + magicCandidate);
                magicNumbers.put(square, magicCandidate);
                break;
            }
        }
    }


    private void writeMagicNumbersToFile(ConcurrentHashMap<Integer, Long> magicNumbers) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("src/main/resources/magic/rook_magic_numbers.txt", true))) {
            for (Map.Entry<Integer, Long> entry : magicNumbers.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue() + "\n");
            }
        } catch (IOException e) {
            log.error("Error writing magic numbers to file", e);
        }
    }


    // ... [Rest of the class remains the same]













    public void findMagicNumbers() {
        // Then, find magic numbers
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("src/main/resources/magic/rook_magic_numbers.txt", true))) { // Append mode set to true

            boolean allFound = false;
            while (!allFound) {
                long magicCandidate = randomMagicNumber();
                allFound = true; // Assume all found, and set to false if any are missing

                for (int square = 0; square < 64; square++) {
                    if (squareMagicFound[square]) {
                        continue; // Skip if magic number already found for this square
                    }

                    long mask = rookMasks[square]; // Use the pre-stored mask
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
                        rookMagics[square] = magicCandidate;
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

        // Finally, initialize rook attacks using the found magic numbers
        initializeRookAttacks();
    }


    public void initializeRookAttacks() {
        for (int square = 0; square < 64; square++) {
            long mask = rookMasks[square];
            List<Long> occupancies = generateAllOccupancies(mask);
            rookAttacks[square] = new long[occupancies.size()];

            for (int i = 0; i < occupancies.size(); i++) {
                long occupancy = occupancies.get(i);
                int index = transform(occupancy, rookMagics[square], mask);
                rookAttacks[square][index] = calculateRookMoves(square, occupancy);
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

    public long calculateRookMoves(int square, long occupancy) {
        long moves = 0L;
        int row = square / 8, col = square % 8;

        for (int[] dir : directions) {
            int r = row + dir[0], c = col + dir[1]; // Move one step in the direction initially

            while (r >= 0 && r < 8 && c >= 0 && c < 8) {
                long positionBit = 1L << (r * 8 + c); // Calculate the bit for the current position

                moves |= positionBit; // Add this square to the moves
                if ((occupancy & positionBit) != 0) {
                    break; // Stop if there's a piece in the way
                }


                r += dir[0]; // Move in the row direction
                c += dir[1]; // Move in the column direction
            }
        }

        return moves;
    }




    public long generateOccupancyMask(int square) {
        long mask = 0L;
        int row = square / 8, col = square % 8;

        // Include all squares the rook can potentially move to, including edges
        for (int i = 1; i < 8; i++) {
            if (col - i >= 0) mask |= (1L << (square - i));      // Left
            if (col + i <= 7) mask |= (1L << (square + i));     // Right
            if (row - i >= 0) mask |= (1L << (square - 8 * i)); // Up
            if (row + i <= 7) mask |= (1L << (square + 8 * i)); // Down
        }
        return mask;
    }
    private long randomMagicNumber() {
        return ThreadLocalRandom.current().nextLong();
    }

    public int transform(long occupancy, long magicNumber, long mask) {
        return (int)((occupancy * magicNumber) >>> (64 - Long.bitCount(mask)));
    }

    public void loadMagicNumbers() {
        try (BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/magic/rook_magic_numbers.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    int square = Integer.parseInt(parts[0]);
                    squareMagicFound[square] = true;

                    long magicNumber = Long.parseLong(parts[1]);
                    rookMagics[square] = magicNumber;
                }
            }
        } catch (IOException e) {
            log.error("Error reading magic numbers from file", e);
        }
    }

}
