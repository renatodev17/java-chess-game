package boardgame;

public class Piece {
  protected Position position = null;
  private Board board;

  public Piece(Board board) {
    this.board = board;
  }

  protected Board getBoard() {
    return board;
  }
}
