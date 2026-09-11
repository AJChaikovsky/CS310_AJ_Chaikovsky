package com.example.minesweeper;

/** The game data for one square, independent of its Android view. */
public final class Cell {
    private final boolean mine;
    private final int adjacentMines;
    private boolean revealed;
    private boolean flagged;

    Cell(boolean mine, int adjacentMines) {
        this.mine = mine;
        this.adjacentMines = adjacentMines;
        revealed = false;
        flagged = false;
    }

    public boolean isMine() {
        return mine;
    }

    public int getAdjacentMines() {
        return adjacentMines;
    }

    public boolean isRevealed() {
        return revealed;
    }

    void reveal() {
        revealed = true;
    }

    public boolean isFlagged() {
        return flagged;
    }
}
