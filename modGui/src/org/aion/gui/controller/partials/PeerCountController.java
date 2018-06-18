package org.aion.gui.controller.partials;

//import com.google.common.eventbus.Subscribe;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.aion.gui.controller.AbstractController;
import org.aion.gui.events.RefreshEvent;
import org.aion.gui.controller.KernelConnection;

import java.net.URL;
import java.util.ResourceBundle;

public class PeerCountController extends AbstractController {
    @FXML
    private Label peerCount;

    private final KernelConnection kernel;

    public PeerCountController(KernelConnection kernelConnection) {
        this.kernel = kernelConnection;
    }

    @Override
    public void internalInit(final URL location, final ResourceBundle resources) {
    }

    @Override
    protected final void refreshView(final RefreshEvent event) {
        if (RefreshEvent.Type.TIMER.equals(event.getType())) {
            final Task<Integer> getPeerCountTask = getApiTask(o -> kernel.getPeerCount(), null);
            runApiTask(
                    getPeerCountTask,
                    evt -> setPeerCount(getPeerCountTask.getValue()),
                    getErrorEvent(throwable -> {}, getPeerCountTask),
                    getEmptyEvent()
            );
        }
    }

    private void setPeerCount(int numberOfPeers) {
        if(numberOfPeers == 1) {
            peerCount.setText(numberOfPeers + " peer");
            return;
        }
        peerCount.setText(numberOfPeers + " peers");
    }
}
