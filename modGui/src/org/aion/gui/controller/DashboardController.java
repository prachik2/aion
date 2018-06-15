package org.aion.gui.controller;

import com.google.common.eventbus.Subscribe;
import javafx.scene.input.MouseEvent;
import org.aion.gui.events.HeaderPaneButtonEvent;
import org.aion.gui.events.RefreshEvent;
import org.aion.gui.model.AccountDTO;
import org.aion.mcf.config.CfgGuiLauncher;
import org.aion.os.KernelInstance;
import org.aion.os.KernelLauncher;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class DashboardController extends AbstractController {
    // FIXME
    KernelLauncher kernelLauncher = new KernelLauncher(CfgGuiLauncher.AUTODETECTING_CONFIG);


    @Override
    public void internalInit(final URL location, final ResourceBundle resources) {
    }

    @Override
    protected void registerEventBusConsumer() {
    }

    private void reloadAccounts() {

    }

    private void reloadAccountObservableList(List<AccountDTO> accounts) {
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
        //FIXME
        System.out.println("launchKernel");
        kernelLauncher.launch();

        Thread.sleep(2000);
        KernelConnection.getInstance().connect();
    }

    public void terminateKernel(MouseEvent mouseEvent) throws Exception {
        //FIXME
        System.out.println("terminateKernel");
        System.out.println("kernelLauncher.hasLaunchedInstance = " + kernelLauncher.hasLaunchedInstance() );
        if(kernelLauncher.hasLaunchedInstance()
            || (!kernelLauncher.hasLaunchedInstance() && kernelLauncher.tryResume())) {
            kernelLauncher.terminate();
            KernelConnection.getInstance().disconnect();
        }
    }
}
