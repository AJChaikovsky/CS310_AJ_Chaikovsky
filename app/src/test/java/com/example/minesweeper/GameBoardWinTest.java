package com.example.minesweeper;

import org.junit.Test;
import static org.junit.Assert.*;

public class GameBoardWinTest {
    private GameBoard knownBoard() {
        return GameBoard.fromMinePositions(0, 2, 14, 44, 55, 99);
    }

    @Test
    public void zeroExpansionRevealsConnectedSafeCellsButNeverMines() {
        GameBoard board = knownBoard();
        board.dig(9, 0);
        assertTrue(board.getRevealedSafeCount() > 1);
        // Expansion reaches the numbered boundary, but not isolated numbered cells.
        assertEquals(1, board.getCell(9, 8).getAdjacentMines());
        assertTrue(board.getCell(9, 8).isRevealed());
        assertFalse(board.getCell(0, 1).isRevealed());
        assertFalse(board.isWon());
        for (int row = 0; row < 10; row++) {
            for (int column = 0; column < 10; column++) {
                if (board.getCell(row, column).isRevealed()) {
                    assertFalse(board.getCell(row, column).isMine());
                }
            }
        }
        assertFalse(board.isLost());
    }

    @Test
    public void flaggedCellStopsExpansionUntilItIsUnflagged() {
        GameBoard board = knownBoard();
        board.toggleFlag(8, 0);
        board.dig(9, 0);
        assertTrue(board.getCell(8, 0).isFlagged());
        assertFalse(board.getCell(8, 0).isRevealed());
        int revealedBeforeUnflag = board.getRevealedSafeCount();
        board.toggleFlag(8, 0);
        board.dig(8, 0);
        assertTrue(board.getCell(8, 0).isRevealed());
        assertTrue(board.getRevealedSafeCount() > revealedBeforeUnflag);
        assertFalse(board.isWon());
    }

    @Test
    public void revealingAllNinetyFourSafeCellsWinsAndRevealsTheBoard() {
        GameBoard board = knownBoard();
        for (int row = 0; row < 10; row++) {
            for (int column = 0; column < 10; column++) {
                Cell cell = board.getCell(row, column);
                if (!cell.isMine() && !cell.isRevealed()) {
                    assertTrue(board.dig(row, column));
                }
            }
        }
        assertTrue(board.isWon());
        assertFalse(board.isLost());
        assertTrue(board.isGameOver());
        assertEquals(94, board.getRevealedSafeCount());
        for (int row = 0; row < 10; row++) {
            for (int column = 0; column < 10; column++) {
                assertTrue(board.getCell(row, column).isRevealed());
            }
        }
        assertFalse(board.dig(0, 0));
        assertFalse(board.toggleFlag(9, 0));
        assertFalse(board.toggleMode());
    }

    @Test
    public void flaggedSafeCellPreventsAnOtherwiseCompleteFirstTapWin() {
        GameBoard board = GameBoard.fromMinePositions(0, 1, 2, 3, 4, 5);
        board.toggleFlag(9, 9);
        board.dig(9, 0);
        assertEquals(93, board.getRevealedSafeCount());
        assertFalse(board.isGameOver());
        assertFalse(board.getCell(9, 9).isRevealed());
        board.toggleFlag(9, 9);
        board.dig(9, 9);
        assertEquals(94, board.getRevealedSafeCount());
        assertTrue(board.isWon());
    }

    @Test
    public void flagsAloneDoNotWin() {
        GameBoard board = knownBoard();
        for (int position : new int[]{0, 2, 14, 44, 55, 99}) {
            board.toggleFlag(position / 10, position % 10);
        }
        assertFalse(board.isWon());
        assertFalse(board.isLost());
        assertEquals(0, board.getRevealedSafeCount());
        assertEquals(0, countRevealed(board));
    }

    private int countRevealed(GameBoard board) {
        int count = 0;
        for (int row = 0; row < 10; row++) {
            for (int column = 0; column < 10; column++) {
                if (board.getCell(row, column).isRevealed()) count++;
            }
        }
        return count;
    }
}
