package de.flyndre.flengine.rules;

import de.flyndre.flengine.datamodel.Board;
import de.flyndre.flengine.datamodel.Field;
import de.flyndre.flengine.datamodel.Move;
import de.flyndre.flengine.datamodel.Piece;
import de.flyndre.flengine.datamodel.enums.Color;
import de.flyndre.flengine.datamodel.enums.Line;
import de.flyndre.flengine.datamodel.enums.Row;
import de.flyndre.flengine.datamodel.enums.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author Tobias
 * Implementation of the individual rules for each chess piece.
 * Also contains the methods isFieldCovered and isLegalMove.
 */
public class PieceRule {

    protected final Line[] LINES = Line.values();
    protected final Row[] ROWS = Row.values();

    /**
     * Contains the current field of the king for future calculations.
     */
    protected Field kingField = null;

    /**
     * Returns all possible moves of a piece on the given field.
     * @param board current chessboard
     * @param field given field of piece
     * @return list of possible moves
     */
    public List<Move> getLegalMoves(Board board, Field field) {

        List<Move> moves;

        switch (board.getPiece(field).getTypeOfFigure()) {
            case PAWN -> moves = getLegalMovesPawn(board, field);
            case ROOK -> moves = getLegalMovesRook(board, field);
            case KNIGHT -> moves = getLegalMovesKnight(board, field);
            case BISHOP -> moves = getLegalMovesBishop(board, field);
            case QUEEN -> moves = getLegalMovesQueen(board, field);
            case KING -> moves = getLegalMovesKing(board, field);
            default -> throw new IllegalArgumentException(String.format(
                "Couldn't read field %s with piece of type %s", field, board.getPiece(field).getTypeOfFigure()));
        }
        return moves;
    }

    /**
     * Returns all possible moves of a pawn at given position.
     * @param board current chess board
     * @param field current field of a pawn
     * @return list of possible pawn moves
     */
    private List<Move> getLegalMovesPawn(Board board, Field field) {

        List<Move> moves = new ArrayList<>();
        Color color = board.getPiece(field).getColor();
        int fieldLine = field.getLine().ordinal();
        int fieldRow = field.getRow().ordinal();

        int direction = -1;
        if (color.equals(Color.WHITE)) direction = 1;

        // type is only set if pawn moves from second to last line to last line
        Type type = (field.getLine() == Line.SEVEN && color.equals(Color.WHITE) ||
            field.getLine() == Line.TWO && color.equals(Color.BLACK)) ? Type.QUEEN : null;

        // the field straight ahead has to be unoccupied to move
        if (board.getPiece(new Field(LINES[fieldLine + direction], ROWS[fieldRow])) == null)
        {
            moves.add(new Move(field, new Field(LINES[fieldLine + direction], ROWS[fieldRow]), type));
        }
        // the fields diagonal have to be occupied by opponent
        if (fieldRow > 0 && board.getPiece(new Field(LINES[fieldLine + direction], ROWS[fieldRow - 1])) != null &&
                !board.getPiece(new Field(LINES[fieldLine + direction], ROWS[fieldRow - 1])).getColor().equals(color))
        {
            moves.add(new Move(field, new Field(LINES[fieldLine + direction], ROWS[fieldRow - 1]), type));
        }
        if (fieldRow < 7 && board.getPiece(new Field(LINES[fieldLine + direction], ROWS[fieldRow + 1])) != null &&
                !board.getPiece(new Field(LINES[fieldLine + direction], ROWS[fieldRow + 1])).getColor().equals(color))
        {
            moves.add(new Move(field, new Field(LINES[fieldLine + direction], ROWS[fieldRow + 1]), type));
        }
        // the two fields ahead of the start line have to be unoccupied
        if ((field.getLine() == Line.TWO && color.equals(Color.WHITE) || field.getLine() == Line.SEVEN && color.equals(Color.BLACK)) &&
                board.getPiece(new Field(LINES[fieldLine + direction], ROWS[fieldRow])) == null &&
                board.getPiece(new Field(LINES[fieldLine + 2*direction], ROWS[fieldRow])) == null)
        {
            moves.add(new Move(field, new Field(LINES[fieldLine + 2*direction], ROWS[fieldRow])));
        }

        if (board.getEnPassantField() != null)
        {
            if ((board.getPiece(field).getColor().equals(Color.WHITE) && field.getLine().equals(Line.FIVE) &&
                    board.getEnPassantField().getLine().equals(Line.SIX) &&
                    Math.abs(field.getRow().ordinal() - board.getEnPassantField().getRow().ordinal()) == 1)) {
                moves.add(new Move(field, board.getEnPassantField()));
            }
            if ((board.getPiece(field).getColor().equals(Color.BLACK) && field.getLine().equals(Line.FOUR) &&
                    board.getEnPassantField().getLine().equals(Line.THREE) &&
                    Math.abs(field.getRow().ordinal() - board.getEnPassantField().getRow().ordinal()) == 1)) {
                moves.add(new Move(field, board.getEnPassantField()));
            }
        }

        return moves;
    }

