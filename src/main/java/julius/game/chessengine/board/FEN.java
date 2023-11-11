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
}
