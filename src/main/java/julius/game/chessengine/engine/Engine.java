package julius.game.chessengine.engine;

import julius.game.chessengine.board.BitBoard;
import julius.game.chessengine.board.FEN;
import julius.game.chessengine.board.Move;
import julius.game.chessengine.board.Position;
import julius.game.chessengine.figures.PieceType;
import julius.game.chessengine.utils.Color;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static julius.game.chessengine.helper.BitHelper.bitIndex;
import static julius.game.chessengine.helper.BitHelper.isValidBoardPosition;
import static julius.game.chessengine.helper.KnightHelper.knightMoves;

@Data
@Service
@Log4j2
public class Engine {


    private LinkedList<Move> moves = new LinkedList<>();
    private BitBoard bitBoard = new BitBoard();
    private GameState gameState = new GameState();

    public void startNewGame() {
        bitBoard = new BitBoard();
        gameState = new GameState();
        // Set up the initial position of the pieces on the bitboard
        bitBoard.setInitialPosition();
    }

    public List<Move> getAllLegalMoves() {
        return bitBoard.getAllCurrentPossibleMoves()
                .stream()
                .filter(move -> isLegalMove(bitBoard, move))
                .collect(Collectors.toList());
    }

    public List<Move> getAllLegalMovesForBitBoard(BitBoard b) {
        return b.getAllCurrentPossibleMoves()
                .stream()
                .filter(move -> isLegalMove(b, move))
                .collect(Collectors.toList());
    }

    // Each of these methods would need to be implemented to handle the specific move generation for each piece type.
    public List<Move> getMovesFromPosition(Position fromPosition) {
        return getAllLegalMoves().stream()
                .filter(move -> move.getFrom().equals(fromPosition))
                .collect(Collectors.toList());
    }

    public GameState moveRandomFigure(Color color) {
        // Now, the color parameter is used to determine which moves to generate
        List<Move> moves = getAllLegalMoves();

        if (moves.isEmpty()) {
            throw new RuntimeException("No moves possible for " + color);
        }

        Random rand = new Random();
        Move randomMove = moves.get(rand.nextInt(moves.size()));

        if (randomMove.isEnPassantMove()) {
            // Clear the captured pawn from its position for en passant
            bitBoard.clearSquare(bitIndex(randomMove.getTo().getX(), randomMove.getFrom().getY()), Color.getOpponentColor(color));
        }

        // Execute the move on the bitboard
        bitBoard.performMove(randomMove);

        // Update the game state
        updateGameState();

        return gameState;
    }


    public GameState moveFigure(BitBoard bitBoard, Position fromPosition, Position toPosition) {
        // Determine the piece type and color from the bitboard based on the 'from' position
        PieceType pieceType = bitBoard.getPieceTypeAtPosition(fromPosition);
        Color color = bitBoard.getPieceColorAtPosition(fromPosition);

        if (pieceType == null || color == null) {
            throw new IllegalStateException("No piece at the starting position");
        }

        // Check if it's the correct player's turn
        Color pieceColor = bitBoard.getPieceColorAtPosition(fromPosition);
        if ((pieceColor == Color.WHITE && !bitBoard.whitesTurn) || (pieceColor == Color.BLACK && bitBoard.whitesTurn)) {
            bitBoard.logBoard();
            throw new IllegalStateException("It's not " + pieceColor + "'s turn");
        }

        Move move = getAllLegalMoves().stream()
                .filter(m -> m.getFrom().equals(fromPosition) && m.getTo().equals(toPosition))
                .findAny().orElseThrow(() -> new IllegalStateException("Move not found"));


        // Perform the move on the bitboard
        bitBoard.performMove(move);

        moves.add(move);
        // Update the game state
        updateGameState();

        return gameState;
    }


    private boolean isLegalMove(BitBoard bitBoard, Move move) {
        // Check if the move is within bounds of the board
        if (!isMoveOnBoard(move)) {
            return false;
        }

        BitBoard testBoard = simulateMove(bitBoard, move);
        return !testBoard.isInCheck(move.getColor());
    }

