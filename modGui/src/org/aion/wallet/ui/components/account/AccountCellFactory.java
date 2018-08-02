package org.aion.wallet.ui.components.account;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import org.aion.wallet.account.AccountManager;
import org.aion.wallet.console.ConsoleManager;
import org.aion.wallet.dto.AccountDTO;
import org.aion.wallet.ui.components.partials.SaveKeystoreDialog;
import org.aion.wallet.ui.components.partials.UnlockAccountDialog;

public class AccountCellFactory implements Callback<ListView<AccountDTO>, ListCell<AccountDTO>> {
    private final UnlockAccountDialog accountUnlockDialog;
    private final SaveKeystoreDialog saveKeystoreDialog;

    public AccountCellFactory(UnlockAccountDialog unlockAccountDialog,
                              SaveKeystoreDialog saveKeystoreDialog) {
        this.accountUnlockDialog = unlockAccountDialog;
        this.saveKeystoreDialog = saveKeystoreDialog;
    }

    public AccountCellFactory(AccountManager accountManager, ConsoleManager consoleManager) {
        this.accountUnlockDialog = new UnlockAccountDialog(accountManager, consoleManager);
        this.saveKeystoreDialog = new SaveKeystoreDialog(accountManager, consoleManager);
    }

    @Override
    public ListCell<AccountDTO> call(ListView<AccountDTO> param) {
        return new AccountCellItem(accountUnlockDialog, saveKeystoreDialog);
    }
}