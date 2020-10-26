package application;

import chess.ChessException;
import chess.ChessMatch;
import chess.ChessPiece;
import chess.ChessPosition;

import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {
  public static void main(String[] args) {
    Scanner read = new Scanner(System.in);
    ChessMatch chessMatch = new ChessMatch();

    while (true) {
      try {
        UI.clearScreen();
        UI.printMatch(chessMatch);
        System.out.println();
        System.out.print("Source: ");
        ChessPosition source = UI.readChessPosition(read);

        boolean[][] possibleMoves = chessMatch.possibleMoves(source);

        UI.clearScreen();
        UI.printBoard(chessMatch.getPieces(), possibleMoves);

        System.out.println();
        System.out.print("Target: ");
        ChessPosition target = UI.readChessPosition(read);

        ChessPiece capturedPiece = chessMatch.performChessMove(source, target);
      } catch (ChessException | InputMismatchException error) {
        System.out.println(error.getMessage());
        read.nextLine();
      }
    }
  }
}
