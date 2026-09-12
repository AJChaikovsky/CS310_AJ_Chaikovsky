package com.example.minesweeper;

import org.junit.Test;
import java.util.Random;
import static org.junit.Assert.*;

public class GameBoardDigTest {
    private GameBoard knownBoard() {
        return GameBoard.fromMinePositions(0, 2, 14, 44, 55, 99);
    }

    @Test
    public void diggingSafeCellRevealsItsClueAndOnlyThatCell() {
        GameBoard board = knownBoard();
        assertTrue(board.dig(0, 1));
        assertTrue(board.getCell(0, 1).isRevealed());
        assertEquals(2, board.getCell(0, 1).getAdjacentMines());
        assertEquals(1, board.getRevealedSafeCount());
        assertEquals(1, countVisibleCells(board));
        assertFalse(board.isLost());
        assertEquals(-1, board.getDetonatedMinePosition());
    }

    @Test
    public void repeatedDigDoesNotCountASafeCellTwice() {
        GameBoard board = knownBoard();
        assertTrue(board.dig(0, 1));
        assertFalse(board.dig(0, 1));
        assertFalse(board.dig(0, 1));
        assertEquals(1, board.getRevealedSafeCount());
        assertEquals(1, countVisibleCells(board));
    }

    @Test
    public void zeroCellExpandsToConnectedZerosAndNumberedBoundary() {
        GameBoard board = knownBoard();
        assertTrue(board.dig(9, 0));
        assertEquals(0, board.getCell(9, 0).getAdjacentMines());
        assertTrue(countVisibleCells(board) > 1);
        assertTrue(board.getCell(9, 1).isRevealed());
        assertFalse(board.isLost());
    }

    @Test
    public void mineOnFirstDigLosesAndRevealsEveryCell() {
        GameBoard board = knownBoard();
        assertTrue(board.dig(9, 9));
        assertTrue(board.isLost());
        assertEquals(99, board.getDetonatedMinePosition());
        assertEquals(100, countVisibleCells(board));
        assertEquals(0, board.getRevealedSafeCount());
    }

    @Test
    public void revealingTheBoardAfterLossDoesNotCountAsSafeDigs() {
        GameBoard board = knownBoard();
        board.dig(0, 1);
        board.dig(5, 4);
        assertEquals(2, board.getRevealedSafeCount());
        assertTrue(board.dig(0, 0));
        assertTrue(board.isLost());
        assertEquals(100, countVisibleCells(board));
        assertEquals(2, board.getRevealedSafeCount());
    }

    @Test
    public void allFurtherDigsAreIgnoredAfterLoss() {
        GameBoard board = knownBoard();
        board.dig(0, 1);
        board.dig(4, 4);
        for (int row = 0; row < 10; row++) {
            for (int column = 0; column < 10; column++) {
                assertFalse(board.dig(row, column));
            }
        }
        assertTrue(board.isLost());
        assertEquals(44, board.getDetonatedMinePosition());
        assertEquals(1, board.getRevealedSafeCount());
        assertEquals(100, countVisibleCells(board));
    }

    @Test
    public void invalidDigCoordinatesDoNotChangeTheBoard() {
        GameBoard board = knownBoard();
        assertThrows(IndexOutOfBoundsException.class, () -> board.dig(-1, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> board.dig(10, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> board.dig(0, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> board.dig(0, 10));
        assertEquals(0, countVisibleCells(board));
        assertEquals(0, board.getRevealedSafeCount());
        assertFalse(board.isLost());
    }

    @Test
    public void replayingDigsRestoresBothActiveAndLostBoards() {
        GameBoard original = new GameBoard(new Random(42));
        GameBoard restored = new GameBoard(new Random(42));
        // This is the same seed + accepted-digs strategy used by MainActivity.
        for (int position = 0; position < 100; position++) {
            int row = position / 10;
            int column = position % 10;
            if (original.dig(row, column)) {
                assertTrue(restored.dig(row, column));
            }
            assertEquals(original.isLost(), restored.isLost());
            assertEquals(original.getRevealedSafeCount(), restored.getRevealedSafeCount());
            assertEquals(original.getDetonatedMinePosition(), restored.getDetonatedMinePosition());
            for (int r = 0; r < 10; r++) {
                for (int c = 0; c < 10; c++) {
                    assertEquals(original.getCell(r, c).isRevealed(),
                            restored.getCell(r, c).isRevealed());
                }
            }
        }
        assertTrue(restored.isLost());
    }

    private int countVisibleCells(GameBoard board) {
        int total = 0;
        for (int row = 0; row < 10; row++) {
            for (int column = 0; column < 10; column++) {
                if (board.getCell(row, column).isRevealed()) total++;
            }
        }
        return total;
    }
}
