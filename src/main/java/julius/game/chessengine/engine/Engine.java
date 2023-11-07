package julius.game.chessengine.engine;

import julius.game.chessengine.board.BitBoard;
import julius.game.chessengine.board.Move;
import julius.game.chessengine.board.Position;
import julius.game.chessengine.figures.PieceType;
import julius.game.chessengine.utils.Color;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Data
@Service
@Log4j2
public class Engine {

    private BitBoard bitBoard = new BitBoard();
    private GameState gameState = new GameState();
    private boolean whitesTurn = true;

    public void startNewGame() {
        bitBoard = new BitBoard();
        whitesTurn = true;
        gameState = new GameState();
        // Set up the initial position of the pieces on the bitboard
        bitBoard.setInitialPosition();
    }

    public GameState moveRandomFigure(Color color) {
        // Now, the color parameter is used to determine which moves to generate
        List<Move> moves = bitBoard.generateAllPossibleMoves(color);

        if (moves.isEmpty()) {
            throw new RuntimeException("No moves possible for " + color);
        }

        Random rand = new Random();
        Move randomMove = moves.get(rand.nextInt(moves.size()));

        // Execute the move on the bitboard
        bitBoard.performMove(randomMove);

        // Toggle turn unconditionally
        whitesTurn = !whitesTurn;

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
        if ((pieceColor == Color.WHITE && !whitesTurn) || (pieceColor == Color.BLACK && whitesTurn)) {
            bitBoard.logBoard();
            throw new IllegalStateException("It's not " + pieceColor + "'s turn");
        }


        // Determine if the move is a capture and if the move is en passant
        boolean isCapture = bitBoard.isOccupiedByOpponent(toPosition, color);
        boolean isEnPassantMove = bitBoard.isEnPassantPossible(toPosition, color) && pieceType == PieceType.PAWN;
        if (isEnPassantMove) {
            // Clear the captured pawn from its position for en passant
            bitBoard.clearSquare(bitBoard.bitIndex(toPosition.getX(), fromPosition.getY()), Color.getOpponentColor(color));
        }

        // Determine if the move is castling
        boolean isCastlingMove = pieceType == PieceType.KING && (Math.abs(toPosition.getX() - fromPosition.getX()) == 2);

        // Determine if the move is a promotion
        boolean isPromotion = pieceType == PieceType.PAWN && (toPosition.getY() == 1 || toPosition.getY() == 8);
        PieceType promotionPieceType = isPromotion ? PieceType.QUEEN : null; // Assume queen promotion for simplicity

        // Create a Move object for the move
        Move move = new Move(fromPosition, toPosition, pieceType, color, isCapture, isCastlingMove, isEnPassantMove, promotionPieceType);

        // Check if the move is legal and doesn't result in a check
        if (!isLegalMove(bitBoard, move)) {
            log.info(move.toString());
            bitBoard.logBoard();
            throw new IllegalStateException("Move is not legal or results in a check");
        }

        // Perform the move on the bitboard
        bitBoard.performMove(move);

        // Toggle the turn only after a successful move
        whitesTurn = !whitesTurn;

        // Update the game state
        updateGameState();

        return gameState;
    }


    private boolean isLegalMove(BitBoard bitBoard, Move move) {
        // Check if the move is within bounds of the board
        if (!isMoveOnBoard(move)) {
            return false;
        }

        // Check if the move adheres to the specific movement rules of the piece
        if (!moveMatchesPieceRules(bitBoard, move)) {
            return false;
        }

        // Check if the path is clear for moves that require it
        if (requiresClearPath(move.getPieceType()) && !isPathClear(bitBoard, move)) {
            return false;
        }

        // Check for special moves like castling or en passant
        if (move.isCastlingMove() && !canCastle(bitBoard, move)) {
            return false;
        }
        if (move.isEnPassantMove() && !canEnPassant(bitBoard, move)) {
            return false;
        }

        // Simulate the move and check for check
        BitBoard testBoard = simulateMove(bitBoard, move);
        if (testBoard.isInCheck(move.getColor())) {
            return false;
        }

        // If all checks pass, the move is legal
        return true;
    }

