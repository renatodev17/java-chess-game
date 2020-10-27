package chess;

import boardgame.Board;
import boardgame.Piece;
import boardgame.Position;
import chess.pieces.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ChessMatch {
  private Board board;
  private int turn;
  private Color currentPlayer;
  private List<Piece> piecesOnTheBoard = new ArrayList<>();
  private List<Piece> capturedPieces = new ArrayList<>();
  private boolean check; // initial value -> false
  private boolean checkMate;
  private ChessPiece enPassantVulnerable;

  public ChessMatch() {
    board = new Board(8, 8);
    turn = 1;
    currentPlayer = Color.WHITE;
    initialSetup();
  }

  public ChessPiece getEnPassantVulnerable() {
    return enPassantVulnerable;
  }

  public boolean getCheck() {
    return check;
  }

  public boolean getCheckMate() {
    return checkMate;
  }

  public int getTurn() {
    return turn;
  }

  public Color getCurrentPlayer() {
    return currentPlayer;
  }

  public ChessPiece[][] getPieces() {
    ChessPiece[][] mat = new ChessPiece[board.getRows()][board.getColumns()];

    for (int i = 0; i < board.getRows(); i++) {
      for (int j = 0; j < board.getColumns(); j++) {
        mat[i][j] = (ChessPiece) board.piece(i, j);
      }
    }

    return mat;
  }

  public boolean[][] possibleMoves(ChessPosition searchPosition) {
    Position position = searchPosition.toPosition();
    validateSourcePosition(position);
    return board.piece(position).possibleMoves();
  }

  public ChessPiece performChessMove(ChessPosition sourcePosition, ChessPosition targetPosition) {
    Position source = sourcePosition.toPosition();
    Position target = targetPosition.toPosition();
    validateSourcePosition(source);
    validateTargetPosition(source, target);
    Piece capturedPiece = makeMove(source, target);
    if (testCheck(currentPlayer)) {
      undoMove(source, target, capturedPiece);

      throw new ChessException("You can't put yourself in check");
    }

    ChessPiece movedPiece = (ChessPiece) board.piece(target);

    check = testCheck(opponent(currentPlayer));

    if (testCheckMate(opponent(currentPlayer))) {
      checkMate = true;
    } else {
      nextTurn();
    }

    if (movedPiece instanceof Pawn && target.getRow() == source.getRow() - 2 || target.getRow() == source.getRow() + 2) {
      enPassantVulnerable = movedPiece;
    } else {
      enPassantVulnerable = null;
    }

    return (ChessPiece) capturedPiece;
  }

  private void validateSourcePosition(Position position) {
    if (!board.thereIsAPiece(position)) {
      throw new ChessException("There is no piece on source position");
    }

    if (currentPlayer != ((ChessPiece)board.piece(position)).getColor()) {
      throw new ChessException("The chose piece is not yours");
    }

    if (!board.piece(position).isThereAnyPossibleMove()) {
      throw new ChessException("There is no possible moves for the chosen piece");
    }
  }

  private void validateTargetPosition(Position source, Position target) {
    if (!board.piece(source).possibleMove(target)) {
      throw new ChessException("The chosen piece can't move to target position");
    }
  }

  private void nextTurn() {
    turn++;
    currentPlayer = currentPlayer == Color.WHITE ? Color.BLACK : Color.WHITE;
  }

  private Piece makeMove(Position sourcePosition, Position targetPosition) {
    ChessPiece p = (ChessPiece) board.removePiece(sourcePosition);
    p.increaseMoveCount();
    Piece capturedPiece = board.removePiece(targetPosition);
    board.placePiece(p, targetPosition);

    if (capturedPiece != null) {
      piecesOnTheBoard.remove(capturedPiece);
      capturedPieces.add(capturedPiece);
    }

    if (p instanceof King && targetPosition.getColumn() == sourcePosition.getColumn() + 2) {
      Position sourceT = new Position(sourcePosition.getRow(), sourcePosition.getColumn() + 3);
      Position targetT = new Position(sourcePosition.getRow(), sourcePosition.getColumn() + 1);
      ChessPiece rook = (ChessPiece) board.removePiece(sourceT);
      board.placePiece(rook, targetT);
      rook.increaseMoveCount();
    }

    if (p instanceof King && targetPosition.getColumn() == sourcePosition.getColumn() - 2) {
      Position sourceT = new Position(sourcePosition.getRow(), sourcePosition.getColumn() - 4);
      Position targetT = new Position(sourcePosition.getRow(), sourcePosition.getColumn() - 1);
      ChessPiece rook = (ChessPiece) board.removePiece(sourceT);
      board.placePiece(rook, targetT);
      rook.increaseMoveCount();
    }

    if (p instanceof Pawn) {
      if (sourcePosition.getColumn() != targetPosition.getColumn() && capturedPiece == null) {
        Position pawnPosition;

        if (p.getColor() == Color.WHITE) {
          pawnPosition = new Position(targetPosition.getRow() + 1, targetPosition.getColumn());
        } else {
          pawnPosition = new Position(targetPosition.getRow() - 1, targetPosition.getColumn());
        }

        capturedPiece = board.removePiece(pawnPosition);
        capturedPieces.add(capturedPiece);
        piecesOnTheBoard.remove(capturedPiece);
      }
    }

    return capturedPiece;
  }

  private void undoMove(Position source, Position target, Piece capturedPiece) {
    ChessPiece p = (ChessPiece) board.removePiece(target);
    p.decreaseMoveCount();

    board.placePiece(p, source);

    if (capturedPiece != null) {
      board.placePiece(capturedPiece, target);
      capturedPieces.remove(capturedPiece);
      piecesOnTheBoard.add(capturedPiece);
    }

    if (p instanceof King && target.getColumn() == source.getColumn() + 2) {
      Position sourceT = new Position(source.getRow(), source.getColumn() + 3);
      Position targetT = new Position(source.getRow(), source.getColumn() + 1);
      ChessPiece rook = (ChessPiece) board.removePiece(targetT);
      board.placePiece(rook, sourceT);
      rook.decreaseMoveCount();
    }

    if (p instanceof King && target.getColumn() == source.getColumn() - 2) {
      Position sourceT = new Position(source.getRow(), source.getColumn() - 4);
      Position targetT = new Position(source.getRow(), source.getColumn() - 1);
      ChessPiece rook = (ChessPiece) board.removePiece(targetT);
      board.placePiece(rook, sourceT);
      rook.decreaseMoveCount();
    }

    if (p instanceof Pawn) {
      if (source.getColumn() != target.getColumn() && capturedPiece == enPassantVulnerable) {
        ChessPiece pawn = (ChessPiece)board.removePiece(target);
        Position pawnPosition;
        if (p.getColor() == Color.WHITE) {
          pawnPosition = new Position(3, target.getColumn());
        } else {
          pawnPosition = new Position(4, target.getColumn());
        }
        board.placePiece(pawn, pawnPosition);
      }
    }
  }

  private ChessPiece king(Color color) {
    List<Piece> list = piecesOnTheBoard.stream().filter(pieceOnTheBoard -> ((ChessPiece)pieceOnTheBoard).getColor() == color).collect(Collectors.toList());
    for (Piece piece : list) {
      if (piece instanceof King) {
        return (ChessPiece) piece;
      }
    }

    throw new IllegalStateException("There is no " + color + " king on the board");
  }

  private boolean testCheck(Color color) {
    Position kingPosition = king(color).getChessPosition().toPosition();
    List<Piece> opponentPieces = piecesOnTheBoard.stream().filter(pieceOnTheBoard -> ((ChessPiece) pieceOnTheBoard).getColor() == opponent(color)).collect(Collectors.toList());

    for (Piece piece : opponentPieces) {
      boolean[][] matrix = piece.possibleMoves();

      if (matrix[kingPosition.getRow()][kingPosition.getColumn()]) {
        return true;
      }
    }

    return false;
  }

  private boolean testCheckMate(Color color) {
    if (!testCheck(color)) {
      return false;
    }

    List<Piece> list = piecesOnTheBoard.stream().filter(pieceOnTheBoard -> ((ChessPiece)pieceOnTheBoard).getColor() == color).collect(Collectors.toList());

    for (Piece piece : list) {
      boolean[][] matrix = piece.possibleMoves();

      for (int i = 0; i < board.getRows(); i++ ) {
        for (int j = 0; j < board.getColumns(); j++ ) {
          if (matrix[i][j]) {
            Position source = ((ChessPiece) piece).getChessPosition().toPosition();

            Position target = new Position(i, j);

            Piece capturedPiece = makeMove(source, target);

            boolean testCheck = testCheck(color);

            undoMove(source, target, capturedPiece);

            if (!testCheck) {
              return false;
            }
          }
        }
      }
    }

    return true;
  }

  private void placeNewPiece(char column, int row, ChessPiece piece) {
    board.placePiece(piece, new ChessPosition(column, row).toPosition());
    piecesOnTheBoard.add(piece);
  }

  private void initialSetup() {
    //WHITE
    placeNewPiece('d', 1, new Queen(board, Color.WHITE));

    placeNewPiece('b', 1, new Knight(board, Color.WHITE));
    placeNewPiece('g', 1, new Knight(board, Color.WHITE));

    placeNewPiece('c', 1, new Bishop(board, Color.WHITE));
    placeNewPiece('f', 1, new Bishop(board, Color.WHITE));

    placeNewPiece('e', 1, new King(board, Color.WHITE, this));

    placeNewPiece('a', 1, new Rook(board, Color.WHITE));
    placeNewPiece('h', 1, new Rook(board, Color.WHITE));

    placeNewPiece('a', 2, new Pawn(board, Color.WHITE, this));
    placeNewPiece('b', 2, new Pawn(board, Color.WHITE, this));
    placeNewPiece('c', 2, new Pawn(board, Color.WHITE, this));
    placeNewPiece('d', 2, new Pawn(board, Color.WHITE, this));
    placeNewPiece('e', 2, new Pawn(board, Color.WHITE, this));
    placeNewPiece('f', 2, new Pawn(board, Color.WHITE, this));
    placeNewPiece('g', 2, new Pawn(board, Color.WHITE, this));
    placeNewPiece('h', 2, new Pawn(board, Color.WHITE, this));

    placeNewPiece('c', 8, new Bishop(board, Color.BLACK));
    placeNewPiece('f', 8, new Bishop(board, Color.BLACK));

    //BLACK
    placeNewPiece('d', 8, new Queen(board, Color.BLACK));

    placeNewPiece('g', 8, new Knight(board, Color.BLACK));
    placeNewPiece('b', 8, new Knight(board, Color.BLACK));

    placeNewPiece('e', 8, new King(board, Color.BLACK, this));

    placeNewPiece('h', 8, new Rook(board, Color.BLACK));
    placeNewPiece('a', 8, new Rook(board, Color.BLACK));

    placeNewPiece('a', 7, new Pawn(board, Color.BLACK, this));
    placeNewPiece('b', 7, new Pawn(board, Color.BLACK, this));
    placeNewPiece('c', 7, new Pawn(board, Color.BLACK, this));
    placeNewPiece('d', 7, new Pawn(board, Color.BLACK, this));
    placeNewPiece('e', 7, new Pawn(board, Color.BLACK, this));
    placeNewPiece('f', 7, new Pawn(board, Color.BLACK, this));
    placeNewPiece('g', 7, new Pawn(board, Color.BLACK, this));
    placeNewPiece('h', 7, new Pawn(board, Color.BLACK, this));
  }

  private Color opponent(Color color) {
    if (color == Color.WHITE) {
      return Color.BLACK;
    } else {
      return Color.WHITE;
    }
  }
}
