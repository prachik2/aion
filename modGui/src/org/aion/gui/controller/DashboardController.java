package org.aion.gui.controller;

import com.google.common.eventbus.Subscribe;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import org.aion.mcf.config.CfgGuiLauncher;
import org.aion.os.KernelLauncher;
import org.aion.gui.model.AccountDTO;
import org.aion.gui.events.HeaderPaneButtonEvent;
import org.aion.gui.events.RefreshEvent;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/* copy pasta of OverviewController and then removed stuff */
public class DashboardController extends AbstractController {

    //    private final BlockchainConnector blockchainConnector = BlockchainConnector.getInstance();
    @FXML
    private ListView<AccountDTO> accountListView;
    //    private AddAccountDialog addAccountDialog;
    private AccountDTO account;


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

    public void openAddAccountDialog(MouseEvent mouseEvent) throws Exception {
        //FIXME
        System.out.println("openAddAccountDialog");
        new KernelLauncher(CfgGuiLauncher.AUTODETECTING_CONFIG).launch();
    }
}