    private BitBoard simulateMove(BitBoard bitBoard, Move move) {
        // Create a deep copy of the BitBoard object to avoid mutating the original board.
        BitBoard boardCopy = new BitBoard(bitBoard); // You need to implement a copy constructor in your BitBoard class.

        // Perform the move on the copied board.
        boardCopy.performMove(move);

        // Return the new board state.
        return boardCopy;
    }

    private boolean canEnPassant(BitBoard bitBoard, Move move) {
        // En passant is only possible if the last move made by the opponent was a pawn moving two steps from the starting rank
        Position lastPawnPosition = bitBoard.getLastMoveDoubleStepPawnPosition();
        if (lastPawnPosition == null) {
            return false; // No pawn made the double step, or it's the first move of the game
        }

        // Check if the current pawn is on its fifth rank
        int currentPawnRank = move.getFrom().getY();
        if ((move.getColor() == Color.WHITE && currentPawnRank != 5) || (move.getColor() == Color.BLACK && currentPawnRank != 4)) {
            return false; // The pawn is not on the correct rank for en passant
        }

        // Check if the capturing pawn is adjacent to the pawn it is trying to capture en passant
        int fileDifference = Math.abs(move.getFrom().getX() - lastPawnPosition.getX());
        if (fileDifference != 1 || move.getFrom().getY() != lastPawnPosition.getY()) {
            return false; // The pawn is not adjacent or not on the same rank as the double-stepped pawn
        }

        // Check if the to position is the en passant target square
        if (!move.getTo().equals(new Position(lastPawnPosition.getX(), currentPawnRank == 5 ? 6 : 3))) {
            return false; // The move is not capturing the double-stepped pawn en passant
        }

        // All conditions are satisfied for en passant
        return true;
    }


    private boolean isInCheck(BitBoard bitBoard, Color color) {
        // Find the king's position
        Position kingPosition = findKingPosition(bitBoard, color);

        // Check if the king is under attack by any pawns
        if (isAttackedByPawns(bitBoard, kingPosition, color)) {
            return true;
        }

        // Check if the king is under attack by any knights
        if (isAttackedByKnights(bitBoard, kingPosition, color)) {
            return true;
        }

        // Check if the king is under attack horizontally or vertically (by rooks or queens)
        if (isAttackedHorizontallyOrVertically(bitBoard, kingPosition, color, PieceType.ROOK) ||
                isAttackedHorizontallyOrVertically(bitBoard, kingPosition, color, PieceType.QUEEN)) {
            return true;
        }

        // Check if the king is under attack diagonally (by bishops or queens)
        if (isAttackedDiagonally(bitBoard, kingPosition, color, PieceType.BISHOP) ||
                isAttackedDiagonally(bitBoard, kingPosition, color, PieceType.QUEEN)) {
            return true;
        }

        // Check if the king is under attack by the opposing king
        if (isAttackedByKing(bitBoard, kingPosition, color)) {
            return true;
        }

        // If none of the checks return true, the king is not in check
        return false;
    }


// Helper methods to check for attacks from each type of piece

    private boolean isAttackedByPawns(BitBoard bitBoard, Position kingPosition, Color color) {
        // Pawns attack one square diagonally forward
        int pawnAttackDirection = color == Color.WHITE ? -1 : 1; // Pawns move in opposite directions for each color
        int kingRank = kingPosition.getY();
        char kingFile = kingPosition.getX();

        // Check the two potential squares where an enemy pawn could be attacking from
        Position attackFromLeft = new Position((char) (kingFile - 1), kingRank + pawnAttackDirection);
        Position attackFromRight = new Position((char) (kingFile + 1), kingRank + pawnAttackDirection);

        // Check if either of these positions are occupied by an enemy pawn
        if (isValidBoardPosition(attackFromLeft) && bitBoard.isOccupiedByPawn(attackFromLeft, Color.getOpponentColor(color))) {
            return true;
        }

        if (isValidBoardPosition(attackFromRight) && bitBoard.isOccupiedByPawn(attackFromRight, Color.getOpponentColor(color))) {
            return true;
        }

        // No pawns are attacking the king
        return false;
    }

