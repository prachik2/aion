package org.aion.gui;

import javafx.scene.Parent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import org.aion.gui.controller.DashboardController;
import org.aion.gui.controller.MainWindow;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit.ApplicationTest;

public class KernelLaunchIntegTest extends ApplicationTest {
    private MainWindow unit;

    @Before
    public void setUp () throws Exception {
    }

    @Override
    public void start(Stage stage) throws Exception {
        this.unit = new MainWindow();
        unit.start(stage);
    }

    @After
    public void tearDown () throws Exception {
        FxToolkit.hideStage();
        release(new KeyCode[]{});
        release(new MouseButton[]{});
    }

    /**
     * Use case: use UI to start an instance of kernel, then terminate it.
     */
    @Test
    public void testLaunchTerminate() throws InterruptedException {
        clickOn("#launchKernelButton");
        Thread.sleep(5000);
    }
}
