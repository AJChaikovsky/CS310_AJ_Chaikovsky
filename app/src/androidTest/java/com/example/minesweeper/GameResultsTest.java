package com.example.minesweeper;

import android.os.SystemClock;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class GameResultsTest {
    @Test
    public void nextTouchAnywhereOpensResultsWithoutRestarting() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            for (int target : new int[]{R.id.timer, R.id.mode_indicator, R.id.main}) {
                AtomicReference<GameBoard> finished = new AtomicReference<>();
                AtomicReference<String> frozen = new AtomicReference<>();
                scenario.onActivity(activity -> touch(activity,
                        cell(activity, firstMine(board(activity))), false));
                InstrumentationRegistry.getInstrumentation().waitForIdleSync();
                scenario.onActivity(activity -> {
                    assertFinishedBoard(activity);
                    finished.set(board(activity));
                    frozen.set(text(activity, R.id.timer));
                    touch(activity, activity.findViewById(target), target == R.id.main);
                });
                InstrumentationRegistry.getInstrumentation().waitForIdleSync();
                scenario.onActivity(activity -> {
                    assertResults(activity, R.string.result_lost, frozen.get());
                    assertSame(finished.get(), board(activity));
                    activity.findViewById(R.id.play_again).performClick();
                    assertFreshGame(activity);
                });
            }
        }
    }

    @Test
    public void lossWaitsForAnotherTapAndPreservesResultsAcrossRecreation() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            SystemClock.sleep(1200);
            AtomicReference<String> frozen = new AtomicReference<>();
            scenario.onActivity(activity -> {
                clickMine(activity);
                assertTrue(board(activity).isLost());
                assertFinishedBoard(activity);
                frozen.set(text(activity, R.id.timer));
                assertNotEquals("0 s", frozen.get());
            });
            scenario.recreate();
            scenario.onActivity(activity -> {
                assertFinishedBoard(activity);
                assertEquals(frozen.get(), text(activity, R.id.timer));
                cell(activity, 0).performClick();
                assertResults(activity, R.string.result_lost, frozen.get());
            });
            SystemClock.sleep(1200);
            scenario.recreate();
            scenario.onActivity(activity -> assertResults(
                    activity, R.string.result_lost, frozen.get()));
        }
    }

    @Test
    public void winWaitsForAnotherTapAndRestartClearsFlagsAndElapsedTime() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                GameBoard game = board(activity);
                int mine = firstMine(game);
                activity.findViewById(R.id.mode_indicator).performClick();
                cell(activity, mine).performClick();
                activity.findViewById(R.id.mode_indicator).performClick();
                assertEquals("5", text(activity, R.id.mine_count));
                for (int position = 0; position < GameBoard.CELL_COUNT; position++) {
                    Cell state = game.getCell(position / 10, position % 10);
                    if (!state.isMine() && !state.isRevealed()) cell(activity, position).performClick();
                }
                assertTrue(game.isWon());
                assertEquals(94, game.getRevealedSafeCount());
                assertFinishedBoard(activity);
                String frozen = text(activity, R.id.timer);
                // Even the previously flagged mine can open results after game end.
                cell(activity, mine).performClick();
                assertResults(activity, R.string.result_won, frozen);
                activity.findViewById(R.id.play_again).performClick();
                assertNotSame(game, board(activity));
                assertFreshGame(activity);
                assertEquals("0 s", text(activity, R.id.timer));
            });
            scenario.recreate();
            scenario.onActivity(GameResultsTest::assertFreshGame);
        }
    }

    @Test
    public void repeatedRestartsReuseActivityAndKeepOnlyOneBoard() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                for (int game = 0; game < 3; game++) {
                    clickMine(activity);
                    assertFinishedBoard(activity);
                    cell(activity, 99).performClick();
                    assertResults(activity, R.string.result_lost, text(activity, R.id.timer));
                    activity.findViewById(R.id.play_again).performClick();
                    assertFreshGame(activity);
                    assertEquals(100, ((GridLayout) activity.findViewById(R.id.board)).getChildCount());
                }
            });
        }
    }

    private static void assertFinishedBoard(MainActivity activity) {
        assertEquals(View.VISIBLE, activity.findViewById(R.id.game_page).getVisibility());
        assertEquals(View.GONE, activity.findViewById(R.id.results_page).getVisibility());
        assertTrue(activity.findViewById(R.id.results_hint).isShown());
        assertTrue(activity.findViewById(R.id.finished_screen_target).isShown());
        assertFalse(activity.findViewById(R.id.mode_indicator).isEnabled());
        for (int p = 0; p < 100; p++) {
            assertTrue(board(activity).getCell(p / 10, p % 10).isRevealed());
            assertTrue(cell(activity, p).isEnabled());
        }
    }

    private static void assertResults(MainActivity activity, int outcome, String elapsed) {
        assertEquals(View.GONE, activity.findViewById(R.id.game_page).getVisibility());
        assertTrue(activity.findViewById(R.id.results_page).isShown());
        assertEquals(activity.getString(outcome), text(activity, R.id.result_outcome));
        assertEquals(elapsed, text(activity, R.id.result_time));
        assertTrue(activity.findViewById(R.id.play_again).isShown());
        assertEquals(View.GONE, activity.findViewById(R.id.finished_screen_target).getVisibility());
    }

    private static void assertFreshGame(MainActivity activity) {
        GameBoard game = board(activity);
        assertFalse(game.isGameOver());
        assertFalse(game.isFlagMode());
        assertEquals(0, game.getFlagCount());
        assertEquals(0, game.getRevealedSafeCount());
        assertEquals("6", text(activity, R.id.mine_count));
        assertEquals(View.VISIBLE, activity.findViewById(R.id.game_page).getVisibility());
        assertEquals(View.GONE, activity.findViewById(R.id.results_page).getVisibility());
        assertEquals(View.GONE, activity.findViewById(R.id.game_status).getVisibility());
        assertEquals(View.GONE, activity.findViewById(R.id.finished_screen_target).getVisibility());
        assertTrue(activity.findViewById(R.id.mode_indicator).isEnabled());
        int mines = 0;
        for (int p = 0; p < 100; p++) {
            Cell state = game.getCell(p / 10, p % 10);
            assertFalse(state.isRevealed());
            assertFalse(state.isFlagged());
            assertTrue(cell(activity, p).isEnabled());
            if (state.isMine()) mines++;
        }
        assertEquals(6, mines);
    }

    private static void clickMine(MainActivity activity) {
        cell(activity, firstMine(board(activity))).performClick();
    }

    private static void touch(MainActivity activity, View target, boolean blankBottom) {
        int[] origin = new int[2];
        target.getLocationInWindow(origin);
        float x = origin[0] + target.getWidth() / 2f;
        float y = origin[1] + (blankBottom
                ? target.getHeight() - target.getPaddingBottom() - 12
                : target.getHeight() / 2f);
        long now = SystemClock.uptimeMillis();
        MotionEvent down = MotionEvent.obtain(now, now, MotionEvent.ACTION_DOWN, x, y, 0);
        MotionEvent up = MotionEvent.obtain(now, now + 50, MotionEvent.ACTION_UP, x, y, 0);
        down.setSource(InputDevice.SOURCE_TOUCHSCREEN);
        up.setSource(InputDevice.SOURCE_TOUCHSCREEN);
        try {
            activity.dispatchTouchEvent(down);
            activity.dispatchTouchEvent(up);
        } finally {
            down.recycle();
            up.recycle();
        }
    }

    private static int firstMine(GameBoard game) {
        for (int p = 0; p < 100; p++) if (game.getCell(p / 10, p % 10).isMine()) return p;
        throw new AssertionError("No mine in board");
    }

    private static View cell(MainActivity activity, int position) {
        return ((GridLayout) activity.findViewById(R.id.board)).getChildAt(position);
    }

    private static String text(MainActivity activity, int id) {
        return ((TextView) activity.findViewById(id)).getText().toString();
    }

    private static GameBoard board(MainActivity activity) {
        // Inspect the real random layout so both outcomes can be exercised reliably.
        // All interactions still go through the activity's cell and button listeners.
        try {
            Field field = MainActivity.class.getDeclaredField("gameBoard");
            field.setAccessible(true);
            return (GameBoard) field.get(activity);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }
}
