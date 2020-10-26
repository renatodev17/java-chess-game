package application;

import chess.ChessMatch;
import chess.ChessPiece;
import chess.ChessPosition;

import java.util.Scanner;

public class Main {
  public static void main(String[] args) {
    Scanner read = new Scanner(System.in);
    ChessMatch chessMatch = new ChessMatch();

    while (true) {
      UI.printBoard(chessMatch.getPieces());
      System.out.println();
      System.out.print("Source: ");
      ChessPosition source = UI.readChessPosition(read);

      System.out.println();
      System.out.print("Target: ");
      ChessPosition target = UI.readChessPosition(read);

      ChessPiece capturedPiece = chessMatch.performChessMove(source, target);
    }
  }
}