    private boolean isValidBoardPosition(Position position) {
        return position.getX() >= 'a' && position.getX() <= 'h' && position.getY() >= 1 && position.getY() <= 8;
    }

    private boolean isAttackedByKnights(BitBoard bitBoard, Position kingPosition, Color color) {
        // These are the relative moves a knight can make from any position
        int[][] knightMoves = {
                {-2, -1}, {-1, -2}, {1, -2}, {2, -1},
                {-2, 1}, {-1, 2}, {1, 2}, {2, 1}
        };

        // Check all possible positions a knight could attack the king from
        for (int[] move : knightMoves) {
            int targetX = kingPosition.getX() + move[0];
            int targetY = kingPosition.getY() + move[1];

            // Check if the target position is within the board bounds
            if (targetX >= 'a' && targetX <= 'h' && targetY >= 1 && targetY <= 8) {
                Position targetPosition = new Position((char) targetX, targetY);

                // If a knight of the opposite color occupies one of these positions, the king is in check
                if (bitBoard.isOccupiedByKnight(targetPosition, Color.getOpponentColor(color))) {
                    return true;
                }
            }
        }

        // If no knight attacks the king, return false
        return false;
    }

    private boolean isAttackedByRooks(BitBoard bitBoard, Position kingPosition, Color color) {
        // Rooks attack horizontally and vertically. Check all four directions from the king's position.
        return isAttackedHorizontallyOrVertically(bitBoard, kingPosition, color, PieceType.ROOK);
    }

    private boolean isAttackedByBishops(BitBoard bitBoard, Position kingPosition, Color color) {
        // Bishops attack diagonally. Check all four diagonal directions from the king's position.
        return isAttackedDiagonally(bitBoard, kingPosition, color, PieceType.BISHOP);
    }

    private boolean isAttackedByQueens(BitBoard bitBoard, Position kingPosition, Color color) {
        // Queens attack in any direction. Check horizontally, vertically, and diagonally.
        return isAttackedHorizontallyOrVertically(bitBoard, kingPosition, color, PieceType.QUEEN) ||
                isAttackedDiagonally(bitBoard, kingPosition, color, PieceType.QUEEN);
    }

