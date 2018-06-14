package org.aion.wallet.util;

import com.google.common.eventbus.EventBus;
import javafx.application.Platform;
import org.aion.gui.events.EventBusFactory;
import org.aion.gui.events.RefreshEvent;

import java.util.TimerTask;

public class DataUpdater extends TimerTask {

    public static final String UI_DATA_REFRESH = "gui.data_refresh";

    private final EventBus eventBus = EventBusFactory.getBus(UI_DATA_REFRESH);

    @Override
    public void run() {
        Platform.runLater(() -> eventBus.post(new RefreshEvent(RefreshEvent.Type.TIMER)));
    }
}
