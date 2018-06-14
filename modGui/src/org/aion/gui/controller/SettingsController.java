package org.aion.gui.controller;

import com.google.common.eventbus.Subscribe;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.aion.log.AionLoggerFactory;
import org.aion.gui.model.LightAppSettings;
import org.aion.gui.model.ApiType;
import org.aion.gui.events.EventBusFactory;
import org.aion.gui.events.EventPublisher;
import org.aion.gui.events.HeaderPaneButtonEvent;
import org.slf4j.Logger;

import java.net.URL;
import java.util.ResourceBundle;

public class SettingsController extends AbstractController {

    //private static final Logger logger = AionLoggerFactory.getLogger(org.aion.log.LogEnum.UI.name());
    private static final Logger logger = AionLoggerFactory.getLogger("temp");

    private final KernelConnection kernel = KernelConnection.getInstance();

    @FXML
    public TextField protocol;
    @FXML
    public TextField address;
    @FXML
    public TextField port;
    @FXML
    public Label notification;

    private LightAppSettings settings;

    public SettingsController() {
        /* FIXME probably not the right place for this */
        /* */
        settings = new LightAppSettings("127.0.0.1", "8547", "tcp", ApiType.JAVA);
    }

    @Override
    protected void internalInit(final URL location, final ResourceBundle resources) {
        reloadView();
    }

    @Override
    protected void registerEventBusConsumer() {
        EventBusFactory.getBus(HeaderPaneButtonEvent.ID).register(this);
    }

    public void changeSettings() {
        EventPublisher.fireApplicationSettingsChanged(new LightAppSettings(address.getText().trim(), port.getText().trim(),
                protocol.getText().trim(), settings.getType()));
        notification.setText("Changes applied");

    }

    @Subscribe
    private void handleHeaderPaneButtonEvent(final HeaderPaneButtonEvent event) {
        if (event.getType().equals(HeaderPaneButtonEvent.Type.SETTINGS)) {
            reloadView();
        }
    }

    private void reloadView() {
        settings = kernel.getSettings();
        protocol.setText(settings.getProtocol());
        address.setText(settings.getAddress());
        port.setText(settings.getPort());
        notification.setText("");
    }
}
