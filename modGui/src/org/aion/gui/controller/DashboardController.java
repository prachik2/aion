package org.aion.gui.controller;

import com.google.common.eventbus.Subscribe;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import org.aion.gui.events.EventBusRegistry;
import org.aion.gui.events.HeaderPaneButtonEvent;
import org.aion.gui.events.KernelProcEvent;
import org.aion.gui.events.RefreshEvent;
import org.aion.gui.model.GeneralKernelInfoRetriever;
import org.aion.gui.model.dto.AccountDTO;
import org.aion.gui.model.KernelConnection;
import org.aion.gui.model.KernelUpdateTimer;
import org.aion.gui.model.dto.SyncInfoDTO2;
import org.aion.gui.util.DataUpdater;
import org.aion.gui.util.SyncStatusFormatter;
import org.aion.log.AionLoggerFactory;
import org.aion.os.KernelLauncher;
import org.slf4j.Logger;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class DashboardController extends AbstractController {
    private final KernelLauncher kernelLauncher;
    private final KernelConnection kernelConnection;
    private final KernelUpdateTimer kernelUpdateTimer;

    private final GeneralKernelInfoRetriever generalKernelInfoRetriever;
    private final SyncInfoDTO2 syncInfoDTO2;

    @FXML private Button launchKernelButton;
    @FXML private Button terminateKernelButton;

    // These should probably be in their own classes
    @FXML private Label kernelStatusLabel;
    @FXML private Label numPeersLabel;
    @FXML private Label isMining;
    @FXML private Label blocksLabel;


    private static final Logger LOG = AionLoggerFactory.getLogger(org.aion.log.LogEnum.GUI.name());

    public DashboardController(KernelLauncher kernelLauncher,
                               KernelConnection kernelConnection,
                               KernelUpdateTimer kernelUpdateTimer,
                               GeneralKernelInfoRetriever generalKernelInfoRetriever,
                               SyncInfoDTO2 syncInfoDTO2) {
        this.kernelLauncher = kernelLauncher;
        this.kernelConnection = kernelConnection;
        this.kernelUpdateTimer = kernelUpdateTimer;
        this.generalKernelInfoRetriever = generalKernelInfoRetriever;
        this.syncInfoDTO2 = syncInfoDTO2;
    }

    @Override
    public void internalInit(final URL location, final ResourceBundle resources) {
    }

    @Override
    protected void registerEventBusConsumer() {
        EventBusRegistry.INSTANCE.getBus(EventBusRegistry.KERNEL_BUS).register(this);
        EventBusRegistry.INSTANCE.getBus(DataUpdater.UI_DATA_REFRESH).register(this);
    }

    // -- Handlers for Events coming from Model ---------------------------------------------------
    @Subscribe
    private void handleAccountChanged(final AccountDTO account) {
        LOG.warn("Implement me!");
    }

    @Subscribe
    private void handleHeaderPaneButtonEvent(final HeaderPaneButtonEvent event) {
        LOG.warn("Implement me!");
    }

    @Subscribe
    private void handleRefreshEvent(final RefreshEvent event){
        LOG.warn("Implement me!");
    }

    @Subscribe
    private void handleUiTimerTick(RefreshEvent event) {
        LOG.trace("handleUiTimerTick");
        if (RefreshEvent.Type.TIMER.equals(event.getType())) {
            // peer count
            final Task<Optional<Integer>> getPeerCountTask = getApiTask(o -> generalKernelInfoRetriever.getPeerCount(), null);
            runApiTask(
                    getPeerCountTask,
                    evt -> numPeersLabel.setText(displayStringOfOptional(getPeerCountTask.getValue())),
                    getErrorEvent(throwable -> {}, getPeerCountTask),
                    getEmptyEvent()
            );
            // sync status
            Task<Void> getSyncInfoTask = getApiTask(o -> syncInfoDTO2.loadFromApi(), null);
            runApiTask(
                    getSyncInfoTask,
                    evt -> blocksLabel.setText(String.valueOf(SyncStatusFormatter.formatSyncStatusByBlockNumbers(syncInfoDTO2))),
                    getErrorEvent(throwable -> {}, getSyncInfoTask),
                    getEmptyEvent()
            );
            // mining status
            Task<Optional<Boolean>> getMiningStatusTask = getApiTask(o -> generalKernelInfoRetriever.isMining(), null);
            runApiTask(
                    getMiningStatusTask,
                    evt -> isMining.setText(displayStringOfOptional(getMiningStatusTask.getValue())),
                    getErrorEvent(throwable -> {}, getSyncInfoTask),
                    getEmptyEvent()
            );
        }
    }

    private String displayStringOfOptional(Optional<?> opt) {
        if(!opt.isPresent()) {
            return "<unknown>";
        } else {
            return String.valueOf(opt.get());
        }
    }

    @Subscribe
    private void handleKernelLaunched(final KernelProcEvent.KernelLaunchedEvent ev) {
        LOG.trace("handleKernelLaunched");
        kernelConnection.connect(); // TODO: what if we launched the process but can't connect?
        kernelStatusLabel.setText("Running");
        kernelUpdateTimer.start(); // should actually wait until connect() happens
        enableTerminateButton();
    }

    @Subscribe
    private void handleKernelTerminated(final KernelProcEvent.KernelTerminatedEvent ev) {
        enableLaunchButton();
        numPeersLabel.setText("--");
        blocksLabel.setText("--");
        isMining.setText("--");
    }

    // -- Handlers for View components ------------------------------------------------------------
    public void launchKernel(MouseEvent ev) throws Exception {
        disableLaunchTerminateButtons();
        kernelStatusLabel.setText("Starting...");
        try {
            kernelLauncher.launch();
        } catch (RuntimeException ex) {
            enableLaunchButton();
        }
    }

    public void terminateKernel(MouseEvent ev) throws Exception {
        LOG.info("terminateKernel");
        disableLaunchTerminateButtons();
        kernelStatusLabel.setText("Terminating...");

        try {
            if (kernelLauncher.hasLaunchedInstance()
                    || (!kernelLauncher.hasLaunchedInstance() && kernelLauncher.tryResume())) {
                kernelUpdateTimer.stop();
                kernelConnection.disconnect();
                kernelLauncher.terminate();
                kernelStatusLabel.setText("Not running");
            }
        } catch (RuntimeException ex) {
            LOG.error("Termination error", ex);
            enableLaunchButton();
//            kernelUpdateTimer.fireImmediatelyAndThenStart();
        }
    }

    // -- Helpers methods -------------------------------------------------------------------------
    private void enableLaunchButton() {
        launchKernelButton.setDisable(false);
        terminateKernelButton.setDisable(true);
    }

    private void enableTerminateButton() {
        launchKernelButton.setDisable(true);
        terminateKernelButton.setDisable(false);
    }

    private void disableLaunchTerminateButtons() {
        launchKernelButton.setDisable(true);
        terminateKernelButton.setDisable(true);
    }
}