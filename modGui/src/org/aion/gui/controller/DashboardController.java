package org.aion.gui.controller;

import com.google.common.eventbus.Subscribe;
import javafx.scene.input.MouseEvent;
import org.aion.api.log.AionLoggerFactory;
import org.aion.api.log.LogEnum;
import org.aion.gui.events.HeaderPaneButtonEvent;
import org.aion.gui.events.RefreshEvent;
import org.aion.gui.model.AccountDTO;
import org.aion.mcf.config.CfgGuiLauncher;
import org.aion.os.KernelInstance;
import org.aion.os.KernelLauncher;
import org.slf4j.Logger;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class DashboardController extends AbstractController {
    // FIXME
    private static final Logger LOG = org.aion.log.AionLoggerFactory.getLogger(org.aion.log.LogEnum.GUI.name());

    private final KernelLauncher kernelLauncher; //= new KernelLauncher(CfgGuiLauncher.AUTODETECTING_CONFIG);
    private final KernelConnection kernelConnection;

    public DashboardController(KernelLauncher kernelLauncher,
                               KernelConnection kernelConnection) {
        this.kernelLauncher = kernelLauncher;
        this.kernelConnection = kernelConnection;
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
        LOG.debug("Launching kernel");
        kernelLauncher.launch();

        Thread.sleep(2000);
        kernelConnection.connect();
    }

    public void terminateKernel(MouseEvent mouseEvent) throws Exception {
        //FIXME
        LOG.debug("Exiting kernel");
        System.out.println("kernelLauncher.hasLaunchedInstance = " + kernelLauncher.hasLaunchedInstance() );
        if(kernelLauncher.hasLaunchedInstance()
            || (!kernelLauncher.hasLaunchedInstance() && kernelLauncher.tryResume())) {
            kernelLauncher.terminate();
            kernelConnection.disconnect();
        }
    }
}
