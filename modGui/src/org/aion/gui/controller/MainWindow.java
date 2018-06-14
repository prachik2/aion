package org.aion.gui.controller;

import com.google.common.eventbus.Subscribe;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.aion.gui.events.EventBusFactory;
import org.aion.gui.events.HeaderPaneButtonEvent;
import org.aion.gui.events.WindowControlsEvent;
import org.aion.wallet.util.AionConstants;
import org.aion.wallet.util.DataUpdater;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.Executors;

public class MainWindow extends Application {
    private static final String TITLE = "Aion Kernel";
    private static final String MAIN_WINDOW_FXML = "MainWindow.fxml";
    private static final String AION_LOGO = "components/icons/aion_logo.png";

    private double xOffset;
    private double yOffset;
    private Stage stage;
    private final Timer timer = new Timer();

    private final Map<HeaderPaneButtonEvent.Type, Node> panes = new HashMap<>();

    @Override
    public void start(Stage stage) throws Exception {
        startFancy(stage);
    }

    /** This impl contains start-up code to make the GUI more fancy.  Lifted from aion_ui.  */
    private void startFancy(Stage stage) throws Exception {

        this.stage = stage;
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.getIcons().add(new Image(getClass().getResourceAsStream(AION_LOGO)));

        registerEventBusConsumer();

        Parent root = FXMLLoader.load(getClass().getResource(MAIN_WINDOW_FXML));
        root.setOnMousePressed(this::handleMousePressed);
        root.setOnMouseDragged(this::handleMouseDragged);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        stage.setOnCloseRequest(t -> shutDown());

        stage.setTitle(TITLE);
        stage.setScene(scene);
        stage.show();

        panes.put(HeaderPaneButtonEvent.Type.OVERVIEW, scene.lookup("#overviewPane"));
        panes.put(HeaderPaneButtonEvent.Type.SETTINGS, scene.lookup("#settingsPane"));

        timer.schedule(
                new DataUpdater(),
                AionConstants.BLOCK_MINING_TIME_MILLIS,
                3 * AionConstants.BLOCK_MINING_TIME_MILLIS
        );
    }

    private void registerEventBusConsumer() {
        EventBusFactory.getBus(WindowControlsEvent.ID).register(this);
        EventBusFactory.getBus(HeaderPaneButtonEvent.ID).register(this);
    }

    private void handleMouseDragged(final MouseEvent event) {
        stage.setX(event.getScreenX() - xOffset);
        stage.setY(event.getScreenY() - yOffset);
    }

    private void shutDown() {
        System.out.println("XXX MainWindow#shutDown() called");
        Platform.exit();
        //BlockchainConnector.getInstance().close();
        Executors.newSingleThreadExecutor().submit(() -> System.exit(0));
        timer.cancel();
        timer.purge();
    }

    private void handleMousePressed(final MouseEvent event) {
        xOffset = event.getSceneX();
        yOffset = event.getSceneY();
    }

    @Subscribe
    private void handleWindowControlsEvent(final org.aion.gui.events.WindowControlsEvent event) {
        switch (event.getType()) {
            case MINIMIZE:
                minimize(event);
                break;
            case CLOSE:
                shutDown();
                break;
        }
    }

    @Subscribe
    private void handleHeaderPaneButtonEvent(final org.aion.gui.events.HeaderPaneButtonEvent event) {
        if(stage.getScene() == null) {
            return;
        }
//        log.debug(event.getType().toString());
        // todo: refactor by adding a view controller
        for(Map.Entry<HeaderPaneButtonEvent.Type, Node> entry: panes.entrySet()) {
            if(event.getType().equals(entry.getKey())) {
                entry.getValue().setVisible(true);
            } else {
                entry.getValue().setVisible(false);
            }
        }
    }

    private void minimize(final org.aion.gui.events.WindowControlsEvent event) {
        ((Stage) event.getSource().getScene().getWindow()).setIconified(true);
    }


}
