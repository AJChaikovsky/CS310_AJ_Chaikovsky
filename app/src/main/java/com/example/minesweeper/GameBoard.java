package com.example.minesweeper;

import java.util.Objects;
import java.util.Random;

/** Board data and digging rules, independent of Android views. */
public final class GameBoard {
    public static final int BOARD_SIZE = 10;
    public static final int CELL_COUNT = BOARD_SIZE * BOARD_SIZE;
    public static final int MINE_COUNT = 6;

    private final Cell[][] cells = new Cell[BOARD_SIZE][BOARD_SIZE];
    private boolean lost;
    private int revealedSafeCount;
    private int detonatedMinePosition = -1;

    public GameBoard() {
        this(new Random());
    }

    /** A seeded Random lets tests reproduce the same board. */
    public GameBoard(Random random) {
        this(randomMinePositions(Objects.requireNonNull(random, "random")));
    }

    private GameBoard(int[] minePositions) {
        Objects.requireNonNull(minePositions, "minePositions");
        if (minePositions.length != MINE_COUNT) {
            throw new IllegalArgumentException("A board must have exactly six mines");
        }
        boolean[][] mines = new boolean[BOARD_SIZE][BOARD_SIZE];
        for (int position : minePositions) {
            if (position < 0 || position >= CELL_COUNT) {
                throw new IllegalArgumentException("Mine position outside the board: " + position);
            }
            int row = position / BOARD_SIZE;
            int column = position % BOARD_SIZE;
            if (mines[row][column]) {
                throw new IllegalArgumentException("Duplicate mine position: " + position);
            }
            mines[row][column] = true;
        }

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int column = 0; column < BOARD_SIZE; column++) {
                cells[row][column] = new Cell(mines[row][column],
                        countAdjacentMines(mines, row, column));
            }
        }
    }

    /** Fixed row-major positions (0..99) for tests in this package. */
    static GameBoard fromMinePositions(int... positions) {
        return new GameBoard(positions);
    }

    public int getSize() {
        return BOARD_SIZE;
    }

    public int getMineCount() {
        return MINE_COUNT;
    }

    public Cell getCell(int row, int column) {
        if (row < 0 || row >= BOARD_SIZE || column < 0 || column >= BOARD_SIZE) {
            throw new IndexOutOfBoundsException("Cell outside board: " + row + ", " + column);
        }
        return cells[row][column];
    }

    /** Returns true only when this dig changes the game. */
    public boolean dig(int row, int column) {
        Cell cell = getCell(row, column);
        if (lost || cell.isRevealed() || cell.isFlagged()) {
            return false;
        }
        if (cell.isMine()) {
            lost = true;
            detonatedMinePosition = row * BOARD_SIZE + column;
            // Showing the board at game end must not count as successful digs.
            for (Cell[] boardRow : cells) {
                for (Cell boardCell : boardRow) {
                    boardCell.reveal();
                }
            }
        } else {
            cell.reveal();
            revealedSafeCount++;
        }
        return true;
    }

    public boolean isLost() {
        return lost;
    }

    public int getRevealedSafeCount() {
        return revealedSafeCount;
    }

    public int getDetonatedMinePosition() {
        return detonatedMinePosition;
    }

    private static int[] randomMinePositions(Random random) {
        int[] available = new int[CELL_COUNT];
        for (int i = 0; i < CELL_COUNT; i++) {
            available[i] = i;
        }
        int[] selected = new int[MINE_COUNT];
        // Partial shuffle: each pick removes one position from the available pool.
        // This guarantees six distinct mines without retrying duplicate picks.
        for (int i = 0; i < MINE_COUNT; i++) {
            int chosen = i + random.nextInt(CELL_COUNT - i);
            selected[i] = available[chosen];
            available[chosen] = available[i];
            available[i] = selected[i];
        }
        return selected;
    }

    private static int countAdjacentMines(boolean[][] mines, int row, int column) {
        int count = 0;
        for (int neighborRow = Math.max(0, row - 1);
                neighborRow <= Math.min(BOARD_SIZE - 1, row + 1); neighborRow++) {
            for (int neighborColumn = Math.max(0, column - 1);
                    neighborColumn <= Math.min(BOARD_SIZE - 1, column + 1); neighborColumn++) {
                if (neighborRow == row && neighborColumn == column) {
                    continue;
                }
                if (mines[neighborRow][neighborColumn]) {
                    count++;
                }
            }
        }
        return count;
    }
}