    private boolean isAttackedHorizontallyOrVertically(BitBoard bitBoard, Position kingPosition, Color color, PieceType pieceType) {
        // Check all four directions: up, down, left, and right from the king's position for rooks or queens
        int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}}; // Up, Right, Down, Left
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
                    // If it's an opposing rook or queen, and matches the piece type we're looking for
                    if (pieceColor == Color.getOpponentColor(color) &&
                            (pieceAtPosition == pieceType || pieceAtPosition == PieceType.QUEEN)) {
                        return true;
                    }
                    break; // We've hit a piece, can't look any further in this direction
                }
            }
        }
        return false;
    }

    private boolean isAttackedDiagonally(BitBoard bitBoard, Position kingPosition, Color color, PieceType pieceType) {
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
                log.info("Checking position: " + file + rank + " for diagonal attacks");
                if (pieceAtPosition != null) {
                    bitBoard.logBoard();
                    log.info("Encountered a piece: " + pieceAtPosition + " of color " + pieceColor);
                    // If it's an opposing bishop or queen, and matches the piece type we're looking for
                    if (pieceColor == Color.getOpponentColor(color) &&
                            (pieceAtPosition == pieceType || pieceAtPosition == PieceType.QUEEN)) {
                        return true;
                    }
                    break; // We've hit a piece, can't look any further in this direction
                }
            }
        }
        return false;
    }

    private boolean isAttackedByKing(BitBoard bitBoard, Position kingPosition, Color color) {
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
            if (pieceAtPosition == PieceType.KING && pieceColor == Color.getOpponentColor(color)) {
                return true;
            }
        }

        return false;
    }

    // Utility method to find the king's position
    private Position findKingPosition(BitBoard bitBoard, Color color) {
        // Search through the bitboard to find the king's position
        // This will likely involve iterating over all squares or using a more efficient lookup
        return new Position('e', color == Color.WHITE ? 1 : 8); // Placeholder
    }


    private boolean canCastle(BitBoard bitBoard, Move move) {
        // Check if the king has moved, which would make castling illegal
        if (bitBoard.hasKingMoved(move.getColor())) {
            return false;
        }

        // Determine the rook's position based on the castling side (kingside or queenside)
        Position rookPosition = move.getColor() == Color.WHITE
                ? (move.getTo().getX() > move.getFrom().getX() ? new Position('h', 1) : new Position('a', 1))
                : (move.getTo().getX() > move.getFrom().getX() ? new Position('h', 8) : new Position('a', 8));

        // Check if the rook has moved
        if (bitBoard.hasRookMoved(rookPosition)) {
            return false;
        }

        // Check if the path between the king and the rook is clear
        if (!isPathClear(bitBoard, new Move(move.getFrom(), rookPosition, PieceType.KING, move.getColor(), false, false, false, null))) {
            return false;
        }

        // Check if the king is in check
        if (isInCheck(bitBoard, move.getColor())) {
            return false;
        }

        // Check if the squares the king passes over are under attack
        Position[] positionsToCheck = move.getColor() == Color.WHITE
                ? (new Position[]{new Position('f', 1), new Position('g', 1)})
                : (new Position[]{new Position('f', 8), new Position('g', 8)});
        for (Position position : positionsToCheck) {
            if (isSquareUnderAttack(bitBoard, position, move.getColor())) {
                return false;
            }
        }

        // All checks passed, castling is legal
        return true;
    }

    private boolean isSquareUnderAttack(BitBoard bitBoard, Position position, Color color) {
        // This method will need to check if the square is attacked by any of the opponent's pieces
        // This is a placeholder implementation and should be completed with actual attack detection logic
        return false;
    }