    private BitBoard simulateMove(BitBoard bitBoard, Move move) {
        // Create a deep copy of the BitBoard object to avoid mutating the original board.
        BitBoard boardCopy = new BitBoard(bitBoard);

        // Perform the move on the copied board.
        boardCopy.performMove(move);

        // Return the new board state.
        return boardCopy;
    }
    private boolean isInCheck(BitBoard bitBoard, Color color) {
        Color opponentColor = Color.getOpponentColor(color);
        // Find the king's position
        Position kingPosition = findKingPosition(color);

        // Check if the king is under attack by any pawns
        if (isAttackedByPawns(bitBoard, kingPosition, color, opponentColor)) {
            return true;
        }

        // Check if the king is under attack by any knights
        if (isAttackedByKnights(bitBoard, kingPosition, opponentColor)) {
            return true;
        }

        // Check if the king is under attack horizontally or vertically (by rooks or queens)
        if (isAttackedHorizontallyOrVertically(bitBoard, kingPosition, opponentColor, PieceType.ROOK) ||
                isAttackedHorizontallyOrVertically(bitBoard, kingPosition, opponentColor, PieceType.QUEEN)) {
            return true;
        }

        // Check if the king is under attack diagonally (by bishops or queens)
        if (isAttackedDiagonally(bitBoard, kingPosition, opponentColor, PieceType.BISHOP) ||
                isAttackedDiagonally(bitBoard, kingPosition, opponentColor, PieceType.QUEEN)) {
            return true;
        }

        // Check if the king is under attack by the opposing king
        return isAttackedByKing(bitBoard, kingPosition, opponentColor);

        // If none of the checks return true, the king is not in check
    }


// Helper methods to check for attacks from each type of piece

    private boolean isAttackedByPawns(BitBoard bitBoard, Position kingPosition, Color color, Color opponentColor) {
        // Pawns attack one square diagonally forward
        int pawnAttackDirection = color == Color.WHITE ? -1 : 1; // Pawns move in opposite directions for each color
        int kingRank = kingPosition.getY();
        char kingFile = kingPosition.getX();

        // Check the two potential squares where an enemy pawn could be attacking from
        Position attackFromLeft = new Position((char) (kingFile - 1), kingRank + pawnAttackDirection);
        Position attackFromRight = new Position((char) (kingFile + 1), kingRank + pawnAttackDirection);

        // Check if either of these positions are occupied by an enemy pawn
        if (isValidBoardPosition(attackFromLeft) && bitBoard.isOccupiedByPawn(attackFromLeft, opponentColor)) {
            return true;
        }

        return isValidBoardPosition(attackFromRight) && bitBoard.isOccupiedByPawn(attackFromRight, Color.getOpponentColor(color));

        // No pawns are attacking the king
    }

    private boolean isAttackedByKnights(BitBoard bitBoard, Position kingPosition, Color opponentColor) {

        // Check all possible positions a knight could attack the king from
        for (int[] move : knightMoves) {
            int targetX = kingPosition.getX() + move[0];
            int targetY = kingPosition.getY() + move[1];

            // Check if the target position is within the board bounds
            if (targetX >= 'a' && targetX <= 'h' && targetY >= 1 && targetY <= 8) {
                Position targetPosition = new Position((char) targetX, targetY);

                // If a knight of the opposite color occupies one of these positions, the king is in check
                if (bitBoard.isOccupiedByKnight(targetPosition, opponentColor)) {
                    return true;
                }
            }
        }

        // If no knight attacks the king, return false
        return false;
    }