    /**
     * Returns all possible moves of a rook at given position.
     * @param board current chess board
     * @param field current field of a rook
     * @return list of possible rook moves
     */
    private List<Move> getLegalMovesRook(Board board, Field field) {

        List<Move> moves = new ArrayList<>();
        int[][] directions = {{0,1},{0,-1},{1,0},{-1,0}};
        int fieldLine = field.getLine().ordinal();
        int fieldRow = field.getRow().ordinal();

        for (int[] direction : directions) {

            int l = fieldLine;
            int r = fieldRow;
            while (l + direction[0] >= 0 && l + direction[0] < 8 && r + direction[1] >= 0 && r + direction[1] < 8)
            {
                // next field in direction is unoccupied
                if (board.getPiece(new Field(LINES[l + direction[0]], ROWS[r + direction[1]])) == null)
                {
                    l += direction[0];
                    r += direction[1];
                    moves.add(new Move(field, new Field(LINES[l], ROWS[r])));
                }
                // next field in direction is occupied by opponent
                else if (!board.getPiece(new Field(LINES[l + direction[0]], ROWS[r + direction[1]])).getColor().equals(board.getPiece(field).getColor()))
                {
                    moves.add(new Move(field, new Field(LINES[l + direction[0]], ROWS[r + direction[1]])));
                    break;
                }
                // next field in direction is occupied by own piece
                else break;
            }
        }
        return moves;
    }

    /**
     * Returns all possible moves of a knight at given position.
     * @param board current chess board
     * @param field current field of a knight
     * @return list of possible knight moves
     */
    private List<Move> getLegalMovesKnight(Board board, Field field) {

        List<Move> moves = new ArrayList<>();
        int[][] directions = {{-2,-1},{-2,1},{2,-1},{2,1},{-1,-2},{-1,2},{1,-2},{1,2}};
        int fieldLine = field.getLine().ordinal();
        int fieldRow = field.getRow().ordinal();

        for (int[] direction : directions) {

            // field has to be unoccupied or occupied by an opponent
            if (fieldLine + direction[0] >= 0 && fieldLine + direction[0] < 8 && fieldRow + direction[1] >= 0 && fieldRow + direction[1] < 8 &&
                    (board.getPiece(new Field(LINES[fieldLine + direction[0]], ROWS[fieldRow + direction[1]])) == null ||
                    !board.getPiece(new Field(LINES[fieldLine + direction[0]], ROWS[fieldRow + direction[1]])).getColor().equals(board.getPiece(field).getColor())))
            {
                moves.add(new Move(field,new Field(LINES[fieldLine + direction[0]], ROWS[fieldRow + direction[1]])));
            }
        }
        return moves;
    }

    /**
     * Returns all possible moves of a bishop at given position.
     * @param board current chess board
     * @param field current field of a bishop
     * @return list of possible bishop moves
     */
    private List<Move> getLegalMovesBishop(Board board, Field field) {

        List<Move> moves = new ArrayList<>();
        int[][] directions = {{1,1},{-1,-1},{1,-1},{-1,1}};
        int fieldLine = field.getLine().ordinal();
        int fieldRow = field.getRow().ordinal();

        for (int[] direction : directions) {

            int l = fieldLine;
            int r = fieldRow;
            while (l + direction[0] >= 0 && l + direction[0] < 8 && r + direction[1] >= 0 && r + direction[1] < 8)
            {
                // next field in direction is unoccupied
                if (board.getPiece(new Field(LINES[l + direction[0]], ROWS[r + direction[1]])) == null)
                {
                    l += direction[0];
                    r += direction[1];
                    moves.add(new Move(field, new Field(LINES[l], ROWS[r])));
                }
                // next field in direction is occupied by opponent
                else if (!board.getPiece(new Field(LINES[l + direction[0]], ROWS[r + direction[1]])).getColor().equals(board.getPiece(field).getColor()))
                {
                    moves.add(new Move(field, new Field(LINES[l + direction[0]], ROWS[r + direction[1]])));
                    break;
                }
                // next field in direction is occupied by own piece
                else break;
            }
        }
        return moves;
    }

