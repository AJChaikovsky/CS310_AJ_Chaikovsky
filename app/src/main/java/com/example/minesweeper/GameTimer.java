package com.example.minesweeper;

import java.util.function.LongSupplier;

/** Elapsed duration from a monotonic clock, independent of UI refresh frequency. */
final class GameTimer {
    private final LongSupplier clock;
    private final long startedAtMillis;
    private long stoppedElapsedMillis;

    GameTimer(LongSupplier clock) {
        this(clock, clock.getAsLong(), -1);
    }

    GameTimer(LongSupplier clock, long startedAtMillis, long stoppedElapsedMillis) {
        this.clock = clock;
        this.startedAtMillis = startedAtMillis;
        this.stoppedElapsedMillis = stoppedElapsedMillis;
    }

    long elapsedMillis() {
        return stoppedElapsedMillis >= 0 ? stoppedElapsedMillis
                : Math.max(0, clock.getAsLong() - startedAtMillis);
    }

    void stop() {
        stoppedElapsedMillis = elapsedMillis();
    }

    long getStartedAtMillis() {
        return startedAtMillis;
    }

    long getStoppedElapsedMillis() {
        return stoppedElapsedMillis;
    }
}
