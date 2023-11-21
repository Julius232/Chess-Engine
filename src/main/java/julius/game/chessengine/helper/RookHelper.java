package julius.game.chessengine.helper;

import lombok.extern.log4j.Log4j2;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Log4j2
public class RookHelper {

    int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}}; // Rook directions

    public long[] rookMasks = new long[64];
    public long[][] rookAttacks = new long[64][];
    public long[] rookMagics = new long[64]; // To store the found magic numbers

    boolean[] squareMagicFound = new boolean[64];

    private static RookHelper instance = null;

    public RookHelper() {
        loadMagicNumbers();
        // First, generate and store occupancy masks
        for (int square = 0; square < 64; square++) {
            rookMasks[square] = generateOccupancyMask(square);
        }
        initializeRookAttacks();
    }

    public static RookHelper getInstance() {
        if (instance == null) {
            instance = new RookHelper();
        }
        return instance;
    }


    public void findMagicNumbersParallel() {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        ConcurrentHashMap<Integer, Long> magicNumbers = new ConcurrentHashMap<>();

        // Map to store square indices and their corresponding index counts
        Map<Integer, Integer> squareIndexCounts = new HashMap<>();

        int total = 0;
        // Calculate index counts for each square
        for (int square = 0; square < 64; square++) {
            long mask = rookMasks[square];
            int indexCount = calculateIndexCount(rookMagics[square], square, mask);
            squareIndexCounts.put(square, indexCount);
            total += indexCount;
        }
        log.info(" --- Rook attacks take up a size of {} --- ", total);

        // Create a list of square indices sorted by index counts in descending order
        List<Integer> squares = squareIndexCounts.entrySet().stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .toList();

        for (int square : squares) {
            Set<Long> uniqueAttacks = new HashSet<>();
            boolean duplicatesFound = false;

            for (long attacks : rookAttacks[square]) {
                if (!uniqueAttacks.add(attacks)) { // add() returns false if the item was already in the set
                    duplicatesFound = true;
                    break;
                }
            }

            if (duplicatesFound) {
                // Submit the task only if duplicates are found, indicating a need for optimization
                executor.submit(() -> findMagicNumberForSquare(square, magicNumbers));
            }
            else {
                log.info("Rook square {} is fully optimized size {}", square, squareIndexCounts.get(square));
            }
        }

        // Shutdown executor and wait for termination
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.MINUTES)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }

        writeMagicNumbersToFile(magicNumbers);
    }

    private void findMagicNumberForSquare(int square, ConcurrentHashMap<Integer, Long> magicNumbers) {
        long mask = rookMasks[square];
        Set<Long> occupancies = generateAllOccupancies(mask);
        int minIndices = calculateIndexCount(rookMagics[square], square, mask);
        log.info("Rook Square: {}, has a size of {}", square, minIndices);

        while (true) {
            long magicCandidate = randomMagicNumber();
            Map<Integer, Long> indexToOccupancy = new HashMap<>();
            boolean collision = false;

            for (long occupancy : occupancies) {
                int index = transform(occupancy, magicCandidate, mask);
                Long existingOccupancy = indexToOccupancy.get(index);

                if (existingOccupancy != null && calculateRookMoves(square, occupancy) != calculateRookMoves(square, existingOccupancy)) {
                    collision = true;
                    break;
                }

                indexToOccupancy.put(index, occupancy);
            }

            if (!collision && indexToOccupancy.size() < minIndices) {
                minIndices = indexToOccupancy.size(); // Update to the new lower value
                rookMagics[square] = magicCandidate; // Update the magic number
                squareMagicFound[square] = true;
                log.info("Rook Optimized magic number found for square " + square + ": " + magicCandidate);
                magicNumbers.put(square, magicCandidate);
                break;
            }
        }
    }

    private int calculateIndexCount(long magicNumber, int square, long mask) {
        Set<Long> occupancies = generateAllOccupancies(mask);
        Set<Integer> indices = new HashSet<>();

        for (long occupancy : occupancies) {
            int index = transform(occupancy, magicNumber, mask);
            indices.add(index);
        }
        return indices.size();
    }

    private void writeMagicNumbersToFile(ConcurrentHashMap<Integer, Long> magicNumbers) {
        File file = new File("src/main/resources/magic/rook_magic_numbers.txt");
        Map<Integer, Long> existingNumbers = new HashMap<>();

        // Load existing magic numbers from the file
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(":");
                    if (parts.length == 2) {
                        int square = Integer.parseInt(parts[0]);
                        long magicNumber = Long.parseLong(parts[1]);
                        existingNumbers.put(square, magicNumber);
                    }
                }
            } catch (IOException e) {
                log.error("Error reading magic numbers from file", e);
            }
        }

        // Update with new magic numbers
        existingNumbers.putAll(magicNumbers);

        // Write updated magic numbers back to the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) { // false to overwrite the file
            for (Map.Entry<Integer, Long> entry : existingNumbers.entrySet()) {
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

                for (int square = 43; square < 44; square++) {
                    if (squareMagicFound[square]) {
                        continue; // Skip if magic number already found for this square
                    }

                    long mask = rookMasks[square]; // Use the pre-stored mask
                    Set<Long> occupancies = generateAllOccupancies(mask);

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
            Set<Long> occupancies = generateAllOccupancies(mask);
            rookAttacks[square] = new long[occupancies.size()];

            for (long occupancy : occupancies) {
                int index = transform(occupancy, rookMagics[square], mask);
                rookAttacks[square][index] = calculateRookMoves(square, occupancy);
            }
        }
    }

    public Set<Long> generateAllOccupancies(long mask) {
        Set<Long> occupancies = new HashSet<>();
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

        // Include squares the rook can move to, excluding the edges and the rook's current square
        for (int i = 1; i < 7; i++) {
            if (col - i > 0) mask |= (1L << (square - i));      // Left
            if (col + i < 7) mask |= (1L << (square + i));     // Right
            if (row - i > 0) mask |= (1L << (square - 8 * i)); // Up
            if (row + i < 7) mask |= (1L << (square + 8 * i)); // Down
        }
        return mask;
    }

    private long randomMagicNumber() {
        return ThreadLocalRandom.current().nextLong();
    }

    public int transform(long occupancy, long magicNumber, long mask) {
        return (int) ((occupancy * magicNumber) >>> (64 - Long.bitCount(mask)));
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

    public long calculateMovesUsingRookMagic(int square, long occupancy) {
        // Calculate the index using the magic number
        int index = this.transform(occupancy, this.rookMagics[square], this.rookMasks[square]);
        // Retrieve the moves from the rookAttacks table
        return this.rookAttacks[square][index];
    }

}