    private boolean isAttackedHorizontallyOrVertically(BitBoard bitBoard, Position kingPosition, Color opponentColor, PieceType pieceType) {
        int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}}; // Up, Right, Down, Left

        for (int[] dir : directions) {
            for (int i = 1; i <= 8; i++) {
                char file = (char) (kingPosition.getX() + dir[0] * i);
                int rank = kingPosition.getY() + dir[1] * i;

                // Check board boundaries first
                if (file < 'a' || file > 'h' || rank < 1 || rank > 8) {
                    break; // Off the edge of the board
                }

                Position position = new Position(file, rank); // Consider optimizing this if possible
                PieceType pieceAtPosition = bitBoard.getPieceTypeAtPosition(position);
                Color pieceColor = bitBoard.getPieceColorAtPosition(position);

                // If we encounter a piece
                if (pieceAtPosition != null) {
                    if (pieceColor == opponentColor && (pieceAtPosition == pieceType || pieceAtPosition == PieceType.QUEEN)) {
                        return true;
                    }
                    break; // Hit a piece, can't look any further in this direction
                }
            }
        }
        return false;
    }


    private boolean isAttackedDiagonally(BitBoard bitBoard, Position kingPosition, Color opponentColor, PieceType pieceType) {
        // Check all four diagonal directions from the king's position for bishops or queens
        int[][] directions = {{1, 1}, {1, -1}, {-1, -1}, {-1, 1}}; // Diagonal directions
        for (int[] dir : directions) {
            for (int i = 1; i <= 8; i++) {
                char file = (char) (kingPosition.getX() + dir[0] * i);
                int rank = kingPosition.getY() + dir[1] * i;

                if (file < 'a' || file > 'h' || rank < 1 || rank > 8) {
                    break; // We've gone off the edge of the board
                }

                Position position = new Position(file, rank);
                PieceType pieceAtPosition = bitBoard.getPieceTypeAtPosition(position);
                Color pieceColor = bitBoard.getPieceColorAtPosition(position);

                // If we encounter a piece
                if (pieceAtPosition != null) {
                    // If it's an opposing bishop or queen, and matches the piece type we're looking for
                    if (pieceColor == opponentColor &&
                            (pieceAtPosition == pieceType || pieceAtPosition == PieceType.QUEEN)) {
                        return true;
                    }
                    break; // We've hit a piece, can't look any further in this direction
                }
            }
        }
        return false;
    }

    private boolean isAttackedByKing(BitBoard bitBoard, Position kingPosition, Color opponentColor) {
        // The king can move one square in any direction, check all surrounding squares
        int[][] moves = {
                {1, 0}, {1, 1}, {0, 1}, {-1, 1},
                {-1, 0}, {-1, -1}, {0, -1}, {1, -1}
        };

        for (int[] move : moves) {
            char file = (char) (kingPosition.getX() + move[0]);
            int rank = kingPosition.getY() + move[1];

            if (file < 'a' || file > 'h' || rank < 1 || rank > 8) {
                continue; // This move is off the board
            }

            Position position = new Position(file, rank);
            PieceType pieceAtPosition = bitBoard.getPieceTypeAtPosition(position);
            Color pieceColor = bitBoard.getPieceColorAtPosition(position);

            // If there is a piece and it is the opposing king
            if (pieceAtPosition == PieceType.KING && pieceColor == opponentColor) {
                return true;
            }
        }

        return false;
    }

    // Utility method to find the king's position
    private Position findKingPosition(Color color) {
        // Search through the bitboard to find the king's position
        // This will likely involve iterating over all squares or using a more efficient lookup
        return new Position('e', color == Color.WHITE ? 1 : 8); // Placeholder
    }

    private boolean isMoveOnBoard(Move move) {
        // Check if the 'from' position is on the board
        if (move.getFrom().getX() < 'a' || move.getFrom().getX() > 'h' ||
                move.getFrom().getY() < 1 || move.getFrom().getY() > 8) {
            return false;
        }

        // Check if the 'to' position is on the board
        return move.getTo().getX() >= 'a' && move.getTo().getX() <= 'h' &&
                move.getTo().getY() >= 1 && move.getTo().getY() <= 8;

        // If both positions are within the valid range, the move is on the board
    }

    public List<Position> getPossibleMovesForPosition(Position fromPosition) {
        return getMovesFromPosition(fromPosition).stream()
                .map(Move::getTo)
                .collect(Collectors.toList());
    }

    private boolean isNotInCheckAfterMove(BitBoard board, Move move) {
        BitBoard testBoard = simulateMove(board, move);
        return !testBoard.isInCheck(move.getColor());
    }


    private boolean isInStateCheck(BitBoard board, Color color) {
        // The BitBoard class already has a method to check if a king is in check
        return board.isInCheck(color);
    }

    public boolean isInStateCheckMate(BitBoard board, Color color) {
        // First, check if the king is in check.
        boolean isInCheck = board.isInCheck(color);

        // Then, generate all possible moves for the player.
        List<Move> possibleMoves = board.getAllCurrentPossibleMoves();

        // Filter out the moves that would leave the king in check after they are made.
        // Note: You need to implement a method that checks if making a certain move would result in check.
        long legalMovesCount = possibleMoves.stream()
                .filter(move -> isNotInCheckAfterMove(board, move))
                .count();

        // Checkmate occurs when the king is in check and there are no legal moves left.
        return isInCheck && legalMovesCount == 0;
    }

    private void updateGameState() {
        if (getAllLegalMoves().size() == 0 && isInStateCheck(bitBoard, Color.WHITE) && bitBoard.whitesTurn) {
            gameState.setState("BLACK WON");
        } else if (getAllLegalMoves().size() == 0 && isInStateCheck(bitBoard, Color.BLACK) && !bitBoard.whitesTurn) {
            gameState.setState("WHITE WON");
        } else if (getAllLegalMoves().size() == 0) {
            gameState.setState("DRAW");
        }
    }

    public List<Move> getAllPossibleMovesForPlayerColor(Color color) {
        // Generate all possible moves for the color
        List<Move> allMoves = getAllLegalMoves();

        // Filter out moves that would result in the player being in check
        return allMoves.stream()
                .filter(move -> !wouldBeInCheckAfterMove(move, color))
                .collect(Collectors.toList());
    }

    private boolean wouldBeInCheckAfterMove(Move move, Color color) {
        // Simulate the move on a copy of the bitboard
        BitBoard boardCopy = new BitBoard(bitBoard);
        boardCopy.performMove(move);

        // Check if the player would be in check after the move
        return isInCheck(boardCopy, color);
    }

    public void undoLastMove() {
        if (moves.size() > 0) {
            bitBoard.undoMove(moves.getLast());
            moves.removeLast();
        }
    }

    public void importBoardFromFen(String fen) {
        this.bitBoard = FEN.translateFENtoBitBoard(fen);
    }
}
