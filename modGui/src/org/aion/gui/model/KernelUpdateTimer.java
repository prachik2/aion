package org.aion.gui.model;

import org.aion.gui.util.AionConstants;
import org.aion.gui.util.DataUpdater;
import org.aion.log.AionLoggerFactory;
import org.slf4j.Logger;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class KernelUpdateTimer {
    private final ScheduledExecutorService timer;
    private ScheduledFuture<?> execution;
    private static final Logger LOG = AionLoggerFactory.getLogger(org.aion.log.LogEnum.GUI.name());

    public KernelUpdateTimer(ScheduledExecutorService timer) {
        this.timer = timer;
    }

    public void fireImmediatelyAndThenStart() {
        new DataUpdater().run();
        start();
    }

    public void start() {
        LOG.info("Started timer");
        if(execution == null) {
            execution = timer.scheduleAtFixedRate(
                    new DataUpdater(),
                    AionConstants.BLOCK_MINING_TIME_MILLIS,
//                    3 * AionConstants.BLOCK_MINING_TIME_MILLIS,
                    AionConstants.BLOCK_MINING_TIME_MILLIS,
                    TimeUnit.MILLISECONDS
            );
        }
    }

    public void stop() {
        LOG.info("Stopped timer");
        if(execution != null) {
            execution.cancel(true);
        }
        execution = null;
    }
}
