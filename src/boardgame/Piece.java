package boardgame;

public abstract class Piece {
  protected Position position = null;
  private Board board;

  public Piece(Board board) {
    this.board = board;
  }

  protected Board getBoard() {
    return board;
  }

  public abstract boolean[][] possibleMoves();

  public boolean possibleMove(Position position) {
    return possibleMoves()[position.getRow()][position.getColumn()];
  }

  public boolean isThereAnyPossibleMove() {
    boolean[][] aux = possibleMoves();
    for (int i = 0; i < aux.length; i++) {
      for (int j = 0; j < aux.length; j++) {
        if (aux[i][j]) {
          return true;
        }
      }
    }

    return false;
  }
}
