package com.example.minesweeper;

import android.app.Activity;
import android.graphics.Insets;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.widget.GridLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Random;


public class MainActivity extends Activity {
    private static final String BOARD_SEED_KEY = "boardSeed";
    private static final String DUG_CELLS_KEY = "dugCells";
    private GameBoard gameBoard;
    private long boardSeed;
    private ArrayList<Integer> dugCells;
    private TextView[][] cellViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        boardSeed = savedInstanceState != null && savedInstanceState.containsKey(BOARD_SEED_KEY)
                ? savedInstanceState.getLong(BOARD_SEED_KEY) : new Random().nextLong();
        gameBoard = new GameBoard(new Random(boardSeed));
        dugCells = savedInstanceState == null ? null
                : savedInstanceState.getIntegerArrayList(DUG_CELLS_KEY);
        if (dugCells == null) {
            dugCells = new ArrayList<>();
        }
        // Replaying accepted digs restores revealed cells and loss after rotation.
        for (int position : dugCells) {
            gameBoard.dig(position / gameBoard.getSize(), position % gameBoard.getSize());
        }
        TextView mineCount = findViewById(R.id.mine_count);
        mineCount.setText(getString(R.string.mine_count_value, gameBoard.getMineCount()));
        mineCount.setContentDescription(getString(
                R.string.mines_remaining_description, gameBoard.getMineCount()));
        // Keep content clear of system bars and cutouts on edge-to-edge devices.
        View main = findViewById(R.id.main);
        main.setOnApplyWindowInsetsListener((view, windowInsets) -> {
            Insets bars = windowInsets.getInsets(
                    WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return windowInsets;
        });
        main.requestApplyInsets();
        createBoardViews();
        renderBoard();
    }

    private void createBoardViews() {
        GridLayout board = findViewById(R.id.board);
        board.setRowCount(gameBoard.getSize());
        board.setColumnCount(gameBoard.getSize());
        cellViews = new TextView[gameBoard.getSize()][gameBoard.getSize()];
        for (int row = 0; row < gameBoard.getSize(); row++) {
            for (int column = 0; column < gameBoard.getSize(); column++) {
                TextView cell = new TextView(this);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                        GridLayout.spec(row, 1, GridLayout.FILL, 1f),
                        GridLayout.spec(column, 1, GridLayout.FILL, 1f));
                params.width = 0;
                params.height = 0;
                cell.setLayoutParams(params);
                cell.setBackgroundColor(getColor((row + column) % 2 == 0
                        ? R.color.cell_light : R.color.cell_dark));
                cell.setGravity(android.view.Gravity.CENTER);
                cell.setTextColor(getColor(R.color.ink));
                cell.setTextSize(18);
                cell.setTypeface(Typeface.DEFAULT_BOLD);
                cell.setFocusable(true);
                cell.setContentDescription(getString(
                        R.string.hidden_cell_description, row + 1, column + 1));

                final int cellRow = row;
                final int cellColumn = column;
                cell.setOnClickListener(view -> digCell(cellRow, cellColumn));
                cellViews[row][column] = cell;
                board.addView(cell);
            }
        }
    }

    private void digCell(int row, int column) {
        if (gameBoard.dig(row, column)) {
            dugCells.add(row * gameBoard.getSize() + column);
            renderBoard();
        }
    }

    private void renderBoard() {
        for (int row = 0; row < gameBoard.getSize(); row++) {
            for (int column = 0; column < gameBoard.getSize(); column++) {
                Cell state = gameBoard.getCell(row, column);
                TextView view = cellViews[row][column];
                boolean lightSquare = (row + column) % 2 == 0;
                view.setEnabled(!gameBoard.isLost() && !state.isRevealed());
                if (!state.isRevealed()) {
                    view.setText("");
                    view.setBackgroundColor(getColor(lightSquare
                            ? R.color.cell_light : R.color.cell_dark));
                    view.setContentDescription(getString(
                            R.string.hidden_cell_description, row + 1, column + 1));
                } else if (state.isMine()) {
                    view.setText(R.string.mine_symbol);
                    view.setTextColor(getColor(R.color.mine_ink));
                    boolean detonated = row * gameBoard.getSize() + column
                            == gameBoard.getDetonatedMinePosition();
                    view.setBackgroundColor(getColor(detonated
                            ? R.color.detonated_mine : R.color.revealed_mine));
                    view.setContentDescription(getString(detonated
                            ? R.string.detonated_cell_description
                            : R.string.mine_cell_description, row + 1, column + 1));
                } else {
                    int adjacent = state.getAdjacentMines();
                    view.setText(getString(R.string.cell_count, adjacent));
                    view.setTextColor(getColor(R.color.ink));
                    view.setBackgroundColor(getColor(lightSquare
                            ? R.color.revealed_light : R.color.revealed_dark));
                    view.setContentDescription(getResources().getQuantityString(
                            R.plurals.revealed_cell_description, adjacent,
                            row + 1, column + 1, adjacent));
                }
            }
        }
        TextView status = findViewById(R.id.game_status);
        status.setVisibility(gameBoard.isLost() ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Recreate the same mine layout after rotation instead of reshuffling it.
        outState.putLong(BOARD_SEED_KEY, boardSeed);
        outState.putIntegerArrayList(DUG_CELLS_KEY, new ArrayList<>(dugCells));
    }
}
