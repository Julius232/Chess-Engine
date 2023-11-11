package julius.game.chessengine.board;

import julius.game.chessengine.figures.PieceType;
import julius.game.chessengine.utils.Color;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class FEN {
    private final String renderBoard;

    public static FEN translateBoardToFEN(BitBoard board) {
        StringBuilder fenBuilder = new StringBuilder();
        for (int rank = 8; rank >= 1; rank--) {
            int emptyCount = 0;
            for (char file = 'a'; file <= 'h'; file++) {
                Position position = new Position(file, rank);
                PieceType pieceType = board.getPieceTypeAtPosition(position);
                Color color = board.getPieceColorAtPosition(position);

                if (pieceType != null) {
                    if (emptyCount > 0) {
                        fenBuilder.append(emptyCount);
                        emptyCount = 0;
                    }
                    char fenChar = getFenCharacter(pieceType, color);
                    fenBuilder.append(fenChar);
                } else {
                    emptyCount++;
                }
            }
            if (emptyCount > 0) {
                fenBuilder.append(emptyCount);
            }
            if (rank > 1) {
                fenBuilder.append('/');
            }
        }
        // You would add the active color, castling availability, en passant target square,
        // halfmove clock, and fullmove number after this
        return new FEN(fenBuilder.toString());
    }

    private static char getFenCharacter(PieceType pieceType, Color color) {
        char fenChar = pieceType.getNotation();
        if (color == Color.BLACK) {
            fenChar = Character.toLowerCase(fenChar);
        }
        return fenChar;
    }

    public static BitBoard translateFENtoBitBoard(String fen) {
        String[] parts = fen.split(" ");
        String[] ranks = parts[0].split("/");

        long whitePawns = 0, blackPawns = 0, whiteKnights = 0, blackKnights = 0, whiteBishops = 0, blackBishops = 0;
        long whiteRooks = 0, blackRooks = 0, whiteQueens = 0, blackQueens = 0, whiteKing = 0, blackKing = 0;
        long whitePieces = 0, blackPieces = 0, allPieces = 0;

        for (int i = 0; i < ranks.length; i++) {
            int file = 0;
            for (char c : ranks[i].toCharArray()) {
                if (Character.isDigit(c)) {
                    file += Character.getNumericValue(c);
                } else {
                    int rank = 8 - i;
                    Position position = new Position((char) ('a' + file), rank);
                    long bit = 1L << positionToBitIndex(position);

                    switch (Character.toLowerCase(c)) {
                        case 'p': if (c == 'p') blackPawns |= bit; else whitePawns |= bit; break;
                        case 'n': if (c == 'n') blackKnights |= bit; else whiteKnights |= bit; break;
                        case 'b': if (c == 'b') blackBishops |= bit; else whiteBishops |= bit; break;
                        case 'r': if (c == 'r') blackRooks |= bit; else whiteRooks |= bit; break;
                        case 'q': if (c == 'q') blackQueens |= bit; else whiteQueens |= bit; break;
                        case 'k': if (c == 'k') blackKing |= bit; else whiteKing |= bit; break;
                    }

                    if (Character.isUpperCase(c)) whitePieces |= bit;
                    else blackPieces |= bit;
                    allPieces |= bit;

                    file++;
                }
            }
        }

        boolean whitesTurn = parts[1].equals("w");
        // You'll need to parse the other FEN parts like castling availability, en passant, etc.

        // Set castling and en passant flags...
        boolean whiteKingMoved = !parts[2].contains("K");
        boolean blackKingMoved = !parts[2].contains("k");
        boolean whiteRookA1Moved = !parts[2].contains("Q");
        boolean whiteRookH1Moved = !parts[2].contains("K");
        boolean blackRookA8Moved = !parts[2].contains("q");
        boolean blackRookH8Moved = !parts[2].contains("k");

        Position lastMoveDoubleStepPawnPosition = null;
        if (!parts[3].equals("-")) {
            lastMoveDoubleStepPawnPosition = new Position(parts[3].charAt(0), Character.getNumericValue(parts[3].charAt(1)));
        }

        return new BitBoard(whitesTurn, whitePawns, blackPawns, whiteKnights, blackKnights, whiteBishops, blackBishops, whiteRooks, blackRooks, whiteQueens, blackQueens, whiteKing, blackKing, whitePieces, blackPieces, allPieces, lastMoveDoubleStepPawnPosition, whiteKingMoved, blackKingMoved, whiteRookA1Moved, whiteRookH1Moved, blackRookA8Moved, blackRookH8Moved);
    }

    private static int positionToBitIndex(Position position) {
        // Assuming position file 'a' to 'h' and rank 1 to 8
        int file = position.getX() - 'a';
        int rank = position.getY() - 1;
        return 8 * rank + file;
    }

}
