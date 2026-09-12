package com.example.minesweeper;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class GameTimerTest {
    private long now = 100_000;

    @Test
    public void startsAtZeroAndUsesElapsedTimeWithoutCallbacks() {
        GameTimer timer = new GameTimer(() -> now);
        assertEquals(0, timer.elapsedMillis());
        now += 12_345;
        assertEquals(12_345, timer.elapsedMillis());
    }

    @Test
    public void stopFreezesDurationEvenWhenCalledAgain() {
        GameTimer timer = new GameTimer(() -> now);
        now += 2_999;
        timer.stop();
        now += 20_000;
        timer.stop();
        assertEquals(2_999, timer.elapsedMillis());
    }

    @Test
    public void restoringRunningTimerIncludesTimeAway() {
        GameTimer timer = new GameTimer(() -> now);
        now += 3_250;
        long start = timer.getStartedAtMillis();
        long stop = timer.getStoppedElapsedMillis();
        now += 8_000;
        GameTimer restored = new GameTimer(() -> now, start, stop);
        assertEquals(11_250, restored.elapsedMillis());
    }

    @Test
    public void restoringFinishedTimerPreservesFrozenDuration() {
        GameTimer timer = new GameTimer(() -> now);
        now += 4_567;
        timer.stop();
        now += 60_000;
        GameTimer restored = new GameTimer(() -> now,
                timer.getStartedAtMillis(), timer.getStoppedElapsedMillis());
        restored.stop();
        assertEquals(4_567, restored.elapsedMillis());
    }

    @Test
    public void newGameStartsAtZeroAfterFinishedGame() {
        GameTimer first = new GameTimer(() -> now);
        now += 9_000;
        first.stop();
        GameTimer next = new GameTimer(() -> now);
        assertEquals(0, next.elapsedMillis());
        now += 1_200;
        assertEquals(1_200, next.elapsedMillis());
        assertEquals(9_000, first.elapsedMillis());
    }
}
