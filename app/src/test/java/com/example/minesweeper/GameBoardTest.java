package com.example.minesweeper;

import org.junit.Test;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import static org.junit.Assert.*;

public class GameBoardTest {
    @Test
    public void freshBoardsHaveOneHundredHiddenCellsAndSixMines() {
        for (int seed = 0; seed < 100; seed++) {
            GameBoard board = new GameBoard(new Random(seed));
            Set<Cell> uniqueCells = new HashSet<>();
            int mines = 0;
            int safe = 0;
            assertEquals(10, board.getSize());
            assertEquals(6, board.getMineCount());
            for (int row = 0; row < 10; row++) {
                for (int col = 0; col < 10; col++) {
                    Cell cell = board.getCell(row, col);
                    uniqueCells.add(cell);
                    assertFalse(cell.isRevealed());
                    assertFalse(cell.isFlagged());
                    if (cell.isMine()) mines++; else safe++;
                }
            }
            assertEquals(100, uniqueCells.size());
            assertEquals(6, mines);
            assertEquals(94, safe);
        }
    }

    @Test
    public void fixedBoardHasCorrectCornerEdgeAndInteriorClues() {
        GameBoard board = GameBoard.fromMinePositions(0, 2, 14, 44, 55, 99);
        assertEquals(0, board.getCell(0, 0).getAdjacentMines()); // Excludes itself.
        assertEquals(2, board.getCell(0, 1).getAdjacentMines());
        assertEquals(2, board.getCell(0, 3).getAdjacentMines()); // Includes a diagonal.
        assertEquals(2, board.getCell(5, 4).getAdjacentMines());
        assertEquals(1, board.getCell(9, 8).getAdjacentMines());
        assertEquals(0, board.getCell(9, 0).getAdjacentMines());
    }

    @Test
    public void neighborCountsMatchMineDistancesAcrossEveryCell() {
        for (int seed = 0; seed < 25; seed++) {
            GameBoard board = new GameBoard(new Random(seed));
            for (int row = 0; row < 10; row++) {
                for (int col = 0; col < 10; col++) {
                    int expected = 0;
                    // Independent oracle: check distances to every mine on the board.
                    for (int mineRow = 0; mineRow < 10; mineRow++) {
                        for (int mineCol = 0; mineCol < 10; mineCol++) {
                            if (board.getCell(mineRow, mineCol).isMine()
                                    && Math.max(Math.abs(row - mineRow),
                                    Math.abs(col - mineCol)) == 1) expected++;
                        }
                    }
                    assertEquals(expected, board.getCell(row, col).getAdjacentMines());
                }
            }
        }
    }

    @Test
    public void neighborsDoNotWrapAcrossRows() {
        GameBoard board = GameBoard.fromMinePositions(19, 20, 44, 55, 70, 99);
        assertEquals(0, board.getCell(0, 0).getAdjacentMines());
        assertEquals(1, board.getCell(0, 9).getAdjacentMines());
        assertEquals(1, board.getCell(1, 0).getAdjacentMines());
        assertEquals(1, board.getCell(2, 9).getAdjacentMines());
    }

    @Test
    public void sameSeedReproducesTheMineLayout() {
        assertEquals(layout(new GameBoard(new Random(42))),
                layout(new GameBoard(new Random(42))));
    }

    @Test
    public void differentSeedsProduceVariedLayouts() {
        Set<String> layouts = new HashSet<>();
        for (int seed = 0; seed < 20; seed++) layouts.add(layout(new GameBoard(new Random(seed))));
        assertTrue(layouts.size() > 1);
    }

    @Test
    public void fixedMinePositionsAreCopiedIntoTheBoard() {
        int[] positions = {0, 2, 14, 44, 55, 99};
        GameBoard board = GameBoard.fromMinePositions(positions);
        positions[0] = 1;
        assertTrue(board.getCell(0, 0).isMine());
        assertFalse(board.getCell(0, 1).isMine());
    }

    @Test
    public void invalidMineLayoutsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> GameBoard.fromMinePositions(0, 1));
        assertThrows(IllegalArgumentException.class,
                () -> GameBoard.fromMinePositions(0, 0, 2, 3, 4, 5));
        assertThrows(IllegalArgumentException.class,
                () -> GameBoard.fromMinePositions(-1, 1, 2, 3, 4, 5));
        assertThrows(IllegalArgumentException.class,
                () -> GameBoard.fromMinePositions(100, 1, 2, 3, 4, 5));
        assertThrows(NullPointerException.class, () -> GameBoard.fromMinePositions((int[]) null));
        assertThrows(NullPointerException.class, () -> new GameBoard((Random) null));
    }

    @Test
    public void invalidCellCoordinatesAreRejected() {
        GameBoard board = new GameBoard(new Random(0));
        assertThrows(IndexOutOfBoundsException.class, () -> board.getCell(-1, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> board.getCell(10, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> board.getCell(0, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> board.getCell(0, 10));
    }

    private String layout(GameBoard board) {
        StringBuilder result = new StringBuilder();
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                result.append(board.getCell(row, col).isMine() ? '*' : '.');
            }
        }
        return result.toString();
    }
}
