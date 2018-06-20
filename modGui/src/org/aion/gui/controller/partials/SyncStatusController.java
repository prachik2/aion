package org.aion.gui.controller.partials;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.aion.gui.controller.AbstractController;
import org.aion.gui.model.KernelConnection;
import org.aion.gui.events.RefreshEvent;
import org.aion.gui.model.dto.SyncInfoDTO;
import org.aion.gui.util.SyncStatusFormatter;

import java.net.URL;
import java.util.ResourceBundle;

public class SyncStatusController extends AbstractController {

    private final KernelConnection kernel;

    @FXML
    private Label progressBarLabel;

    public SyncStatusController(KernelConnection kernelConnection) {
        this.kernel = kernelConnection;
    }

    @Override
    protected void internalInit(URL location, ResourceBundle resources) {
    }

    @Override
    protected final void refreshView(final RefreshEvent event) {
        if (RefreshEvent.Type.TIMER.equals(event.getType())) {
            final Task<SyncInfoDTO> getSyncInfoTask = getApiTask(o -> kernel.getSyncInfo(), null);
            runApiTask(
                    getSyncInfoTask,
                    evt -> setSyncStatus(getSyncInfoTask.getValue()),
                    getErrorEvent(throwable -> {}, getSyncInfoTask),
                    getEmptyEvent()
            );
        }
    }

    private void setSyncStatus(SyncInfoDTO syncInfo) {
        progressBarLabel.setText(SyncStatusFormatter.formatSyncStatusByBlockNumbers(syncInfo));
    }
}
