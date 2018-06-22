package org.aion.gui.controller.partials;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.aion.gui.controller.AbstractController;
import org.aion.gui.events.RefreshEvent;
import org.aion.gui.model.KernelConnection;
import org.aion.gui.model.dto.SyncInfoDto;
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
        // FIXME put me back

//        if (RefreshEvent.Type.TIMER.equals(event.getType())) {
//            final Task<SyncInfoDto> getSyncInfoTask = getApiTask(o -> kernel.getSyncInfo(), null);
//            runApiTask(
//                    getSyncInfoTask,
//                    evt -> setSyncStatus(getSyncInfoTask.getValue()),
//                    getErrorEvent(throwable -> {}, getSyncInfoTask),
//                    getEmptyEvent()
//            );
//        }
    }

    private void setSyncStatus(SyncInfoDto syncInfo) {
        progressBarLabel.setText(SyncStatusFormatter.formatSyncStatusByBlockNumbers(syncInfo));
    }
}
