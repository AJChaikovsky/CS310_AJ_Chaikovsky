package com.example.minesweeper;

import org.junit.Test;
import static org.junit.Assert.*;

public class GameBoardFlagTest {
    private GameBoard knownBoard() {
        return GameBoard.fromMinePositions(0, 2, 14, 44, 55, 99);
    }

    @Test
    public void newGameStartsInDigModeWithSixRemainingMines() {
        GameBoard board = knownBoard();
        assertFalse(board.isFlagMode());
        assertEquals(0, board.getFlagCount());
        assertEquals(6, board.getRemainingMines());
        assertTrue(board.tap(0, 1));
        assertTrue(board.getCell(0, 1).isRevealed());
        assertFalse(board.getCell(0, 1).isFlagged());
    }

    @Test
    public void modeSwitchMakesTapsPlaceAndRemoveFlags() {
        GameBoard board = knownBoard();
        assertTrue(board.toggleMode());
        assertTrue(board.isFlagMode());
        assertTrue(board.tap(0, 0));
        assertTrue(board.getCell(0, 0).isFlagged());
        assertFalse(board.getCell(0, 0).isRevealed());
        assertFalse(board.isLost());
        assertEquals(5, board.getRemainingMines());
        assertTrue(board.tap(0, 0));
        assertFalse(board.getCell(0, 0).isFlagged());
        assertEquals(6, board.getRemainingMines());
        assertTrue(board.toggleMode());
        assertFalse(board.isFlagMode());
    }

    @Test
    public void counterCanGoNegativeAndRecoversWhenUnflagging() {
        GameBoard board = knownBoard();
        board.toggleMode();
        for (int column = 0; column < 7; column++) assertTrue(board.tap(0, column));
        assertEquals(7, board.getFlagCount());
        assertEquals(-1, board.getRemainingMines());
        assertTrue(board.tap(0, 6));
        assertEquals(0, board.getRemainingMines());
        for (int column = 0; column < 6; column++) assertTrue(board.tap(0, column));
        assertEquals(0, board.getFlagCount());
        assertEquals(6, board.getRemainingMines());
    }

    @Test
    public void everyHiddenCellCanBeFlaggedRegardlessOfMineCount() {
        GameBoard board = knownBoard();
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) assertTrue(board.toggleFlag(row, col));
        }
        assertEquals(100, board.getFlagCount());
        assertEquals(-94, board.getRemainingMines());
        assertEquals(0, board.getRevealedSafeCount());
        assertFalse(board.isLost());
    }

    @Test
    public void neitherFlaggedMinesNorFlaggedSafeCellsCanBeDug() {
        for (int column : new int[]{0, 1}) {
            GameBoard board = knownBoard();
            board.toggleMode();
            board.tap(0, column);
            board.toggleMode();
            assertFalse(board.tap(0, column));
            assertFalse(board.dig(0, column));
            assertTrue(board.getCell(0, column).isFlagged());
            assertFalse(board.getCell(0, column).isRevealed());
            assertFalse(board.isLost());
            assertEquals(0, board.getRevealedSafeCount());
            assertEquals(5, board.getRemainingMines());
        }
    }

    @Test
    public void unflaggingMakesCellDiggableAgain() {
        GameBoard board = knownBoard();
        board.toggleFlag(0, 0);
        board.toggleFlag(0, 0);
        assertTrue(board.dig(0, 0));
        assertTrue(board.isLost());
        assertEquals(0, board.getFlagCount());
    }

    @Test
    public void revealedCellsCannotBeFlagged() {
        GameBoard board = knownBoard();
        board.dig(0, 1);
        board.toggleMode();
        assertFalse(board.tap(0, 1));
        assertFalse(board.toggleFlag(0, 1));
        assertFalse(board.getCell(0, 1).isFlagged());
        assertEquals(6, board.getRemainingMines());
        assertEquals(1, board.getRevealedSafeCount());
    }

    @Test
    public void diggingNearAFlagKeepsThatCellHidden() {
        GameBoard board = knownBoard();
        board.toggleFlag(8, 0);
        board.dig(9, 0);
        assertTrue(board.getCell(8, 0).isFlagged());
        assertFalse(board.getCell(8, 0).isRevealed());
        assertEquals(5, board.getRemainingMines());
    }

    @Test
    public void lossRevealsFlagsButBlocksFurtherFlagAndModeChanges() {
        GameBoard board = knownBoard();
        board.toggleFlag(0, 1);
        board.dig(0, 0);
        assertTrue(board.getCell(0, 1).isRevealed());
        assertFalse(board.toggleFlag(0, 1));
        assertFalse(board.toggleFlag(9, 0));
        assertFalse(board.toggleMode());
        assertFalse(board.isFlagMode());
        assertEquals(1, board.getFlagCount());
        assertEquals(5, board.getRemainingMines());
    }

    @Test
    public void invalidFlagCoordinatesLeaveCounterUnchanged() {
        GameBoard board = knownBoard();
        assertThrows(IndexOutOfBoundsException.class, () -> board.toggleFlag(-1, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> board.toggleFlag(10, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> board.toggleFlag(0, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> board.toggleFlag(0, 10));
        assertEquals(6, board.getRemainingMines());
    }
}
