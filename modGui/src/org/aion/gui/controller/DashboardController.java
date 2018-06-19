package org.aion.gui.controller;

import com.google.common.eventbus.Subscribe;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import org.aion.gui.events.HeaderPaneButtonEvent;
import org.aion.gui.events.RefreshEvent;
import org.aion.gui.model.AccountDTO;
import org.aion.log.AionLoggerFactory;
import org.aion.os.KernelLauncher;
import org.slf4j.Logger;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;

public class DashboardController extends AbstractController {
    private final KernelLauncher kernelLauncher;
    private final KernelConnection kernelConnection;
    private final KernelUpdateTimer kernelUpdateTimer;

    @FXML
    private Button launchKernelButton;

    @FXML
    private Button terminateKernelButton;

    private static final Logger LOG = AionLoggerFactory.getLogger(org.aion.log.LogEnum.GUI.name());

    public DashboardController(KernelLauncher kernelLauncher,
                               KernelConnection kernelConnection,
                               KernelUpdateTimer kernelUpdateTimer) {
        this.kernelLauncher = kernelLauncher;
        this.kernelConnection = kernelConnection;
        this.kernelUpdateTimer = kernelUpdateTimer;
    }

    @Override
    public void internalInit(final URL location, final ResourceBundle resources) {
    }

    @Override
    protected void registerEventBusConsumer() {
    }

    @Subscribe
    private void handleAccountChanged(final AccountDTO account) {

    }

    @Subscribe
    private void handleHeaderPaneButtonEvent(final HeaderPaneButtonEvent event) {


    }

    @Subscribe
    private void handleRefreshEvent(final RefreshEvent event){
    }

    public void launchKernel(MouseEvent mouseEvent) throws Exception {
        LOG.debug("launchKernel clicked");
        try {
            launchKernelButton.setDisable(true);
            kernelLauncher.launch();
            terminateKernelButton.setDisable(false);
            Thread.sleep(2000);
            kernelConnection.connect();
            kernelUpdateTimer.fireImmediatelyAndThenStart();
        } catch (RuntimeException ex) {
            launchKernelButton.setDisable(false);
            terminateKernelButton.setDisable(true);
            kernelUpdateTimer.fireImmediatelyAndThenStop();
        }
    }

    public void terminateKernel(MouseEvent mouseEvent) throws Exception {
        LOG.debug("Exiting kernel");
        try {
            if (kernelLauncher.hasLaunchedInstance()
                    || (!kernelLauncher.hasLaunchedInstance() && kernelLauncher.tryResume())) {
                launchKernelButton.setDisable(false);
                kernelLauncher.terminate();
                kernelConnection.disconnect();
                terminateKernelButton.setDisable(true);
                kernelUpdateTimer.fireImmediatelyAndThenStop();
            }

        } catch (RuntimeException ex) {
            launchKernelButton.setDisable(true);
            terminateKernelButton.setDisable(false);
            kernelUpdateTimer.fireImmediatelyAndThenStart();
        }
    }
}
