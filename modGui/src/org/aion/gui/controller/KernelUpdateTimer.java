package org.aion.gui.controller;

import org.aion.gui.util.DataUpdater;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class KernelUpdateTimer {
    private final ScheduledExecutorService timer;

    public KernelUpdateTimer(ScheduledExecutorService timer) {
        this.timer = timer;
    }

    public void fireImmediatelyAndThenStart() {
        new DataUpdater().run();
        start();
    }

    public void fireImmediatelyAndThenStop() {
        new DataUpdater().run();
        stop();
    }

    public void start() {
        timer.scheduleAtFixedRate(
                new DataUpdater(),
                org.aion.wallet.util.AionConstants.BLOCK_MINING_TIME_MILLIS,
                3 * org.aion.wallet.util.AionConstants.BLOCK_MINING_TIME_MILLIS,
                TimeUnit.MILLISECONDS
        );
    }

    public void stop() {
        timer.shutdown();
    }
}
