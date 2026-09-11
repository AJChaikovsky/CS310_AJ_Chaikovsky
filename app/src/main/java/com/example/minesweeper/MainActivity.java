package com.example.minesweeper;

import android.app.Activity;
import android.graphics.Insets;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.widget.GridLayout;
import android.widget.TextView;
import java.util.Random;


public class MainActivity extends Activity {
    private static final String BOARD_SEED_KEY = "boardSeed";
    private GameBoard gameBoard;
    private long boardSeed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        boardSeed = savedInstanceState != null && savedInstanceState.containsKey(BOARD_SEED_KEY)
                ? savedInstanceState.getLong(BOARD_SEED_KEY) : new Random().nextLong();
        gameBoard = new GameBoard(new Random(boardSeed));
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
    }

    private void createBoardViews() {
        GridLayout board = findViewById(R.id.board);
        board.setRowCount(gameBoard.getSize());
        board.setColumnCount(gameBoard.getSize());
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
                cell.setContentDescription(getString(
                        R.string.hidden_cell_description, row + 1, column + 1));

                board.addView(cell);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Recreate the same mine layout after rotation instead of reshuffling it.
        outState.putLong(BOARD_SEED_KEY, boardSeed);
    }
}