    /**
     * Returns all possible moves of a queen at given position.
     * @param board current chess board
     * @param field current field of a queen
     * @return list of possible queen moves
     */
    private List<Move> getLegalMovesQueen(Board board, Field field) {
        return Stream.concat(getLegalMovesBishop(board, field).stream(), getLegalMovesRook(board, field).stream()).toList();
    }

    /**
     * Returns all possible moves of a king at given position.
     * @param board current chess board
     * @param field current field of a king
     * @return list of possible king moves
     */
    private List<Move> getLegalMovesKing(Board board, Field field) {

        List<Move> moves = new ArrayList<>();
        int fieldLine = field.getLine().ordinal();
        int fieldRow = field.getRow().ordinal();
        Color color = board.getPiece(field).getColor();
        Color opponentColor = color.equals(Color.WHITE) ? Color.BLACK : Color.WHITE;

        // set the field of the king for future calculations
        kingField = field;

        // castle/rochade
        moves.addAll(getCastleMoves(board, field));

        // set field of king to null
        board.setPiece(null, field);

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {

                if (i == 0 && j == 0) continue;

                // field has to be unoccupied or occupied by an opponent
                if (fieldLine + i >= 0 && fieldLine + i < 8 && fieldRow + j >= 0 && fieldRow + j < 8 &&
                        (board.getPiece(new Field(LINES[fieldLine + i], ROWS[fieldRow + j])) == null ||
                        !board.getPiece(new Field(LINES[fieldLine + i], ROWS[fieldRow + j])).getColor().equals(color)) &&
                        // field must not be covered by opponent
                        !isFieldCovered(board, new Field(LINES[fieldLine + i], ROWS[fieldRow + j]), opponentColor) &&
                        // a field may not be covered by opponent's king (covered by own piece) but king must not move onto field anyway
                        !isKingInRange(board, new Field(LINES[fieldLine + i], ROWS[fieldRow + j]), opponentColor))
                {
                    moves.add(new Move(field, new Field(LINES[fieldLine + i], ROWS[fieldRow + j])));
                }
            }
        }

        // re-set king field of the chess board
        board.setPiece(new Piece(Type.KING, color), field);

