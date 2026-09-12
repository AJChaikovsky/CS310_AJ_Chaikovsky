package com.example.minesweeper;

import android.app.Activity;
import android.graphics.Insets;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.View;
import android.view.WindowInsets;
import android.widget.GridLayout;
import android.widget.Button;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Random;


public class MainActivity extends Activity {
    private static final String BOARD_SEED_KEY = "boardSeed";
    private static final String DUG_CELLS_KEY = "dugCells";
    private static final String ACTIONS_KEY = "boardActions";
    private static final String TIMER_START_KEY = "timerStart";
    private static final String TIMER_STOP_KEY = "timerStop";
    private static final String RESULTS_VISIBLE_KEY = "resultsVisible";
    private static final int MODE_TOGGLE_ACTION = -1;
    private GameBoard gameBoard;
    private long boardSeed;
    private ArrayList<Integer> boardActions;
    private TextView[][] cellViews;
    private GameTimer gameTimer;
    private boolean resultsVisible;
    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private final Runnable timerTick = this::updateTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        boardSeed = savedInstanceState != null && savedInstanceState.containsKey(BOARD_SEED_KEY)
                ? savedInstanceState.getLong(BOARD_SEED_KEY) : new Random().nextLong();
        gameBoard = new GameBoard(new Random(boardSeed));
        boardActions = savedInstanceState == null ? null
                : savedInstanceState.getIntegerArrayList(ACTIONS_KEY);
        // A saved Milestone 3 game contains only digs, all in the default dig mode.
        if (boardActions == null && savedInstanceState != null) {
            boardActions = savedInstanceState.getIntegerArrayList(DUG_CELLS_KEY);
        }
        if (boardActions == null) {
            boardActions = new ArrayList<>();
        }
        // Replay mode changes and accepted taps in order to restore the whole game.
        for (int action : boardActions) {
            if (action == MODE_TOGGLE_ACTION) {
                gameBoard.toggleMode();
            } else {
                gameBoard.tap(action / gameBoard.getSize(), action % gameBoard.getSize());
            }
        }
        if (savedInstanceState != null && savedInstanceState.containsKey(TIMER_START_KEY)) {
            gameTimer = new GameTimer(SystemClock::elapsedRealtime,
                    savedInstanceState.getLong(TIMER_START_KEY),
                    savedInstanceState.getLong(TIMER_STOP_KEY, -1));
        } else {
            gameTimer = new GameTimer(SystemClock::elapsedRealtime);
        }
        if (gameBoard.isGameOver()) {
            gameTimer.stop();
        }
        findViewById(R.id.mode_indicator).setOnClickListener(view -> switchMode());
        findViewById(R.id.play_again).setOnClickListener(view -> startNewGame());
        findViewById(R.id.finished_screen_target).setOnClickListener(
                view -> setResultsVisible(true));
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
        setResultsVisible(gameBoard.isGameOver() && savedInstanceState != null
                && savedInstanceState.getBoolean(RESULTS_VISIBLE_KEY));
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
                cell.setOnClickListener(view -> tapCell(cellRow, cellColumn));
                cellViews[row][column] = cell;
                board.addView(cell);
            }
        }
    }

    private void tapCell(int row, int column) {
        // The game-ending tap only reveals the board. A later tap opens results.
        if (gameBoard.isGameOver()) {
            setResultsVisible(true);
            return;
        }
        if (gameBoard.tap(row, column)) {
            boardActions.add(row * gameBoard.getSize() + column);
            if (gameBoard.isGameOver()) {
                gameTimer.stop();
                updateTimer();
            }
            renderBoard();
        }
    }

    private void switchMode() {
        if (gameBoard.toggleMode()) {
            boardActions.add(MODE_TOGGLE_ACTION);
            renderBoard();
        }
    }

    private void startNewGame() {
        boardSeed = new Random().nextLong();
        gameBoard = new GameBoard(new Random(boardSeed));
        boardActions.clear();
        gameTimer = new GameTimer(SystemClock::elapsedRealtime);
        setResultsVisible(false);
        updateTimer();
        renderBoard();
        findViewById(R.id.game_page).scrollTo(0, 0);
    }

    private void setResultsVisible(boolean visible) {
        resultsVisible = visible;
        if (visible) {
            TextView outcome = findViewById(R.id.result_outcome);
            outcome.setText(gameBoard.isWon() ? R.string.result_won : R.string.result_lost);
            outcome.setTextColor(getColor(gameBoard.isWon() ? R.color.ink : R.color.flag_red));
            long seconds = gameTimer.elapsedMillis() / 1000;
            TextView time = findViewById(R.id.result_time);
            time.setText(getString(R.string.elapsed_time, seconds));
            time.setContentDescription(getResources().getQuantityString(
                    R.plurals.elapsed_time_description,
                    (int) Math.min(seconds, Integer.MAX_VALUE), seconds));
        }
        findViewById(R.id.game_page).setVisibility(visible ? View.GONE : View.VISIBLE);
        findViewById(R.id.results_page).setVisibility(visible ? View.VISIBLE : View.GONE);
        findViewById(R.id.finished_screen_target).setVisibility(
                gameBoard.isGameOver() && !visible ? View.VISIBLE : View.GONE);
    }

    private void renderBoard() {
        for (int row = 0; row < gameBoard.getSize(); row++) {
            for (int column = 0; column < gameBoard.getSize(); column++) {
                Cell state = gameBoard.getCell(row, column);
                TextView view = cellViews[row][column];
                boolean lightSquare = (row + column) % 2 == 0;
                view.setEnabled(gameBoard.isGameOver() || !state.isRevealed());
                if (!state.isRevealed()) {
                    view.setText(state.isFlagged() ? getString(R.string.flag_symbol) : "");
                    view.setTextColor(getColor(state.isFlagged() ? R.color.flag_red : R.color.ink));
                    view.setBackgroundColor(getColor(lightSquare
                            ? R.color.cell_light : R.color.cell_dark));
                    view.setContentDescription(getString(state.isFlagged()
                            ? R.string.flagged_cell_description
                            : R.string.hidden_cell_description, row + 1, column + 1));
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
                if (gameBoard.isGameOver()) {
                    view.setContentDescription(getString(R.string.finished_cell_description,
                            view.getContentDescription()));
                }
            }
        }
        int remaining = gameBoard.getRemainingMines();
        TextView mineCount = findViewById(R.id.mine_count);
        mineCount.setText(getString(R.string.mine_count_value, remaining));
        mineCount.setContentDescription(getResources().getQuantityString(
                R.plurals.mines_remaining_description, remaining, remaining));
        Button modeButton = findViewById(R.id.mode_indicator);
        boolean flagMode = gameBoard.isFlagMode();
        modeButton.setText(flagMode ? R.string.flag_mode : R.string.dig_mode);
        modeButton.setCompoundDrawablesRelativeWithIntrinsicBounds(
                flagMode ? R.drawable.ic_flag : R.drawable.ic_dig, 0, 0, 0);
        modeButton.setContentDescription(getString(flagMode
                ? R.string.flag_mode_description : R.string.dig_mode_description));
        modeButton.setEnabled(!gameBoard.isGameOver());
        TextView status = findViewById(R.id.game_status);
        status.setVisibility(gameBoard.isGameOver() ? View.VISIBLE : View.GONE);
        status.setText(gameBoard.isWon() ? R.string.game_won : R.string.game_lost);
        findViewById(R.id.results_hint).setVisibility(
                gameBoard.isGameOver() ? View.VISIBLE : View.GONE);
        // This target appears after the game-ending click has been delivered.
        // It catches the next tap anywhere without passing it through to Play Again.
        findViewById(R.id.finished_screen_target).setVisibility(
                gameBoard.isGameOver() && !resultsVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateTimer();
    }

    @Override
    protected void onStop() {
        timerHandler.removeCallbacks(timerTick);
        super.onStop();
    }

    private void updateTimer() {
        // Each activity owns one callback; elapsed time continues while backgrounded.
        timerHandler.removeCallbacks(timerTick);
        long elapsedMillis = gameTimer.elapsedMillis();
        long seconds = elapsedMillis / 1000;
        TextView timer = findViewById(R.id.timer);
        timer.setText(getString(R.string.elapsed_time, seconds));
        timer.setContentDescription(getResources().getQuantityString(
                R.plurals.elapsed_time_description,
                (int) Math.min(seconds, Integer.MAX_VALUE), seconds));
        if (!gameBoard.isGameOver()) {
            timerHandler.postDelayed(timerTick, 1000 - elapsedMillis % 1000);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Recreate the same mine layout after rotation instead of reshuffling it.
        outState.putLong(BOARD_SEED_KEY, boardSeed);
        outState.putIntegerArrayList(ACTIONS_KEY, new ArrayList<>(boardActions));
        outState.putLong(TIMER_START_KEY, gameTimer.getStartedAtMillis());
        outState.putLong(TIMER_STOP_KEY, gameTimer.getStoppedElapsedMillis());
        outState.putBoolean(RESULTS_VISIBLE_KEY, resultsVisible);
    }
}