// The following are placeholder methods to be implemented:

    private boolean hasKingMoved(Color color) {
        // This should check if the king of the given color has moved
        return false;
    }

    private boolean hasRookMoved(Position rookPosition) {
        // This should check if the rook at the given position has moved
        return false;
    }

    private boolean isPathClear(BitBoard bitBoard, Move move) {
        // Calculate the direction of the move
        int xDirection = Integer.compare(move.getTo().getX(), move.getFrom().getX());
        int yDirection = Integer.compare(move.getTo().getY(), move.getFrom().getY());

        // Calculate the distance of the move
        int distance = Math.max(Math.abs(move.getTo().getX() - move.getFrom().getX()),
                Math.abs(move.getTo().getY() - move.getFrom().getY()));

        // Start from the next position after 'from' and go up to (but not including) 'to'
        for (int i = 1; i < distance; i++) {
            char file = (char) (move.getFrom().getX() + i * xDirection);
            int rank = move.getFrom().getY() + i * yDirection;

            // If any position between from and to (excluding to) is occupied, the path is not clear
            if (bitBoard.isOccupied(new Position(file, rank))) {
                return false;
            }
        }

        // If we checked all intervening squares and found no pieces, the path is clear
        return true;
    }


    private boolean requiresClearPath(PieceType pieceType) {
        // Knights can jump over other pieces, so they don't require a clear path.
        if (pieceType == PieceType.KNIGHT) {
            return false;
        }
        // Kings, pawns, and queens move in straight or diagonal lines and cannot jump over other pieces.
        return pieceType == PieceType.BISHOP || pieceType == PieceType.ROOK || pieceType == PieceType.QUEEN;
    }


    private boolean moveMatchesPieceRules(BitBoard bitBoard, Move move) {
        // Get the piece type from the move
        PieceType pieceType = move.getPieceType();

        // The logic will vary based on the piece type
        return switch (pieceType) {
            case PAWN -> pawnMoveMatchesRules(bitBoard, move);
            case KNIGHT -> knightMoveMatchesRules(bitBoard, move);
            case BISHOP -> bishopMoveMatchesRules(bitBoard, move);
            case ROOK -> rookMoveMatchesRules(bitBoard, move);
            case QUEEN -> queenMoveMatchesRules(bitBoard, move);
            case KING -> kingMoveMatchesRules(bitBoard, move);
            // If the piece type is not recognized, return false
        };
    }

    private boolean kingMoveMatchesRules(BitBoard bitBoard, Move move) {
        // Calculate the change in position
        int deltaX = Math.abs(move.getFrom().getX() - move.getTo().getX());
        int deltaY = Math.abs(move.getFrom().getY() - move.getTo().getY());

        // King moves only one square in any direction
        if ((deltaX <= 1 && deltaY <= 1) && !(deltaX == 0 && deltaY == 0)) {
            // Check if the destination square is either empty or occupied by an opponent
            if (!bitBoard.isOccupiedByOpponent(move.getTo(), move.getColor()) || bitBoard.isOccupied(move.getTo())) {
                return true;
            }
        }

        // Special move: castling
        // Implement the specific rules for castling here, which include checks for:
        // - The king and the chosen rook have not moved yet
        // - The squares between the king and the rook are unoccupied
        // - The king is not currently in check
        // - The king does not pass through or end up in a square that is under attack
        // (This is a placeholder, the actual castling logic should be implemented)
        return move.isCastlingMove();

        // If the move is not one square in any direction or a valid castling move, it's illegal
    }


    private boolean queenMoveMatchesRules(BitBoard bitBoard, Move move) {
        // The queen combines the power of a rook and a bishop, so it can move both
        // straight and diagonally. We can use the move matching rules for both pieces.

        // Check if the move is along a file or rank (like a rook)
        if (move.getFrom().getX() == move.getTo().getX() || move.getFrom().getY() == move.getTo().getY()) {
            return rookMoveMatchesRules(bitBoard, move);
        }
        // Check if the move is along a diagonal (like a bishop)
        else if (Math.abs(move.getFrom().getX() - move.getTo().getX()) == Math.abs(move.getFrom().getY() - move.getTo().getY())) {
            return bishopMoveMatchesRules(bitBoard, move);
        }

        // If the move is neither horizontal/vertical nor diagonal, it's not a valid queen move
        return false;
    }

    private boolean rookMoveMatchesRules(BitBoard bitBoard, Move move) {
        // Rooks move horizontally or vertically, so deltaX or deltaY should be zero
        int deltaX = Math.abs(move.getFrom().getX() - move.getTo().getX());
        int deltaY = Math.abs(move.getFrom().getY() - move.getTo().getY());

        if (deltaX != 0 && deltaY != 0) {
            // If both deltaX and deltaY are non-zero, it's not a horizontal or vertical move
            return false;
        }

        // Determine the direction of the move
        int xDirection = Integer.signum(move.getTo().getX() - move.getFrom().getX());
        int yDirection = Integer.signum(move.getTo().getY() - move.getFrom().getY());

        // Check if the path between the start and end position is clear
        // Start checking from the next square to the 'from' position, up to (but not including) the 'to' position
        int distance = Math.max(deltaX, deltaY); // Distance the rook will move
        for (int i = 1; i < distance; i++) {
            char file = (char) (move.getFrom().getX() + i * xDirection);
            int rank = move.getFrom().getY() + i * yDirection;
            Position position = new Position(file, rank);
            if (bitBoard.isOccupied(position)) {
                // If there is a piece in the way, the move is not legal
                return false;
            }
        }

        // Check if the destination square is occupied by a piece of the same color
        // You cannot capture your own pieces
        return !bitBoard.isOccupiedByColor(move.getTo(), move.getColor());

        // If all checks pass, the move is legal
    }


    private boolean bishopMoveMatchesRules(BitBoard bitBoard, Move move) {
        // Bishops move diagonally, so the change in the x (file) and y (rank) should be the same
        int deltaX = Math.abs(move.getFrom().getX() - move.getTo().getX());
        int deltaY = Math.abs(move.getFrom().getY() - move.getTo().getY());

        if (deltaX != deltaY) {
            // If deltaX and deltaY are not equal, it's not a diagonal move
            return false;
        }

        // Check if the path between the start and end position is clear
        int xDirection = Integer.signum(move.getTo().getX() - move.getFrom().getX());
        int yDirection = Integer.signum(move.getTo().getY() - move.getFrom().getY());

        // Start checking from the next square to the from position, up to (but not including) the to position
        for (int i = 1; i < deltaX; i++) {
            char file = (char) (move.getFrom().getX() + i * xDirection);
            int rank = move.getFrom().getY() + i * yDirection;
            Position position = new Position(file, rank);
            if (bitBoard.isOccupied(position)) {
                // If there is a piece in the way, the move is not legal
                return false;
            }
        }

        // Check if the destination square is occupied by a piece of the same color
        // You cannot capture your own pieces
        return !bitBoard.isOccupiedByColor(move.getTo(), move.getColor());

        // If all checks pass, the move is legal
    }


    private boolean pawnMoveMatchesRules(BitBoard bitBoard, Move move) {
        // Calculate movement direction based on pawn color
        int direction = move.getColor() == Color.WHITE ? 1 : -1;

        // Get the start and end positions
        Position from = move.getFrom();
        Position to = move.getTo();

        // Calculate the differences in the x and y coordinates
        int xDiff = Math.abs(from.getX() - to.getX());
        int yDiff = (to.getY() - from.getY()) * direction; // Multiplied by direction for forward movement

        // Check for single square move
        if (xDiff == 0 && yDiff == 1 && !bitBoard.isOccupied(to)) {
            return true;
        }

        // Check for initial double square move
        if (xDiff == 0 && yDiff == 2 && (from.getY() == (direction == 1 ? 2 : 7)) &&
                !bitBoard.isOccupied(to) && !bitBoard.isOccupied(new Position(from.getX(), from.getY() + direction))) {
            return true;
        }

        // Check for capture
        if (xDiff == 1 && yDiff == 1 && bitBoard.isOccupiedByOpponent(to, move.getColor())) {
            return true;
        }

        // Check for en passant (requires additional state to be tracked by the engine)
        if (xDiff == 1 && yDiff == 1 && bitBoard.isEnPassantPossible(to, move.getColor())) {
            return true;
        }

        // Check for promotion (not the full logic, just the position requirement)
        if (xDiff == 0 && (to.getY() == (direction == 1 ? 8 : 1))) {
            // Further checks are needed to ensure a promotion piece type is set in the move
            return move.getPromotionPieceType() != null;
        }

        // If none of the conditions are met, the move is not legal for a pawn
        return false;
    }


    // Here is an example implementation for the knight:
    private boolean knightMoveMatchesRules(BitBoard bitBoard, Move move) {
        // Calculate the differences in the x and y coordinates
        int xDiff = Math.abs(move.getFrom().getX() - move.getTo().getX());
        int yDiff = Math.abs(move.getFrom().getY() - move.getTo().getY());

        // Check if the knight's move is an L shape
        boolean isLShapedMove = (xDiff == 2 && yDiff == 1) || (xDiff == 1 && yDiff == 2);

        // Use the bitboard to check if the destination square is occupied by a piece of the same color
        boolean isDestinationOccupiedByOwnPiece = bitBoard.isOccupiedByColor(move.getTo(), move.getColor());

        // The move is legal if it's an L-shaped move and the destination is not occupied by a friendly piece
        return isLShapedMove && !isDestinationOccupiedByOwnPiece;
    }


    private boolean isMoveOnBoard(Move move) {
        // Check if the 'from' position is on the board
        if (move.getFrom().getX() < 'a' || move.getFrom().getX() > 'h' ||
                move.getFrom().getY() < 1 || move.getFrom().getY() > 8) {
            return false;
        }

        // Check if the 'to' position is on the board
        if (move.getTo().getX() < 'a' || move.getTo().getX() > 'h' ||
                move.getTo().getY() < 1 || move.getTo().getY() > 8) {
            return false;
        }

        // If both positions are within the valid range, the move is on the board
        return true;
    }

    public List<Move> getAllPossibleMoveFieldsForPlayerColor(Color color) {
        return getAllPossibleMoveFieldsForPlayerColor(bitBoard, color);
    }

    public List<Move> getAllPossibleMoveFieldsForPlayerColor(BitBoard board, Color color) {
        // First, generate all possible moves for the given color
        List<Move> moves = board.generateAllPossibleMoves(color);

        // Now filter out moves that would result in the player being in check
        return moves.stream()
                .filter(move -> !isInCheckAfterMoveSimulation(board, move, color))
                .collect(Collectors.toList());
    }

    private boolean isInCheckAfterMoveSimulation(BitBoard board, Move move, Color color) {
        // Create a copy of the bitboard to simulate the move
        BitBoard boardCopy = new BitBoard(board);

        // Perform the move on the copy
        boardCopy.performMove(move);

        // Check if the move results in a check against the moving player's king
        return boardCopy.isInCheck(color);
    }

    public List<Position> getPossibleMovesForPosition(Position fromPosition) {
        Color color = bitBoard.getPieceColorAtPosition(fromPosition);
        if (color == null || (color == Color.WHITE) != whitesTurn) {
            return Collections.emptyList();
        }

        PieceType pieceType = bitBoard.getPieceTypeAtPosition(fromPosition);
        List<Move> moves = bitBoard.generateMovesForPieceAtPosition(pieceType, color, fromPosition);

        return moves.stream()
                .filter(move -> !isInCheckAfterMove(bitBoard, move))
                .map(Move::getTo)
                .collect(Collectors.toList());
    }

    private boolean isInCheckAfterMove(BitBoard board, Move move) {
        BitBoard testBoard = simulateMove(board, move);
        return testBoard.isInCheck(move.getColor());
    }


    private boolean isInStateCheck(BitBoard board, Color color) {
        // The BitBoard class already has a method to check if a king is in check
        return board.isInCheck(color);
    }

    public boolean isInStateCheckMate(BitBoard board, Color color) {
        // First, check if the king is in check.
        boolean isInCheck = board.isInCheck(color);

        // Then, generate all possible moves for the player.
        List<Move> possibleMoves = board.generateAllPossibleMoves(color);

        // Filter out the moves that would leave the king in check after they are made.
        // Note: You need to implement a method that checks if making a certain move would result in check.
        long legalMovesCount = possibleMoves.stream()
                .filter(move -> !isInCheckAfterMove(board, move))
                .count();

        // Checkmate occurs when the king is in check and there are no legal moves left.
        return isInCheck && legalMovesCount == 0;
    }

    public double simulateMoveAndGetEfficiency(BitBoard board, Move move, Color currentPlayerColor, Color color, int levelOfDepth, double mostEfficientMove) {
        double efficiency = 0;
        int iteration = 0;

        // Simulate the move on a copy of the board
        BitBoard simulatedBoard = new BitBoard(board);
        simulatedBoard.performMove(move);

        // Here, you need a way to calculate the score difference after the move.
        // This function must be implemented according to your scoring system.
        int scoreDifference = calculateScoreDifference(simulatedBoard, color);
        efficiency += scoreDifference;

        while (iteration < levelOfDepth && efficiency > mostEfficientMove) {
            // Here, you need a way to determine the most efficient counter-move by the opponent.
            // This recursive function must be implemented.
            double opponentBestMoveEfficiency = getMostEfficientMoveEfficiency(simulatedBoard, Color.getOppositeColor(color), levelOfDepth - 1, -mostEfficientMove);

            if (color == currentPlayerColor) {
                efficiency -= opponentBestMoveEfficiency; // Minimizing opponent's score
            } else {
                efficiency += opponentBestMoveEfficiency; // Maximizing own score
            }
            iteration++;
        }

        // Logging is not available in this environment, but you can use System.out.println or another logging method as needed.
        // System.out.println("Efficiency: " + efficiency + " Move: " + move + " Player: " + color + " MostEfficientMove: " + mostEfficientMove);

        return efficiency;
    }

    private int calculateScoreDifference(BitBoard board, Color color) {
        // Assume we have a method that calculates the material score for a given color.
        int playerScore = calculateMaterialScore(board, color);
        int opponentScore = calculateMaterialScore(board, Color.getOppositeColor(color));

        // The score difference is the player's score minus the opponent's score.
        return playerScore - opponentScore;
    }

    private int calculateMaterialScore(BitBoard board, Color color) {
        // Material values could be defined as follows:
        // Pawn = 1, Knight = 3, Bishop = 3, Rook = 5, Queen = 9, King = 0 (since the king's value is immeasurable in a typical game)
        int pawnValue = 1;
        int knightValue = 3;
        int bishopValue = 3;
        int rookValue = 5;
        int queenValue = 9;
        // King's value is not counted since the game is generally over when the king is taken.

        // Count pieces by scanning the bitboards and using Long.bitCount to count the 1s.
        int pawns = Long.bitCount(board.getBitboardForPiece(PieceType.PAWN, color));
        int knights = Long.bitCount(board.getBitboardForPiece(PieceType.KNIGHT, color));
        int bishops = Long.bitCount(board.getBitboardForPiece(PieceType.BISHOP, color));
        int rooks = Long.bitCount(board.getBitboardForPiece(PieceType.ROOK, color));
        int queens = Long.bitCount(board.getBitboardForPiece(PieceType.QUEEN, color));

        // Calculate material score.
        int score = (pawns * pawnValue) +
                (knights * knightValue) +
                (bishops * bishopValue) +
                (rooks * rookValue) +
                (queens * queenValue);

        return score;
    }


    private double getMostEfficientMoveEfficiency(BitBoard board, Color color, int levelOfDepth, double alphaBetaBound) {
        // This recursive function would simulate moves for the opponent and return the most efficient move's efficiency
        // Implement the logic using a recursive minimax algorithm with alpha-beta pruning, or another suitable algorithm
        return 0; // Placeholder
    }


    public BitBoard simulateMoveAndGetDummyBoard(BitBoard board, Move move) {
        // Create a copy of the original BitBoard
        BitBoard dummyBoard = new BitBoard(board);

        // Perform the move on the dummy board
        dummyBoard.performMove(move);

        return dummyBoard;
    }

    private boolean isInStateCheckAfterMove(BitBoard board, Move move, Color color) {
        // Simulate the move on a dummy board
        BitBoard dummyBoard = simulateMoveAndGetDummyBoard(board, move);

        // Check if the king of the given color is in check after the move
        return dummyBoard.isInCheck(color);
    }

    private void updateGameState() {
        if (getAllPossibleMoveFieldsForPlayerColor(Color.WHITE).size() == 0 && isInStateCheck(bitBoard, Color.WHITE)) {
            gameState.setState("BLACK WON");
        } else if (getAllPossibleMoveFieldsForPlayerColor(Color.BLACK).size() == 0 && isInStateCheck(bitBoard, Color.BLACK)) {
            gameState.setState("WHITE WON");
        } else if (getAllPossibleMoveFieldsForPlayerColor(Color.WHITE).size() == 0 || getAllPossibleMoveFieldsForPlayerColor(Color.BLACK).size() == 0) {
            gameState.setState("DRAW");
        }
    }

    public List<Move> getAllPossibleMovesForPlayerColor(Color color) {
        // Generate all possible moves for the color
        List<Move> allMoves = bitBoard.generateAllPossibleMoves(color);

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

}