        return moves;
    }

    /**
     * Returns the possible castle moves of a king. <br>
     * A castle move is only possible if:
     * <ul>
     *     <li>Neither king nor rook have moved</li>
     *     <li>There are no pieces between king and rook</li>
     *     <li>Neither king nor rook are currently in check</li>
     *     <li>None of the fields between is covered by an opponents piece</li>
     * </ul>
     *
     * @see <a href="https://de.wikipedia.org/wiki/Rochade#Voraussetzungen">Voraussetzungen Rochade</a>
     *
     * @param board current chess board
     * @param field current field of a king
     * @return list of 0 to 2 possible castle moves
     */
    private List<Move> getCastleMoves(Board board, Field field) {

        List<Move> moves = new ArrayList<>();
        Color opponentColor = board.getPiece(field).getColor().equals(Color.WHITE) ? Color.BLACK : Color.WHITE;
        boolean canQueensideCastle = opponentColor.equals(Color.WHITE) ? board.getBlackLongCastling() : board.getWhiteLongCastling();
        boolean canKingsideCastle = opponentColor.equals(Color.WHITE) ? board.getBlackShortCastling() : board.getWhiteShortCastling();

        // king is on its original field
        if (field.getRow().equals(Row.E) && (field.getLine().equals(Line.ONE) && board.getPiece(field).getColor().equals(Color.WHITE) ||
                field.getLine().equals(Line.EIGHT) && board.getPiece(field).getColor().equals(Color.BLACK)) &&
                // king is not in check and king- or queenside castle is possible
                !isFieldCovered(board, field, opponentColor) && (canQueensideCastle || canKingsideCastle))
        {
            // queen-side castle
            // there is a rook on row A in the same line of the same color, queenside castle is possible
            if (canQueensideCastle && board.getPiece(new Field(field.getLine(), Row.A)) != null &&
                    board.getPiece(new Field(field.getLine(), Row.A)).getTypeOfFigure().equals(Type.ROOK) &&
                    board.getPiece(new Field(field.getLine(), Row.A)).getColor().equals(board.getPiece(field).getColor()) &&
                    // the rook is not covered by an opponents piece
                    !isFieldCovered(board, new Field(field.getLine(), Row.A), opponentColor) &&
                    // there are no pieces between
                    board.getPiece(new Field(field.getLine(), Row.B)) == null &&
                    board.getPiece(new Field(field.getLine(), Row.C)) == null &&
                    board.getPiece(new Field(field.getLine(), Row.D)) == null &&
                    // there are no field covered by the opponent between
                    !isFieldCovered(board, new Field(field.getLine(), Row.D), opponentColor) &&
                    !isFieldCovered(board, new Field(field.getLine(), Row.C), opponentColor) &&
                    !isFieldCovered(board, new Field(field.getLine(), Row.B), opponentColor))
            {
                moves.add(new Move(field, new Field(field.getLine(), Row.C)));
            }
            // king-side castle
            // there is a rook on row H in the same line of the same color, kingside castle is possible
            if (canKingsideCastle && board.getPiece(new Field(field.getLine(), Row.H)) != null &&
                    board.getPiece(new Field(field.getLine(), Row.H)).getTypeOfFigure().equals(Type.ROOK) &&
                    board.getPiece(new Field(field.getLine(), Row.H)).getColor().equals(board.getPiece(field).getColor()) &&
                    // the rook is not covered by an opponents piece
                    !isFieldCovered(board, new Field(field.getLine(), Row.H), opponentColor) &&
                    // there are no pieces between
                    board.getPiece(new Field(field.getLine(), Row.F)) == null &&
                    board.getPiece(new Field(field.getLine(), Row.G)) == null &&
                    // there are no field covered by the opponent between
                    !isFieldCovered(board, new Field(field.getLine(), Row.F), opponentColor) &&
                    !isFieldCovered(board, new Field(field.getLine(), Row.G), opponentColor))
            {
                moves.add(new Move(field, new Field(field.getLine(), Row.G)));
            }
        }
        return moves;
    }

    /**
     * Returns if a king of given color is in a one-field-radius of the given field.
     * @param board current chess board
     * @param field field to check if a king is in range
     * @param color color of king in range
     * @return true if a king of given color is in range of the field
     */
    private boolean isKingInRange(Board board, Field field, Color color) {

        int fieldLine = field.getLine().ordinal();
        int fieldRow = field.getRow().ordinal();

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {

                if (i == 0 && j == 0) continue;

                if (fieldLine + i >= 0 && fieldLine + i < 8 && fieldRow + j >= 0 && fieldRow + j < 8 &&
                        board.getPiece(new Field(LINES[fieldLine + i], ROWS[fieldRow + j])) != null &&
                        board.getPiece(new Field(LINES[fieldLine + i], ROWS[fieldRow + j])).equals(new Piece(Type.KING, color))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns if the given field is covered by a piece of the given color.
     * @param board current chess board
     * @param field field that's checked
     * @param color color of player that covers the field
     * @return true if a piece of the player covers that field
     */
    public boolean isFieldCovered(Board board, Field field, Color color) {

        int[][] rookDirections = {{0,1},{0,-1},{1,0},{-1,0}};
        int[][] bishopDirections = {{1,1},{-1,-1},{1,-1},{-1,1}};
        int[][] knightMoves = {{-2,-1},{-2,1},{2,-1},{2,1},{-1,-2},{-1,2},{1,-2},{1,2}};
        int pawnDirection = color.equals(Color.WHITE) ? -1 : 1;

        int fieldLine = field.getLine().ordinal();
        int fieldRow = field.getRow().ordinal();

        // field is covered by pawn
        if (color.equals(Color.WHITE) && fieldLine > 0 || color.equals(Color.BLACK) && fieldLine < 7) {
            if (fieldRow > 0 && board.getPiece(new Field(LINES[fieldLine + pawnDirection], ROWS[fieldRow - 1])) != null &&
                    board.getPiece(new Field(LINES[fieldLine + pawnDirection], ROWS[fieldRow - 1])).getTypeOfFigure().equals(Type.PAWN) &&
                    board.getPiece(new Field(LINES[fieldLine + pawnDirection], ROWS[fieldRow - 1])).getColor().equals(color))
            {
                return true;
            }
            if (fieldRow < 7 && board.getPiece(new Field(LINES[fieldLine + pawnDirection], ROWS[fieldRow + 1])) != null &&
                    board.getPiece(new Field(LINES[fieldLine + pawnDirection], ROWS[fieldRow + 1])).getTypeOfFigure().equals(Type.PAWN) &&
                    board.getPiece(new Field(LINES[fieldLine + pawnDirection], ROWS[fieldRow + 1])).getColor().equals(color))
            {
                return true;
            }
        }

        // field is covered by knight
        for (int[] knightMove : knightMoves) {
            if (fieldLine + knightMove[0] >= 0 && fieldLine + knightMove[0] < 8 && fieldRow + knightMove[1] >= 0 && fieldRow + knightMove[1] < 8 &&
                    board.getPiece(new Field(LINES[fieldLine + knightMove[0]], ROWS[fieldRow + knightMove[1]])) != null &&
                    board.getPiece(new Field(LINES[fieldLine + knightMove[0]], ROWS[fieldRow + knightMove[1]])).getTypeOfFigure().equals(Type.KNIGHT) &&
                    board.getPiece(new Field(LINES[fieldLine + knightMove[0]], ROWS[fieldRow + knightMove[1]])).getColor().equals(color))
            {
                return true;
            }
        }

        // field is covered by rook or queen
        for (int[] rookDirection : rookDirections) {

            int l = fieldLine + rookDirection[0];
            int r = fieldRow + rookDirection[1];

            while (l >= 0 && l < 8 && r >= 0 && r < 8)
            {
                if (board.getPiece(new Field(LINES[l], ROWS[r])) != null) {
                    if ((board.getPiece(new Field(LINES[l], ROWS[r])).getTypeOfFigure().equals(Type.ROOK) ||
                            board.getPiece(new Field(LINES[l], ROWS[r])).getTypeOfFigure().equals(Type.QUEEN)) &&
                            board.getPiece(new Field(LINES[l], ROWS[r])).getColor().equals(color))
                    {
                        return true;
                    }
                    break;
                }
                l += rookDirection[0];
                r += rookDirection[1];
            }
        }

        // field is covered by bishop or queen
        for (int[] bishopDirection : bishopDirections) {

            int l = fieldLine + bishopDirection[0];
            int r = fieldRow + bishopDirection[1];

            while (l >= 0 && l < 8 && r >= 0 && r < 8)
            {
                if (board.getPiece(new Field(LINES[l], ROWS[r])) != null) {
                    if ((board.getPiece(new Field(LINES[l], ROWS[r])).getTypeOfFigure().equals(Type.BISHOP) ||
                            board.getPiece(new Field(LINES[l], ROWS[r])).getTypeOfFigure().equals(Type.QUEEN)) &&
                            board.getPiece(new Field(LINES[l], ROWS[r])).getColor().equals(color))
                    {
                        return true;
                    }
                    break;
                }
                l += bishopDirection[0];
                r += bishopDirection[1];
            }
        }

        // field is covered by king
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {

                if (i == 0 && j == 0) continue;

                if (fieldLine + i >= 0 && fieldLine + i < 8 && fieldRow + j >= 0 && fieldRow + j < 8 &&
                        board.getPiece(new Field(LINES[fieldLine + i], ROWS[fieldRow + j])) != null &&
                        board.getPiece(new Field(LINES[fieldLine + i], ROWS[fieldRow + j])).getTypeOfFigure().equals(Type.KING) &&
                        board.getPiece(new Field(LINES[fieldLine + i], ROWS[fieldRow + j])).getColor().equals(color) &&
                        !isFieldCovered(board, field, color.equals(Color.WHITE) ? Color.BLACK : Color.WHITE))
                {
                    return true;
                }
            }
        }
        return false;
    }
}